package com.acme.todo.util;

public class TodoQuery {
  public static final String TODOS = "SELECT * FROM tp_todo_list WHERE user_id=? ORDER BY completed_on ASC, created ASC";
  
  public static final String TODO = "SELECT * FROM tp_todo_list WHERE id=?";
  
  public static final String ADD = "INSERT INTO tp_todo_list (id, user_id, name, estimate, notes, complete, created) VALUES (?, ?, ?, ?, ?, ?, ?)";
  
  public static final String COMPLETE = "UPDATE tp_todo_list SET complete=1, completed_on=? WHERE id=?";
  
  public static final String DELETE = "DELETE FROM tp_todo_list WHERE id=?";
  
  public static final String DELETE_USER = "DELETE FROM tp_todo_list WHERE user_id=?";
  
  public static final String DELETE_ALL = "DELETE FROM tp_todo_list";
  
  public static final String COUNT = "SELECT COUNT(id) AS total FROM tp_todo_list WHERE complete=1";
  
  public static final String DELETE_COMPLETE = "DELETE FROM tp_todo_list WHERE complete=1";
  
  public static final String ACTIVE_TODO_USERS = "SELECT DISTINCT user_id FROM tp_todo_list WHERE complete=0";
  
  public static final String ACTIVE_TODOS_COUNT = "SELECT COUNT(id) AS total FROM tp_todo_list WHERE user_id=? AND complete=0";
  
  public static final String FLAG_USER = "INSERT INTO tp_flagged_user (id, user_id, username, num_todos, created) VALUES (?, ?, ?, ?, ?)";
  
  public static final String FLAGGED_USERS = "SELECT * FROM tp_flagged_user";
  
  public static final String FLAGGED_USER = "SELECT * FROM tp_flagged_user WHERE id=?";
  
  public static final String PRUNE_FLAGGED_USER = "DELETE FROM tp_flagged_user WHERE id=?";
  
  public static final String IS_USER_FLAGGED = "SELECT COUNT(id) AS total FROM tp_flagged_user WHERE user_id=?";
}


/* Location:              C:\Users\jkp14\Downloads\TodoPlugin-V3.zip!\lib\TodoPlugin.jar!\com\acme\tod\\util\TodoQuery.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */