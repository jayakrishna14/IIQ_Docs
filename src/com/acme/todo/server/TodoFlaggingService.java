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

public class TodoFlaggingService extends BasePluginService
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
    this.maxUntilFlagged = getSettingInt(SETTING_MAX_UNTIL_FLAGGED);
    LOG.info("TodoFlaggingService configured: maxUntilFlagged=" + this.maxUntilFlagged);
  }

  public void execute(SailPointContext context) throws GeneralException {
    LOG.info("TodoFlaggingService execution started");
    pruneFlagged();
    flagUsers(context);
    LOG.info("TodoFlaggingService execution finished");
  }

  public void pruneFlagged() throws GeneralException {
    LOG.info("Pruning flagged users - retrieving list");
    List<FlaggedUser> flaggedUsers = this.flaggedUserService.getFlaggedUsers();
    LOG.info("Prune: found " + (flaggedUsers == null ? 0 : flaggedUsers.size()) + " flagged users");
    for (FlaggedUser flaggedUser : Util.iterate(flaggedUsers)) {
      int activeTodos = this.todoService.getActiveTodosForUser(flaggedUser.getUserId());
      LOG.info("Prune: userId=" + flaggedUser.getUserId() + " username=" + flaggedUser.getUsername() + " activeTodos=" + activeTodos + " maxUntilFlagged=" + this.maxUntilFlagged);
      if (activeTodos <= this.maxUntilFlagged) {
        LOG.info("Prune: removing flagged user id=" + flaggedUser.getId());
        this.flaggedUserService.pruneFlaggedUser(flaggedUser);
      }
    }
  }

  public void flagUsers(SailPointContext context) throws GeneralException {
    if (this.maxUntilFlagged <= 0) {
      return;
    }
    LOG.info("Flagging users - scanning for users with open todos");
    List<String> userIds = this.todoService.getUsersWithOpenTodos();
    LOG.info("Flagging: found " + (userIds == null ? 0 : userIds.size()) + " users with open todos");
    for (String userId : Util.iterate(userIds)) {
      int numTodos = this.todoService.getActiveTodosForUser(userId);
      LOG.info("Flagging: checking userId=" + userId + " numTodos=" + numTodos + " maxUntilFlagged=" + this.maxUntilFlagged);
      if (numTodos > this.maxUntilFlagged) {
        String identityName = userId;
        
        Identity identity = (Identity)context.getObjectById(Identity.class, userId);
        if (identity != null) {
          identityName = identity.getDisplayableName();
        }
        
        if (!this.flaggedUserService.isUserFlagged(userId)) {
          LOG.info("Flagging: user exceeds threshold and is not flagged - creating flag for userId=" + userId + " username=" + identityName + " numTodos=" + numTodos);
          FlaggedUserService.CreateFlagData data = new FlaggedUserService.CreateFlagData();
          data.setId(Util.uuid());
          data.setUserId(userId);
          data.setUsername(identityName);
          data.setNumTodos(numTodos);
          
          this.flaggedUserService.flagUser(data);
        } else {
          LOG.info("Flagging: user already flagged userId=" + userId);
        }
      }
    }
  }
}