 package com.acme.todo.rest;
 
 import com.acme.todo.service.FlaggedUser;
 import com.acme.todo.service.FlaggedUserService;
 import java.util.List;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import sailpoint.integration.ListResult;
 import sailpoint.plugin.PluginContext;
 import sailpoint.rest.plugin.BasePluginResource;
 import sailpoint.rest.plugin.RequiredRight;
 import sailpoint.tools.GeneralException;
 
 @Path("TodoPlugin")
 @Produces({"application/json"})
 @Consumes({"application/json"})
 @RequiredRight("ViewFlaggedUsers")
 public class FlaggedUserResource
   extends BasePluginResource
 {
   public String getPluginName() {
     return "TodoPlugin";
   }
   
   @GET
   @Path("flaggedUsers")
   public ListResult getFlaggedUsers() throws GeneralException {
     List<FlaggedUser> flaggedUsers = getFlaggedUserService().getFlaggedUsers();
     
     return new ListResult(flaggedUsers, flaggedUsers.size());
   }
  
   private FlaggedUserService getFlaggedUserService() {
     return new FlaggedUserService((PluginContext)this);
   }
 }
