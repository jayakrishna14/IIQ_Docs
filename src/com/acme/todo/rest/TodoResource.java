package com.acme.todo.rest;

import com.acme.todo.service.Todo;
import com.acme.todo.service.TodoService;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import sailpoint.authorization.Authorizer;
import sailpoint.integration.ListResult;
import sailpoint.plugin.PluginContext;
import sailpoint.rest.plugin.AllowAll;
import sailpoint.rest.plugin.BasePluginResource;
import sailpoint.rest.plugin.Deferred;
import sailpoint.rest.plugin.RequiredRight;
import sailpoint.rest.plugin.SystemAdmin;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Util;















@Path("TodoPlugin")
@Produces({"application/json"})
@Consumes({"application/json"})
public class TodoResource
  extends BasePluginResource
{
  public String getPluginName() {
    return "TodoPlugin";
  }
 
  @GET
  @Path("todos")
  @AllowAll
  public ListResult getTodos() throws GeneralException {
    TodoService todoService = getTodoService();
    List<Todo> todos = todoService.getTodosForUser(getLoggedInUserId());
    return new ListResult(todos, todos.size());
  }

  @GET
  @Path("todos/{id}")
  @Deferred
  public Todo getTodo(@PathParam("id") String id) throws GeneralException {
    Todo todo = getTodoService().getTodo(id);
    
    authorize(new Authorizer[] { new TodoAuthorizer(todo) });
    
    return todo;
  }
 
  @POST
  @Path("todos")
  @AllowAll
  public Todo addTodo(Map<String, String> data) throws GeneralException {
    return getTodoService().createTodo(getCreateTodoData(data));
  }
 
  @POST
  @Path("todos/{id}")
  @Deferred
  public void completeTodo(@PathParam("id") String id) throws GeneralException {
    TodoService todoService = getTodoService();
    Todo todo = todoService.getTodo(id);
    
    authorize(new Authorizer[] { new TodoAuthorizer(todo) });
    
    todoService.completeTodo(todo);
  }
 
  @DELETE
  @Path("todos/{id}")
  @Deferred
  public void deleteTodo(@PathParam("id") String id) throws GeneralException {
    TodoService todoService = getTodoService();
    Todo todo = todoService.getTodo(id);
    
    authorize(new Authorizer[] { new TodoAuthorizer(todo) });
    
    todoService.deleteTodo(todo);
  }

  @DELETE
  @Path("todos/clear/{userId}")
  @RequiredRight("ViewIdentity")
  public void deleteAllUserTodos(@PathParam("userId") String userId) throws GeneralException {
    getTodoService().deleteUserTodos(userId);
  }
 
  @DELETE
  @Path("todos")
  @SystemAdmin
  public void deleteAllTodos() throws GeneralException {
    getTodoService().deleteAllTodos();
  }
 
  private TodoService.CreateTodoData getCreateTodoData(Map<String, String> data) throws GeneralException {
    String id = Util.uuid();
    
    String name = data.get("name");
    int estimate = Util.otoi(data.get("time"));
    String notes = data.get("notes");
    
    TodoService.CreateTodoData todoData = new TodoService.CreateTodoData();
    todoData.setId(id);
    todoData.setUserId(getLoggedInUserId());
    todoData.setName(name);
    todoData.setEstimate(estimate);
    todoData.setNotes(notes);
    
    return todoData;
  }
 
  private String getLoggedInUserId() throws GeneralException {
    return getLoggedInUser().getId();
  }
 
  private TodoService getTodoService() {
    return new TodoService((PluginContext)this);
  }
}