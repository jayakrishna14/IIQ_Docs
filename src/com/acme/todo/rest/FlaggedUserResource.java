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
/* 36 */     return "TodoPlugin";
   }
 
 
 
 
 
 
   
   @GET
   @Path("flaggedUsers")
   public ListResult getFlaggedUsers() throws GeneralException {
/* 48 */     List<FlaggedUser> flaggedUsers = getFlaggedUserService().getFlaggedUsers();
     
/* 50 */     return new ListResult(flaggedUsers, flaggedUsers.size());
   }
 
 
 
 
 
   
   private FlaggedUserService getFlaggedUserService() {
/* 59 */     return new FlaggedUserService((PluginContext)this);
   }
 }


/* Location:              C:\Users\jkp14\Downloads\TodoPlugin-V3.zip!\lib\TodoPlugin.jar!\com\acme\todo\rest\FlaggedUserResource.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */