package com.acme.todo.service;

public class FlaggedUser
{
  private String id;
  private String userId;
  private String username;
  private int numTodos;
  private long created;
  
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

  public long getCreated() {
    return this.created;
  }

  public void setCreated(long created) {
    this.created = created;
  }
}