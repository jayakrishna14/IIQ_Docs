package com.acme.todo.service;

public class Todo
{
  private String id;
  private String userId;
  private String name;
  private int estimate;
  private String notes;
  private boolean complete;
  private long created;
  private long completedOn;
  
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

  public boolean isComplete() {
    return this.complete;
  }

  public void setComplete(boolean complete) {
    this.complete = complete;
  }

  public long getCreated() {
    return this.created;
  }

  public void setCreated(long created) {
    this.created = created;
  }

  public long getCompletedOn() {
    return this.completedOn;
  }

  public void setCompletedOn(long completedOn) {
    this.completedOn = completedOn;
  }
}