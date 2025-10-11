package com.acme.todo.util;

import java.util.Date;

public class TodoUtil {
  public static final String PLUGIN_NAME = "TodoPlugin";
  
  public static long now() {
    return (new Date()).getTime();
  }
}