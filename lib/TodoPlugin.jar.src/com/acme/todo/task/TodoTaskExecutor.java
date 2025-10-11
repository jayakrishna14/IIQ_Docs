/*    */ package com.acme.todo.task;
/*    */ 
/*    */ import com.acme.todo.service.TodoService;
/*    */ import sailpoint.api.SailPointContext;
/*    */ import sailpoint.object.Attributes;
/*    */ import sailpoint.object.TaskResult;
/*    */ import sailpoint.object.TaskSchedule;
/*    */ import sailpoint.plugin.PluginContext;
/*    */ import sailpoint.task.BasePluginTaskExecutor;
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
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class TodoTaskExecutor
/*    */   extends BasePluginTaskExecutor
/*    */ {
/*    */   private static final String ATT_NUM_DELETED = "numTodosDeleted";
/*    */   
/*    */   public String getPluginName() {
/* 36 */     return "TodoPlugin";
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public void execute(SailPointContext context, TaskSchedule schedule, TaskResult result, Attributes<String, Object> args) throws Exception {
/* 46 */     int numDeleted = getTodoService().deleteCompletedTodos();
/*    */     
/* 48 */     result.put("numTodosDeleted", Integer.valueOf(numDeleted));
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public boolean terminate() {
/* 56 */     return true;
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   private TodoService getTodoService() {
/* 65 */     return new TodoService((PluginContext)this);
/*    */   }
/*    */ }


/* Location:              C:\Users\jkp14\Downloads\TodoPlugin-V3.zip!\lib\TodoPlugin.jar!\com\acme\todo\task\TodoTaskExecutor.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */