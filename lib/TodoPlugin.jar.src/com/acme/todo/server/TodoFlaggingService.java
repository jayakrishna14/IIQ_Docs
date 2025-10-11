/*     */ package com.acme.todo.server;
/*     */ 
/*     */ import com.acme.todo.service.FlaggedUser;
/*     */ import com.acme.todo.service.FlaggedUserService;
/*     */ import com.acme.todo.service.TodoService;
/*     */ import java.util.List;
/*     */ import org.apache.commons.logging.Log;
/*     */ import org.apache.commons.logging.LogFactory;
/*     */ import sailpoint.api.SailPointContext;
/*     */ import sailpoint.object.Identity;
/*     */ import sailpoint.plugin.PluginContext;
/*     */ import sailpoint.server.BasePluginService;
/*     */ import sailpoint.tools.GeneralException;
/*     */ import sailpoint.tools.Util;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class TodoFlaggingService
/*     */   extends BasePluginService
/*     */ {
/*  32 */   private static final Log LOG = LogFactory.getLog(TodoFlaggingService.class);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  58 */   private TodoService todoService = new TodoService((PluginContext)this);
/*  59 */   private FlaggedUserService flaggedUserService = new FlaggedUserService((PluginContext)this);
/*     */   
/*     */   private static final String SETTING_MAX_UNTIL_FLAGGED = "maxUntilFlagged";
/*     */   
/*     */   private int maxUntilFlagged;
/*     */ 
/*     */   
/*     */   public String getPluginName() {
/*  67 */     return "TodoPlugin";
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void configure(SailPointContext context) throws GeneralException {
/*  75 */     this.maxUntilFlagged = getSettingInt("maxUntilFlagged");
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void execute(SailPointContext context) throws GeneralException {
/*  83 */     pruneFlagged();
/*  84 */     flagUsers(context);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void pruneFlagged() throws GeneralException {
/*  93 */     List<FlaggedUser> flaggedUsers = this.flaggedUserService.getFlaggedUsers();
/*  94 */     for (FlaggedUser flaggedUser : Util.iterate(flaggedUsers)) {
/*  95 */       int activeTodos = this.todoService.getActiveTodosForUser(flaggedUser.getUserId());
/*  96 */       if (activeTodos <= this.maxUntilFlagged) {
/*  97 */         this.flaggedUserService.pruneFlaggedUser(flaggedUser);
/*     */       }
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void flagUsers(SailPointContext context) throws GeneralException {
/* 109 */     if (this.maxUntilFlagged <= 0) {
/*     */       return;
/*     */     }
/*     */     
/* 113 */     List<String> userIds = this.todoService.getUsersWithOpenTodos();
/* 114 */     for (String userId : Util.iterate(userIds)) {
/* 115 */       int numTodos = this.todoService.getActiveTodosForUser(userId);
/* 116 */       if (numTodos > this.maxUntilFlagged) {
/* 117 */         String identityName = userId;
/*     */         
/* 119 */         Identity identity = (Identity)context.getObjectById(Identity.class, userId);
/* 120 */         if (identity != null) {
/* 121 */           identityName = identity.getDisplayableName();
/*     */         }
/*     */         
/* 124 */         if (!this.flaggedUserService.isUserFlagged(userId)) {
/* 125 */           FlaggedUserService.CreateFlagData data = new FlaggedUserService.CreateFlagData();
/* 126 */           data.setId(Util.uuid());
/* 127 */           data.setUserId(userId);
/* 128 */           data.setUsername(identityName);
/* 129 */           data.setNumTodos(numTodos);
/*     */           
/* 131 */           this.flaggedUserService.flagUser(data);
/*     */         } 
/*     */       } 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\jkp14\Downloads\TodoPlugin-V3.zip!\lib\TodoPlugin.jar!\com\acme\todo\server\TodoFlaggingService.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */