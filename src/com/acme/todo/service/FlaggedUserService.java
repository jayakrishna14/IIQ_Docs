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

public class FlaggedUserService
{
  private PluginContext pluginContext;
  
  public static class CreateFlagData
  {
    private String id;
    private String userId;
    private String username;
    private int numTodos;
    
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
 
    public String getUsername() {
      return this.username;
    }
 
    public void setUsername(String username) {
      this.username = username;
    }
 
    public int getNumTodos() {
      return this.numTodos;
    }
 
    public void setNumTodos(int numTodos) {
      this.numTodos = numTodos;
    }
  }

  public FlaggedUserService(PluginContext pluginContext) {
    this.pluginContext = pluginContext;
  }

  public List<FlaggedUser> getFlaggedUsers() throws GeneralException {
    Connection connection = null;
    PreparedStatement statement = null;
    
    try {
      List<FlaggedUser> flaggedUsers = new ArrayList<>();
      
      connection = this.pluginContext.getConnection();
      statement = PluginBaseHelper.prepareStatement(connection, "SELECT * FROM tp_flagged_user", new Object[0]);
      
      ResultSet resultSet = statement.executeQuery();
      while (resultSet.next()) {
        flaggedUsers.add(flaggedUserFromResult(resultSet));
      }
      
      return flaggedUsers;
    } catch (SQLException e) {
      throw new GeneralException(e);
    } finally {
      IOUtil.closeQuietly(statement);
      IOUtil.closeQuietly(connection);
    } 
  }

  public FlaggedUser getFlaggedUser(String id) throws GeneralException {
    Connection connection = null;
    PreparedStatement statement = null;
    
    try {
      FlaggedUser flaggedUser = null;
      
      connection = this.pluginContext.getConnection();
      statement = PluginBaseHelper.prepareStatement(connection, "SELECT * FROM tp_flagged_user WHERE id=?", new Object[] { id });
      
      ResultSet resultSet = statement.executeQuery();
      if (resultSet.next()) {
        flaggedUser = flaggedUserFromResult(resultSet);
      }
      
      return flaggedUser;
    } catch (SQLException e) {
      throw new GeneralException(e);
    } finally {
      IOUtil.closeQuietly(statement);
      IOUtil.closeQuietly(connection);
    } 
  }

  public boolean isUserFlagged(String userId) throws GeneralException {
    Connection connection = null;
    PreparedStatement statement = null;
    
    try {
      int count = 0;
      
      connection = this.pluginContext.getConnection();
      statement = PluginBaseHelper.prepareStatement(connection, "SELECT COUNT(id) AS total FROM tp_flagged_user WHERE user_id=?", new Object[] { userId });
      
      ResultSet resultSet = statement.executeQuery();
      if (resultSet.next()) {
        count = resultSet.getInt("total");
      }
      
      return (count > 0);
    } catch (SQLException e) {
      throw new GeneralException(e);
    } finally {
      IOUtil.closeQuietly(statement);
      IOUtil.closeQuietly(connection);
    } 
  }

  public void pruneFlaggedUser(FlaggedUser flaggedUser) throws GeneralException {
    Connection connection = null;
    PreparedStatement statement = null;
    
    try {
      connection = this.pluginContext.getConnection();
      
      statement = PluginBaseHelper.prepareStatement(connection, "DELETE FROM tp_flagged_user WHERE id=?", new Object[] { flaggedUser.getId() });
      statement.executeUpdate();
    } catch (SQLException e) {
      throw new GeneralException(e);
    } finally {
      IOUtil.closeQuietly(statement);
      IOUtil.closeQuietly(connection);
    } 
  }

  public FlaggedUser flagUser(CreateFlagData data) throws GeneralException {
    Connection connection = null;
    PreparedStatement statement = null;
    
    try {
      connection = this.pluginContext.getConnection();
      
      String id = data.getId();
      String userId = data.getUserId();
      String username = data.getUsername();
      int numTodos = data.getNumTodos();
      
      statement = PluginBaseHelper.prepareStatement(connection, "INSERT INTO tp_flagged_user (id, user_id, username, num_todos, created) VALUES (?, ?, ?, ?, ?)", new Object[] { id, userId, username, 
            Integer.valueOf(numTodos), Long.valueOf(TodoUtil.now()) });

      
      statement.executeUpdate();
    } catch (SQLException e) {
      throw new GeneralException(e);
    } finally {
      IOUtil.closeQuietly(statement);
      IOUtil.closeQuietly(connection);
    } 
    
    return getFlaggedUser(data.getId());
  }

  private FlaggedUser flaggedUserFromResult(ResultSet resultSet) throws SQLException {
    FlaggedUser flaggedUser = new FlaggedUser();
    flaggedUser.setId(resultSet.getString("id"));
    flaggedUser.setUserId(resultSet.getString("user_id"));
    flaggedUser.setUsername(resultSet.getString("username"));
    flaggedUser.setNumTodos(resultSet.getInt("num_todos"));
    flaggedUser.setCreated(resultSet.getLong("created"));
    
    return flaggedUser;
  }
}