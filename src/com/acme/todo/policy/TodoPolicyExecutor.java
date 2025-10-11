/*     */ package com.acme.todo.policy;
/*     */ 
/*     */ import com.acme.todo.service.TodoService;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import sailpoint.api.SailPointContext;
/*     */ import sailpoint.object.Identity;
/*     */ import sailpoint.object.Policy;
/*     */ import sailpoint.object.PolicyViolation;
/*     */ import sailpoint.plugin.PluginContext;
/*     */ import sailpoint.policy.BasePluginPolicyExecutor;
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
/*     */ 
/*     */ public class TodoPolicyExecutor
/*     */   extends BasePluginPolicyExecutor
/*     */ {
/*     */   private static final String ARG_MAX_ACTIVE_TODOS = "maxActiveTodos";
/*     */   private static final String ARG_NUM_ACTIVE = "numActive";
/*     */   private static final String RENDERER = "ui/policy/renderer.xhtml";
/*     */   
/*     */   public String getPluginName() {
/*  55 */     return "TodoPlugin";
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public List<PolicyViolation> evaluate(SailPointContext context, Policy policy, Identity id) throws GeneralException {
/*  65 */     List<PolicyViolation> violations = new ArrayList<>();
/*     */ 
/*     */     
/*  68 */     int maxActiveTodos = policy.getInt("maxActiveTodos");
/*  69 */     if (maxActiveTodos <= 0) {
/*  70 */       return violations;
/*     */     }
/*     */     
/*  73 */     TodoService todoService = new TodoService((PluginContext)this);
/*     */     
/*  75 */     int numActive = todoService.getActiveTodosForUser(id.getId());
/*  76 */     if (numActive > maxActiveTodos) {
/*  77 */       violations.add(createViolation(context, policy, id, numActive));
/*     */     }
/*     */     
/*  80 */     return violations;
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
/*     */   private PolicyViolation createViolation(SailPointContext context, Policy policy, Identity identity, int numActive) {
/*  94 */     PolicyViolation violation = new PolicyViolation();
/*  95 */     violation.setStatus(PolicyViolation.Status.Open);
/*  96 */     violation.setIdentity(identity);
/*  97 */     violation.setPolicy(policy);
/*  98 */     violation.setAlertable(true);
/*  99 */     violation.setOwner(policy.getViolationOwnerForIdentity(context, identity));
/* 100 */     violation.setConstraintName(Message.localize("TodoPlugin_max_todo_exceeded").getLocalizedMessage());
/*     */ 
/*     */ 
/*     */     
/* 104 */     violation.setArgument("numActive", Integer.valueOf(numActive));
/*     */     
/* 106 */     return formatViolation(context, identity, policy, null, violation);
/*     */   }
/*     */ }


/* Location:              C:\Users\jkp14\Downloads\TodoPlugin-V3.zip!\lib\TodoPlugin.jar!\com\acme\todo\policy\TodoPolicyExecutor.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */