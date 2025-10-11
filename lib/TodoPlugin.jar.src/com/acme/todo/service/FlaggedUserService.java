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
/*     */ public class FlaggedUserService
/*     */ {
/*     */   private PluginContext pluginContext;
/*     */   
/*     */   public static class CreateFlagData
/*     */   {
/*     */     private String id;
/*     */     private String userId;
/*     */     private String username;
/*     */     private int numTodos;
/*     */     
/*     */     public String getId() {
/*  59 */       return this.id;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public void setId(String id) {
/*  68 */       this.id = id;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public String getUserId() {
/*  77 */       return this.userId;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public void setUserId(String userId) {
/*  86 */       this.userId = userId;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public String getUsername() {
/*  95 */       return this.username;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public void setUsername(String username) {
/* 104 */       this.username = username;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public int getNumTodos() {
/* 113 */       return this.numTodos;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public void setNumTodos(int numTodos) {
/* 122 */       this.numTodos = numTodos;
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
/*     */   public FlaggedUserService(PluginContext pluginContext) {
/* 138 */     this.pluginContext = pluginContext;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public List<FlaggedUser> getFlaggedUsers() throws GeneralException {
/* 148 */     Connection connection = null;
/* 149 */     PreparedStatement statement = null;
/*     */     
/*     */     try {
/* 152 */       List<FlaggedUser> flaggedUsers = new ArrayList<>();
/*     */       
/* 154 */       connection = this.pluginContext.getConnection();
/* 155 */       statement = PluginBaseHelper.prepareStatement(connection, "SELECT * FROM tp_flagged_user", new Object[0]);
/*     */       
/* 157 */       ResultSet resultSet = statement.executeQuery();
/* 158 */       while (resultSet.next()) {
/* 159 */         flaggedUsers.add(flaggedUserFromResult(resultSet));
/*     */       }
/*     */       
/* 162 */       return flaggedUsers;
/* 163 */     } catch (SQLException e) {
/* 164 */       throw new GeneralException(e);
/*     */     } finally {
/* 166 */       IOUtil.closeQuietly(statement);
/* 167 */       IOUtil.closeQuietly(connection);
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
/*     */   public FlaggedUser getFlaggedUser(String id) throws GeneralException {
/* 179 */     Connection connection = null;
/* 180 */     PreparedStatement statement = null;
/*     */     
/*     */     try {
/* 183 */       FlaggedUser flaggedUser = null;
/*     */       
/* 185 */       connection = this.pluginContext.getConnection();
/* 186 */       statement = PluginBaseHelper.prepareStatement(connection, "SELECT * FROM tp_flagged_user WHERE id=?", new Object[] { id });
/*     */       
/* 188 */       ResultSet resultSet = statement.executeQuery();
/* 189 */       if (resultSet.next()) {
/* 190 */         flaggedUser = flaggedUserFromResult(resultSet);
/*     */       }
/*     */       
/* 193 */       return flaggedUser;
/* 194 */     } catch (SQLException e) {
/* 195 */       throw new GeneralException(e);
/*     */     } finally {
/* 197 */       IOUtil.closeQuietly(statement);
/* 198 */       IOUtil.closeQuietly(connection);
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
/*     */   public boolean isUserFlagged(String userId) throws GeneralException {
/* 210 */     Connection connection = null;
/* 211 */     PreparedStatement statement = null;
/*     */     
/*     */     try {
/* 214 */       int count = 0;
/*     */       
/* 216 */       connection = this.pluginContext.getConnection();
/* 217 */       statement = PluginBaseHelper.prepareStatement(connection, "SELECT COUNT(id) AS total FROM tp_flagged_user WHERE user_id=?", new Object[] { userId });
/*     */       
/* 219 */       ResultSet resultSet = statement.executeQuery();
/* 220 */       if (resultSet.next()) {
/* 221 */         count = resultSet.getInt("total");
/*     */       }
/*     */       
/* 224 */       return (count > 0);
/* 225 */     } catch (SQLException e) {
/* 226 */       throw new GeneralException(e);
/*     */     } finally {
/* 228 */       IOUtil.closeQuietly(statement);
/* 229 */       IOUtil.closeQuietly(connection);
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
/*     */   public void pruneFlaggedUser(FlaggedUser flaggedUser) throws GeneralException {
/* 241 */     Connection connection = null;
/* 242 */     PreparedStatement statement = null;
/*     */     
/*     */     try {
/* 245 */       connection = this.pluginContext.getConnection();
/*     */       
/* 247 */       statement = PluginBaseHelper.prepareStatement(connection, "DELETE FROM tp_flagged_user WHERE id=?", new Object[] { flaggedUser.getId() });
/* 248 */       statement.executeUpdate();
/* 249 */     } catch (SQLException e) {
/* 250 */       throw new GeneralException(e);
/*     */     } finally {
/* 252 */       IOUtil.closeQuietly(statement);
/* 253 */       IOUtil.closeQuietly(connection);
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
/*     */   public FlaggedUser flagUser(CreateFlagData data) throws GeneralException {
/* 265 */     Connection connection = null;
/* 266 */     PreparedStatement statement = null;
/*     */     
/*     */     try {
/* 269 */       connection = this.pluginContext.getConnection();
/*     */       
/* 271 */       String id = data.getId();
/* 272 */       String userId = data.getUserId();
/* 273 */       String username = data.getUsername();
/* 274 */       int numTodos = data.getNumTodos();
/*     */       
/* 276 */       statement = PluginBaseHelper.prepareStatement(connection, "INSERT INTO tp_flagged_user (id, user_id, username, num_todos, created) VALUES (?, ?, ?, ?, ?)", new Object[] { id, userId, username, 
/* 277 */             Integer.valueOf(numTodos), Long.valueOf(TodoUtil.now()) });
/*     */ 
/*     */       
/* 280 */       statement.executeUpdate();
/* 281 */     } catch (SQLException e) {
/* 282 */       throw new GeneralException(e);
/*     */     } finally {
/* 284 */       IOUtil.closeQuietly(statement);
/* 285 */       IOUtil.closeQuietly(connection);
/*     */     } 
/*     */     
/* 288 */     return getFlaggedUser(data.getId());
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private FlaggedUser flaggedUserFromResult(ResultSet resultSet) throws SQLException {
/* 299 */     FlaggedUser flaggedUser = new FlaggedUser();
/* 300 */     flaggedUser.setId(resultSet.getString("id"));
/* 301 */     flaggedUser.setUserId(resultSet.getString("user_id"));
/* 302 */     flaggedUser.setUsername(resultSet.getString("username"));
/* 303 */     flaggedUser.setNumTodos(resultSet.getInt("num_todos"));
/* 304 */     flaggedUser.setCreated(resultSet.getLong("created"));
/*     */     
/* 306 */     return flaggedUser;
/*     */   }
/*     */ }


/* Location:              C:\Users\jkp14\Downloads\TodoPlugin-V3.zip!\lib\TodoPlugin.jar!\com\acme\todo\service\FlaggedUserService.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */