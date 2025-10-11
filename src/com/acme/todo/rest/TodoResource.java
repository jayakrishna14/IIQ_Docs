/*     */ package com.acme.todo.rest;
/*     */ 
/*     */ import com.acme.todo.service.Todo;
/*     */ import com.acme.todo.service.TodoService;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import javax.ws.rs.Consumes;
/*     */ import javax.ws.rs.DELETE;
/*     */ import javax.ws.rs.GET;
/*     */ import javax.ws.rs.POST;
/*     */ import javax.ws.rs.Path;
/*     */ import javax.ws.rs.PathParam;
/*     */ import javax.ws.rs.Produces;
/*     */ import sailpoint.authorization.Authorizer;
/*     */ import sailpoint.integration.ListResult;
/*     */ import sailpoint.plugin.PluginContext;
/*     */ import sailpoint.rest.plugin.AllowAll;
/*     */ import sailpoint.rest.plugin.BasePluginResource;
/*     */ import sailpoint.rest.plugin.Deferred;
/*     */ import sailpoint.rest.plugin.RequiredRight;
/*     */ import sailpoint.rest.plugin.SystemAdmin;
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
/*     */ 
/*     */ @Path("TodoPlugin")
/*     */ @Produces({"application/json"})
/*     */ @Consumes({"application/json"})
/*     */ public class TodoResource
/*     */   extends BasePluginResource
/*     */ {
/*     */   public String getPluginName() {
/*  46 */     return "TodoPlugin";
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @GET
/*     */   @Path("todos")
/*     */   @AllowAll
/*     */   public ListResult getTodos() throws GeneralException {
/*  59 */     TodoService todoService = getTodoService();
/*  60 */     List<Todo> todos = todoService.getTodosForUser(getLoggedInUserId());
/*  61 */     return new ListResult(todos, todos.size());
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @GET
/*     */   @Path("todos/{id}")
/*     */   @Deferred
/*     */   public Todo getTodo(@PathParam("id") String id) throws GeneralException {
/*  75 */     Todo todo = getTodoService().getTodo(id);
/*     */     
/*  77 */     authorize(new Authorizer[] { new TodoAuthorizer(todo) });
/*     */     
/*  79 */     return todo;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @POST
/*     */   @Path("todos")
/*     */   @AllowAll
/*     */   public Todo addTodo(Map<String, String> data) throws GeneralException {
/*  93 */     return getTodoService().createTodo(getCreateTodoData(data));
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @POST
/*     */   @Path("todos/{id}")
/*     */   @Deferred
/*     */   public void completeTodo(@PathParam("id") String id) throws GeneralException {
/* 106 */     TodoService todoService = getTodoService();
/* 107 */     Todo todo = todoService.getTodo(id);
/*     */     
/* 109 */     authorize(new Authorizer[] { new TodoAuthorizer(todo) });
/*     */     
/* 111 */     todoService.completeTodo(todo);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @DELETE
/*     */   @Path("todos/{id}")
/*     */   @Deferred
/*     */   public void deleteTodo(@PathParam("id") String id) throws GeneralException {
/* 124 */     TodoService todoService = getTodoService();
/* 125 */     Todo todo = todoService.getTodo(id);
/*     */     
/* 127 */     authorize(new Authorizer[] { new TodoAuthorizer(todo) });
/*     */     
/* 129 */     todoService.deleteTodo(todo);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @DELETE
/*     */   @Path("todos/clear/{userId}")
/*     */   @RequiredRight("ViewIdentity")
/*     */   public void deleteAllUserTodos(@PathParam("userId") String userId) throws GeneralException {
/* 142 */     getTodoService().deleteUserTodos(userId);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @DELETE
/*     */   @Path("todos")
/*     */   @SystemAdmin
/*     */   public void deleteAllTodos() throws GeneralException {
/* 154 */     getTodoService().deleteAllTodos();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private TodoService.CreateTodoData getCreateTodoData(Map<String, String> data) throws GeneralException {
/* 165 */     String id = Util.uuid();
/*     */     
/* 167 */     String name = data.get("name");
/* 168 */     int estimate = Util.otoi(data.get("time"));
/* 169 */     String notes = data.get("notes");
/*     */     
/* 171 */     TodoService.CreateTodoData todoData = new TodoService.CreateTodoData();
/* 172 */     todoData.setId(id);
/* 173 */     todoData.setUserId(getLoggedInUserId());
/* 174 */     todoData.setName(name);
/* 175 */     todoData.setEstimate(estimate);
/* 176 */     todoData.setNotes(notes);
/*     */     
/* 178 */     return todoData;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private String getLoggedInUserId() throws GeneralException {
/* 188 */     return getLoggedInUser().getId();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private TodoService getTodoService() {
/* 197 */     return new TodoService((PluginContext)this);
/*     */   }
/*     */ }


/* Location:              C:\Users\jkp14\Downloads\TodoPlugin-V3.zip!\lib\TodoPlugin.jar!\com\acme\todo\rest\TodoResource.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */