/*     */ package com.acme.todo.service;
/*     */ 
/*     */ import com.acme.todo.util.TodoUtil;
/*     */ import java.sql.Connection;
/*     */ import java.sql.PreparedStatement;
/*     */ import java.sql.ResultSet;
/*     */ import java.sql.SQLException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import sailpoint.plugin.PluginBaseHelper;
/*     */ import sailpoint.plugin.PluginContext;
/*     */ import sailpoint.tools.GeneralException;
/*     */ import sailpoint.tools.IOUtil;
/*     */ import sailpoint.tools.ObjectNotFoundException;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class TodoService
/*     */ {
/*     */   private static final String SETTING_DEFAULT_NAME = "defaultName";
/*     */   private static final String SETTING_DEFAULT_TIME = "defaultTime";
/*     */   private PluginContext pluginContext;
/*     */   
/*     */   public static class CreateTodoData
/*     */   {
/*     */     private String id;
/*     */     private String userId;
/*     */     private String name;
/*     */     private int estimate;
/*     */     private String notes;
/*     */     
/*     */     public String getId() {
/*  76 */       return this.id;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public void setId(String id) {
/*  85 */       this.id = id;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public String getUserId() {
/*  94 */       return this.userId;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public void setUserId(String userId) {
/* 103 */       this.userId = userId;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public String getName() {
/* 112 */       return this.name;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public void setName(String name) {
/* 121 */       this.name = name;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public int getEstimate() {
/* 130 */       return this.estimate;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public void setEstimate(int estimate) {
/* 139 */       this.estimate = estimate;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public String getNotes() {
/* 148 */       return this.notes;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public void setNotes(String notes) {
/* 157 */       this.notes = notes;
/*     */     }
/*     */   }
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
/*     */   public TodoService(PluginContext pluginContext) {
/* 173 */     this.pluginContext = pluginContext;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public List<Todo> getTodosForUser(String userId) throws GeneralException {
/* 184 */     Connection connection = null;
/* 185 */     PreparedStatement statement = null;
/*     */     
/*     */     try {
/* 188 */       connection = this.pluginContext.getConnection();
/*     */       
/* 190 */       statement = PluginBaseHelper.prepareStatement(connection, "SELECT * FROM tp_todo_list WHERE user_id=? ORDER BY completed_on ASC, created ASC", new Object[] { userId });
/* 191 */       ResultSet resultSet = statement.executeQuery();
/*     */       
/* 193 */       List<Todo> todos = new ArrayList<>();
/* 194 */       while (resultSet.next()) {
/* 195 */         todos.add(todoFromResult(resultSet));
/*     */       }
/*     */       
/* 198 */       return todos;
/* 199 */     } catch (SQLException e) {
/* 200 */       throw new GeneralException(e);
/*     */     } finally {
/* 202 */       IOUtil.closeQuietly(statement);
/* 203 */       IOUtil.closeQuietly(connection);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Todo getTodo(String todoId) throws GeneralException {
/* 215 */     Connection connection = null;
/* 216 */     PreparedStatement statement = null;
/*     */     
/*     */     try {
/* 219 */       connection = this.pluginContext.getConnection();
/*     */       
/* 221 */       statement = PluginBaseHelper.prepareStatement(connection, "SELECT * FROM tp_todo_list WHERE id=?", new Object[] { todoId });
/*     */       
/* 223 */       ResultSet resultSet = statement.executeQuery();
/* 224 */       if (resultSet.next()) {
/* 225 */         return todoFromResult(resultSet);
/*     */       }
/* 227 */       throw new ObjectNotFoundException();
/*     */     }
/* 229 */     catch (SQLException e) {
/* 230 */       throw new GeneralException(e);
/*     */     } finally {
/* 232 */       IOUtil.closeQuietly(statement);
/* 233 */       IOUtil.closeQuietly(connection);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Todo createTodo(CreateTodoData data) throws GeneralException {
/* 245 */     Connection connection = null;
/* 246 */     PreparedStatement statement = null;
/*     */     
/*     */     try {
/* 249 */       connection = this.pluginContext.getConnection();
/*     */       
/* 251 */       String name = data.getName();
/* 252 */       if (Util.isNullOrEmpty(name)) {
/* 253 */         name = getTodoDefaultName();
/*     */       }
/*     */       
/* 256 */       int estimate = data.getEstimate();
/* 257 */       if (estimate <= 0) {
/* 258 */         estimate = getTodoDefaultEstimate();
/*     */       }
/*     */       
/* 261 */       statement = PluginBaseHelper.prepareStatement(connection, "INSERT INTO tp_todo_list (id, user_id, name, estimate, notes, complete, created) VALUES (?, ?, ?, ?, ?, ?, ?)", new Object[] { data
/* 262 */             .getId(), data.getUserId(), name, 
/* 263 */             Integer.valueOf(estimate), data.getNotes(), Boolean.valueOf(false), Long.valueOf(TodoUtil.now()) });
/*     */ 
/*     */       
/* 266 */       statement.executeUpdate();
/* 267 */     } catch (SQLException e) {
/* 268 */       throw new GeneralException(e);
/*     */     } finally {
/* 270 */       IOUtil.closeQuietly(statement);
/* 271 */       IOUtil.closeQuietly(connection);
/*     */     } 
/*     */     
/* 274 */     return getTodo(data.getId());
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void completeTodo(Todo todo) throws GeneralException {
/* 284 */     Connection connection = null;
/* 285 */     PreparedStatement statement = null;
/*     */     
/*     */     try {
/* 288 */       connection = this.pluginContext.getConnection();
/*     */       
/* 290 */       statement = PluginBaseHelper.prepareStatement(connection, "UPDATE tp_todo_list SET complete=1, completed_on=? WHERE id=?", new Object[] { Long.valueOf(TodoUtil.now()), todo.getId() });
/* 291 */       statement.executeUpdate();
/* 292 */     } catch (SQLException e) {
/* 293 */       throw new GeneralException(e);
/*     */     } finally {
/* 295 */       IOUtil.closeQuietly(statement);
/* 296 */       IOUtil.closeQuietly(connection);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void deleteTodo(Todo todo) throws GeneralException {
/* 307 */     Connection connection = null;
/* 308 */     PreparedStatement statement = null;
/*     */     
/*     */     try {
/* 311 */       connection = this.pluginContext.getConnection();
/*     */       
/* 313 */       statement = PluginBaseHelper.prepareStatement(connection, "DELETE FROM tp_todo_list WHERE id=?", new Object[] { todo.getId() });
/* 314 */       statement.executeUpdate();
/* 315 */     } catch (SQLException e) {
/* 316 */       throw new GeneralException(e);
/*     */     } finally {
/* 318 */       IOUtil.closeQuietly(statement);
/* 319 */       IOUtil.closeQuietly(connection);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void deleteUserTodos(String userId) throws GeneralException {
/* 330 */     Connection connection = null;
/* 331 */     PreparedStatement statement = null;
/*     */     
/*     */     try {
/* 334 */       connection = this.pluginContext.getConnection();
/*     */       
/* 336 */       statement = PluginBaseHelper.prepareStatement(connection, "DELETE FROM tp_todo_list WHERE user_id=?", new Object[] { userId });
/* 337 */       statement.executeUpdate();
/* 338 */     } catch (SQLException e) {
/* 339 */       throw new GeneralException(e);
/*     */     } finally {
/* 341 */       IOUtil.closeQuietly(statement);
/* 342 */       IOUtil.closeQuietly(connection);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void deleteAllTodos() throws GeneralException {
/* 352 */     Connection connection = null;
/* 353 */     PreparedStatement statement = null;
/*     */     
/*     */     try {
/* 356 */       connection = this.pluginContext.getConnection();
/*     */       
/* 358 */       statement = PluginBaseHelper.prepareStatement(connection, "DELETE FROM tp_todo_list", new Object[0]);
/* 359 */       statement.executeUpdate();
/* 360 */     } catch (SQLException e) {
/* 361 */       throw new GeneralException(e);
/*     */     } finally {
/* 363 */       IOUtil.closeQuietly(statement);
/* 364 */       IOUtil.closeQuietly(connection);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public int deleteCompletedTodos() throws GeneralException {
/* 375 */     Connection connection = null;
/*     */     
/*     */     try {
/* 378 */       connection = this.pluginContext.getConnection();
/*     */       
/* 380 */       int numComplete = countCompletedTodos(connection);
/* 381 */       if (numComplete > 0) {
/* 382 */         deleteCompletedTodos(connection);
/*     */       }
/*     */       
/* 385 */       return numComplete;
/* 386 */     } catch (SQLException e) {
/* 387 */       throw new GeneralException(e);
/*     */     } finally {
/* 389 */       IOUtil.closeQuietly(connection);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public List<String> getUsersWithOpenTodos() throws GeneralException {
/* 400 */     Connection connection = null;
/* 401 */     PreparedStatement statement = null;
/*     */     
/*     */     try {
/* 404 */       List<String> userIds = new ArrayList<>();
/*     */       
/* 406 */       connection = this.pluginContext.getConnection();
/* 407 */       statement = PluginBaseHelper.prepareStatement(connection, "SELECT DISTINCT user_id FROM tp_todo_list WHERE complete=0", new Object[0]);
/*     */       
/* 409 */       ResultSet resultSet = statement.executeQuery();
/* 410 */       while (resultSet.next()) {
/* 411 */         userIds.add(resultSet.getString("user_id"));
/*     */       }
/*     */       
/* 414 */       return userIds;
/* 415 */     } catch (SQLException e) {
/* 416 */       throw new GeneralException(e);
/*     */     } finally {
/* 418 */       IOUtil.closeQuietly(statement);
/* 419 */       IOUtil.closeQuietly(connection);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public int getActiveTodosForUser(String userId) throws GeneralException {
/* 431 */     Connection connection = null;
/* 432 */     PreparedStatement statement = null;
/*     */     
/*     */     try {
/* 435 */       int numActive = 0;
/*     */       
/* 437 */       connection = this.pluginContext.getConnection();
/* 438 */       statement = PluginBaseHelper.prepareStatement(connection, "SELECT COUNT(id) AS total FROM tp_todo_list WHERE user_id=? AND complete=0", new Object[] { userId });
/*     */       
/* 440 */       ResultSet resultSet = statement.executeQuery();
/* 441 */       if (resultSet.next()) {
/* 442 */         numActive = resultSet.getInt("total");
/*     */       }
/*     */       
/* 445 */       return numActive;
/* 446 */     } catch (SQLException e) {
/* 447 */       throw new GeneralException(e);
/*     */     } finally {
/* 449 */       IOUtil.closeQuietly(statement);
/* 450 */       IOUtil.closeQuietly(connection);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private int countCompletedTodos(Connection connection) throws SQLException {
/* 462 */     PreparedStatement statement = null;
/*     */     
/*     */     try {
/* 465 */       int count = 0;
/* 466 */       statement = PluginBaseHelper.prepareStatement(connection, "SELECT COUNT(id) AS total FROM tp_todo_list WHERE complete=1", new Object[0]);
/*     */       
/* 468 */       ResultSet rs = statement.executeQuery();
/* 469 */       if (rs.next()) {
/* 470 */         count = rs.getInt("total");
/*     */       }
/*     */       
/* 473 */       return count;
/*     */     } finally {
/* 475 */       IOUtil.closeQuietly(statement);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void deleteCompletedTodos(Connection connection) throws SQLException {
/* 486 */     PreparedStatement statement = null;
/*     */     
/*     */     try {
/* 489 */       statement = PluginBaseHelper.prepareStatement(connection, "DELETE FROM tp_todo_list WHERE complete=1", new Object[0]);
/* 490 */       statement.executeUpdate();
/*     */     } finally {
/* 492 */       IOUtil.closeQuietly(statement);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private Todo todoFromResult(ResultSet resultSet) throws SQLException {
/* 504 */     Todo todo = new Todo();
/* 505 */     todo.setId(resultSet.getString("id"));
/* 506 */     todo.setUserId(resultSet.getString("user_id"));
/* 507 */     todo.setName(resultSet.getString("name"));
/* 508 */     todo.setEstimate(resultSet.getInt("estimate"));
/* 509 */     todo.setNotes(resultSet.getString("notes"));
/* 510 */     todo.setComplete(resultSet.getBoolean("complete"));
/* 511 */     todo.setCreated(resultSet.getLong("created"));
/* 512 */     todo.setCompletedOn(resultSet.getLong("completed_on"));
/*     */     
/* 514 */     return todo;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private String getTodoDefaultName() {
/* 523 */     return this.pluginContext.getSettingString("defaultName");
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private int getTodoDefaultEstimate() {
/* 532 */     return this.pluginContext.getSettingInt("defaultTime");
/*     */   }
/*     */ }


/* Location:              C:\Users\jkp14\Downloads\TodoPlugin-V3.zip!\lib\TodoPlugin.jar!\com\acme\todo\service\TodoService.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */