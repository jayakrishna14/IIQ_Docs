package com.acme.todo.task;

import com.acme.todo.service.TodoService;
import sailpoint.api.SailPointContext;
import sailpoint.object.Attributes;
import sailpoint.object.TaskResult;
import sailpoint.object.TaskSchedule;
import sailpoint.plugin.PluginContext;
import sailpoint.task.BasePluginTaskExecutor;

public class TodoTaskExecutor  extends BasePluginTaskExecutor {
  private static final String ATT_NUM_DELETED = "numTodosDeleted";
  
  public String getPluginName() {
    return "TodoPlugin";
  }

  public void execute(SailPointContext context, TaskSchedule schedule, TaskResult result, Attributes<String, Object> args) throws Exception {
    int numDeleted = getTodoService().deleteCompletedTodos();
    
    result.put("numTodosDeleted", Integer.valueOf(numDeleted));
  }

  public boolean terminate() {
    return true;
  }

  private TodoService getTodoService() {
    return new TodoService((PluginContext)this);
  }
}