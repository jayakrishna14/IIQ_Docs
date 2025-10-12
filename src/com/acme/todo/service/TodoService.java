package com.acme.todo.service;

import com.acme.todo.util.TodoUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import sailpoint.plugin.PluginBaseHelper;
import sailpoint.plugin.PluginContext;
import sailpoint.tools.GeneralException;
import sailpoint.tools.IOUtil;
import sailpoint.tools.ObjectNotFoundException;
import sailpoint.tools.Util;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TodoService
{
  private static final Log LOG = LogFactory.getLog(TodoService.class);
  private static final String SETTING_DEFAULT_NAME = "defaultName";
  private static final String SETTING_DEFAULT_TIME = "defaultTime";
  private PluginContext pluginContext;
  
  public static class CreateTodoData{
    private String id;
    private String userId;
    private String name;
    private int estimate;
    private String notes;
    
    public String getId() {
      return this.id;
    }
  
    public void setId(String id) {
      this.id = id;
    }
  
    public String getUserId() {
      return this.userId;
    }
  
    public void setUserId(String userId) {
      this.userId = userId;
    }
  
    public String getName() {
      return this.name;
    }
  
    public void setName(String name) {
      this.name = name;
    }
  
    public int getEstimate() {
      return this.estimate;
    }
  
    public void setEstimate(int estimate) {
      this.estimate = estimate;
    }
  
    public String getNotes() {
      return this.notes;
    }
  
    public void setNotes(String notes) {
      this.notes = notes;
    }
  }

  public TodoService(PluginContext pluginContext) {
    this.pluginContext = pluginContext;
  }

  public List<Todo> getTodosForUser(String userId) throws GeneralException {
    Connection connection = null;
    PreparedStatement statement = null;
    
    try {
      LOG.info("getTodosForUser: userId=" + userId);
      connection = this.pluginContext.getConnection();
      
      statement = PluginBaseHelper.prepareStatement(connection, "SELECT * FROM tp_todo_list WHERE user_id=? ORDER BY completed_on ASC, created ASC", new Object[] { userId });
      ResultSet resultSet = statement.executeQuery();
      
      List<Todo> todos = new ArrayList<>();
      while (resultSet.next()) {
        todos.add(todoFromResult(resultSet));
      }
      
      LOG.info("getTodosForUser: returning " + todos.size() + " todos for userId=" + userId);
      return todos;
    } catch (SQLException e) {
      throw new GeneralException(e);
    } finally {
      IOUtil.closeQuietly(statement);
      IOUtil.closeQuietly(connection);
    } 
  }

  public Todo getTodo(String todoId) throws GeneralException {
    Connection connection = null;
    PreparedStatement statement = null;
    
    try {
      connection = this.pluginContext.getConnection();
      
      statement = PluginBaseHelper.prepareStatement(connection, "SELECT * FROM tp_todo_list WHERE id=?", new Object[] { todoId });
      
      ResultSet resultSet = statement.executeQuery();
      if (resultSet.next()) {
        return todoFromResult(resultSet);
      }
      throw new ObjectNotFoundException();
    }
    catch (SQLException e) {
      throw new GeneralException(e);
    } finally {
      IOUtil.closeQuietly(statement);
      IOUtil.closeQuietly(connection);
    } 
  }

  public Todo createTodo(CreateTodoData data) throws GeneralException {
    Connection connection = null;
    PreparedStatement statement = null;
    
    try {
      LOG.info("createTodo: userId=" + data.getUserId() + " name=" + data.getName());
      connection = this.pluginContext.getConnection();
      
      String name = data.getName();
      if (Util.isNullOrEmpty(name)) {
        name = getTodoDefaultName();
      }
      
      int estimate = data.getEstimate();
      if (estimate <= 0) {
        estimate = getTodoDefaultEstimate();
      }
      
      statement = PluginBaseHelper.prepareStatement(connection, "INSERT INTO tp_todo_list (id, user_id, name, estimate, notes, complete, created) VALUES (?, ?, ?, ?, ?, ?, ?)", new Object[] { data
            .getId(), data.getUserId(), name, 
            Integer.valueOf(estimate), data.getNotes(), Boolean.valueOf(false), Long.valueOf(TodoUtil.now()) });

      
      statement.executeUpdate();
    } catch (SQLException e) {
      throw new GeneralException(e);
    } finally {
      IOUtil.closeQuietly(statement);
      IOUtil.closeQuietly(connection);
    } 
    
    return getTodo(data.getId());
  }

  public void completeTodo(Todo todo) throws GeneralException {
    Connection connection = null;
    PreparedStatement statement = null;
    
    try {
      LOG.info("completeTodo: id=" + (todo == null ? "null" : todo.getId()));
      connection = this.pluginContext.getConnection();
      
      statement = PluginBaseHelper.prepareStatement(connection, "UPDATE tp_todo_list SET complete=1, completed_on=? WHERE id=?", new Object[] { Long.valueOf(TodoUtil.now()), todo.getId() });
      statement.executeUpdate();
    } catch (SQLException e) {
      throw new GeneralException(e);
    } finally {
      IOUtil.closeQuietly(statement);
      IOUtil.closeQuietly(connection);
    } 
  }

  public void deleteTodo(Todo todo) throws GeneralException {
    Connection connection = null;
    PreparedStatement statement = null;
    
    try {
      connection = this.pluginContext.getConnection();
      
      statement = PluginBaseHelper.prepareStatement(connection, "DELETE FROM tp_todo_list WHERE id=?", new Object[] { todo.getId() });
      statement.executeUpdate();
    } catch (SQLException e) {
      throw new GeneralException(e);
    } finally {
      IOUtil.closeQuietly(statement);
      IOUtil.closeQuietly(connection);
    } 
  }

  public void deleteUserTodos(String userId) throws GeneralException {
    Connection connection = null;
    PreparedStatement statement = null;
    
    try {
      LOG.info("deleteUserTodos: userId=" + userId);
      connection = this.pluginContext.getConnection();
      
      statement = PluginBaseHelper.prepareStatement(connection, "DELETE FROM tp_todo_list WHERE user_id=?", new Object[] { userId });
      statement.executeUpdate();
    } catch (SQLException e) {
      throw new GeneralException(e);
    } finally {
      IOUtil.closeQuietly(statement);
      IOUtil.closeQuietly(connection);
    } 
  }

  public void deleteAllTodos() throws GeneralException {
    Connection connection = null;
    PreparedStatement statement = null;
    
    try {
      connection = this.pluginContext.getConnection();
      
      statement = PluginBaseHelper.prepareStatement(connection, "DELETE FROM tp_todo_list", new Object[0]);
      statement.executeUpdate();
    } catch (SQLException e) {
      throw new GeneralException(e);
    } finally {
      IOUtil.closeQuietly(statement);
      IOUtil.closeQuietly(connection);
    } 
  }

  public int deleteCompletedTodos() throws GeneralException {
    Connection connection = null;
    
    try {
      LOG.info("deleteCompletedTodos: starting");
      connection = this.pluginContext.getConnection();
      
      int numComplete = countCompletedTodos(connection);
      if (numComplete > 0) {
        deleteCompletedTodos(connection);
      }
      LOG.info("deleteCompletedTodos: deleted=" + numComplete);
      
      return numComplete;
    } catch (SQLException e) {
      throw new GeneralException(e);
    } finally {
      IOUtil.closeQuietly(connection);
    } 
  }

  public List<String> getUsersWithOpenTodos() throws GeneralException {
    Connection connection = null;
    PreparedStatement statement = null;
    
    try {
      LOG.info("getUsersWithOpenTodos: querying distinct user ids with open todos");
      List<String> userIds = new ArrayList<>();
      
      connection = this.pluginContext.getConnection();
      statement = PluginBaseHelper.prepareStatement(connection, "SELECT DISTINCT user_id FROM tp_todo_list WHERE complete=0", new Object[0]);
      
      ResultSet resultSet = statement.executeQuery();
      while (resultSet.next()) {
        userIds.add(resultSet.getString("user_id"));
      }
      LOG.info("getUsersWithOpenTodos: returning " + userIds.size() + " user ids");
      return userIds;
    } catch (SQLException e) {
      throw new GeneralException(e);
    } finally {
      IOUtil.closeQuietly(statement);
      IOUtil.closeQuietly(connection);
    } 
  }
  
  public int getActiveTodosForUser(String userId) throws GeneralException {
    Connection connection = null;
    PreparedStatement statement = null;
    
    try {
      LOG.info("getActiveTodosForUser: userId=" + userId);
      int numActive = 0;
      
      connection = this.pluginContext.getConnection();
      statement = PluginBaseHelper.prepareStatement(connection, "SELECT COUNT(id) AS total FROM tp_todo_list WHERE user_id=? AND complete=0", new Object[] { userId });
      
      ResultSet resultSet = statement.executeQuery();
      if (resultSet.next()) {
        numActive = resultSet.getInt("total");
      }
      LOG.info("getActiveTodosForUser: userId=" + userId + " active=" + numActive);
      return numActive;
    } catch (SQLException e) {
      throw new GeneralException(e);
    } finally {
      IOUtil.closeQuietly(statement);
      IOUtil.closeQuietly(connection);
    } 
  }
  
  private int countCompletedTodos(Connection connection) throws SQLException {
    PreparedStatement statement = null;
    
    try {
      int count = 0;
      statement = PluginBaseHelper.prepareStatement(connection, "SELECT COUNT(id) AS total FROM tp_todo_list WHERE complete=1", new Object[0]);
      
      ResultSet rs = statement.executeQuery();
      if (rs.next()) {
        count = rs.getInt("total");
      }
      
      return count;
    } finally {
      IOUtil.closeQuietly(statement);
    } 
  }

  private void deleteCompletedTodos(Connection connection) throws SQLException {
    PreparedStatement statement = null;
    
    try {
      statement = PluginBaseHelper.prepareStatement(connection, "DELETE FROM tp_todo_list WHERE complete=1", new Object[0]);
      statement.executeUpdate();
    } finally {
      IOUtil.closeQuietly(statement);
    } 
  }
  
  private Todo todoFromResult(ResultSet resultSet) throws SQLException {
    Todo todo = new Todo();
    todo.setId(resultSet.getString("id"));
    todo.setUserId(resultSet.getString("user_id"));
    todo.setName(resultSet.getString("name"));
    todo.setEstimate(resultSet.getInt("estimate"));
    todo.setNotes(resultSet.getString("notes"));
    todo.setComplete(resultSet.getBoolean("complete"));
    todo.setCreated(resultSet.getLong("created"));
    todo.setCompletedOn(resultSet.getLong("completed_on"));
    
    return todo;
  }

  private String getTodoDefaultName() {
    return this.pluginContext.getSettingString(SETTING_DEFAULT_NAME);
  }

  private int getTodoDefaultEstimate() {
    return this.pluginContext.getSettingInt(SETTING_DEFAULT_TIME);
  }
}