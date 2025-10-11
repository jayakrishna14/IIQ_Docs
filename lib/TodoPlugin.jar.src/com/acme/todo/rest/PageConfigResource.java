/*     */ package com.acme.todo.rest;
/*     */ 
/*     */ import javax.ws.rs.Consumes;
/*     */ import javax.ws.rs.GET;
/*     */ import javax.ws.rs.Path;
/*     */ import javax.ws.rs.Produces;
/*     */ import sailpoint.object.Identity;
/*     */ import sailpoint.rest.plugin.AllowAll;
/*     */ import sailpoint.rest.plugin.BasePluginResource;
/*     */ import sailpoint.tools.GeneralException;
/*     */ import sailpoint.tools.Message;
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
/*     */ @Path("TodoPlugin")
/*     */ @Produces({"application/json"})
/*     */ @Consumes({"application/json"})
/*     */ public class PageConfigResource
/*     */   extends BasePluginResource
/*     */ {
/*     */   private static final String RIGHT_VIEW_FLAGGED_USERS = "ViewFlaggedUsers";
/*     */   private static final String SETTING_CAN_DELETE = "canDelete";
/*     */   
/*     */   private static class PageConfig
/*     */   {
/*     */     private boolean showFlagged;
/*     */     private boolean showClear;
/*     */     
/*     */     private PageConfig() {}
/*     */     
/*     */     public boolean isShowFlagged() {
/*  61 */       return this.showFlagged;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public void setShowFlagged(boolean showFlagged) {
/*  70 */       this.showFlagged = showFlagged;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public boolean isShowClear() {
/*  79 */       return this.showClear;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     public void setShowClear(boolean showClear) {
/*  88 */       this.showClear = showClear;
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public String getPluginName() {
/*  98 */     return "TodoPlugin";
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   @GET
/*     */   @Path("pageConfig")
/*     */   @AllowAll
/*     */   public PageConfig getPageConfig() throws GeneralException {
/* 111 */     Identity identity = getLoggedInUser();
/* 112 */     if (identity == null) {
/* 113 */       throw new GeneralException(Message.localize("TodoPlugin_unable_load_identity"));
/*     */     }
/*     */     
/* 116 */     Identity.CapabilityManager capabilityManager = identity.getCapabilityManager();
/*     */     
/* 118 */     boolean showFlagged = (capabilityManager.hasCapability("SystemAdministrator") || capabilityManager.hasRight("ViewFlaggedUsers"));
/*     */     
/* 120 */     PageConfig pageConfig = new PageConfig();
/* 121 */     pageConfig.setShowFlagged(showFlagged);
/* 122 */     pageConfig.setShowClear(isTodoRemovable());
/*     */     
/* 124 */     return pageConfig;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private boolean isTodoRemovable() {
/* 134 */     return getSettingBool("canDelete");
/*     */   }
/*     */ }


/* Location:              C:\Users\jkp14\Downloads\TodoPlugin-V3.zip!\lib\TodoPlugin.jar!\com\acme\todo\rest\PageConfigResource.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */