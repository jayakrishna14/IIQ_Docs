package com.acme.todo.rest;

import com.acme.todo.service.Todo;
import sailpoint.authorization.Authorizer;
import sailpoint.authorization.UnauthorizedAccessException;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Message;
import sailpoint.web.UserContext;

class TodoAuthorizer
  implements Authorizer
{
  private Todo todo;
  
  public TodoAuthorizer(Todo todo) {
    this.todo = todo;
  }
 
  public void authorize(UserContext userContext) throws GeneralException {
    if (!userContext.getLoggedInUser().getId().equals(this.todo.getUserId()))
      throw new UnauthorizedAccessException(Message.localize("TodoPlugin_unauthorized_access")); 
  }
}