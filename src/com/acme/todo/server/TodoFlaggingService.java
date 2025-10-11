package com.acme.todo.server;

import com.acme.todo.service.FlaggedUser;
import com.acme.todo.service.FlaggedUserService;
import com.acme.todo.service.TodoService;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sailpoint.api.SailPointContext;
import sailpoint.object.Identity;
import sailpoint.plugin.PluginContext;
import sailpoint.server.BasePluginService;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Util;

public class TodoFlaggingService
  extends BasePluginService
{
  private static final Log LOG = LogFactory.getLog(TodoFlaggingService.class);
  private TodoService todoService = new TodoService((PluginContext)this);
  private FlaggedUserService flaggedUserService = new FlaggedUserService((PluginContext)this);
  private static final String SETTING_MAX_UNTIL_FLAGGED = "maxUntilFlagged";
  private int maxUntilFlagged;

  
  public String getPluginName() {
    return "TodoPlugin";
  }

  public void configure(SailPointContext context) throws GeneralException {
    this.maxUntilFlagged = getSettingInt("maxUntilFlagged");
  }

  public void execute(SailPointContext context) throws GeneralException {
    pruneFlagged();
    flagUsers(context);
  }

  public void pruneFlagged() throws GeneralException {
    List<FlaggedUser> flaggedUsers = this.flaggedUserService.getFlaggedUsers();
    for (FlaggedUser flaggedUser : Util.iterate(flaggedUsers)) {
      int activeTodos = this.todoService.getActiveTodosForUser(flaggedUser.getUserId());
      if (activeTodos <= this.maxUntilFlagged) {
        this.flaggedUserService.pruneFlaggedUser(flaggedUser);
      }
    } 
  }

  public void flagUsers(SailPointContext context) throws GeneralException {
    if (this.maxUntilFlagged <= 0) {
      return;
    }
    
    List<String> userIds = this.todoService.getUsersWithOpenTodos();
    for (String userId : Util.iterate(userIds)) {
      int numTodos = this.todoService.getActiveTodosForUser(userId);
      if (numTodos > this.maxUntilFlagged) {
        String identityName = userId;
        
        Identity identity = (Identity)context.getObjectById(Identity.class, userId);
        if (identity != null) {
          identityName = identity.getDisplayableName();
        }
        
        if (!this.flaggedUserService.isUserFlagged(userId)) {
          FlaggedUserService.CreateFlagData data = new FlaggedUserService.CreateFlagData();
          data.setId(Util.uuid());
          data.setUserId(userId);
          data.setUsername(identityName);
          data.setNumTodos(numTodos);
          
          this.flaggedUserService.flagUser(data);
        } 
      } 
    } 
  }
}