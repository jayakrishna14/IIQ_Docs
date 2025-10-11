/*    */ package com.acme.todo.rest;
/*    */ 
/*    */ import com.acme.todo.service.Todo;
/*    */ import sailpoint.authorization.Authorizer;
/*    */ import sailpoint.authorization.UnauthorizedAccessException;
/*    */ import sailpoint.tools.GeneralException;
/*    */ import sailpoint.tools.Message;
/*    */ import sailpoint.web.UserContext;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ class TodoAuthorizer
/*    */   implements Authorizer
/*    */ {
/*    */   private Todo todo;
/*    */   
/*    */   public TodoAuthorizer(Todo todo) {
/* 31 */     this.todo = todo;
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public void authorize(UserContext userContext) throws GeneralException {
/* 39 */     if (!userContext.getLoggedInUser().getId().equals(this.todo.getUserId()))
/* 40 */       throw new UnauthorizedAccessException(Message.localize("TodoPlugin_unauthorized_access")); 
/*    */   }
/*    */ }


/* Location:              C:\Users\jkp14\Downloads\TodoPlugin-V3.zip!\lib\TodoPlugin.jar!\com\acme\todo\rest\TodoAuthorizer.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */