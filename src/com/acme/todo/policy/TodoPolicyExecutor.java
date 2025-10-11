package com.acme.todo.policy;

import com.acme.todo.service.TodoService;
import java.util.ArrayList;
import java.util.List;
import sailpoint.api.SailPointContext;
import sailpoint.object.Identity;
import sailpoint.object.Policy;
import sailpoint.object.PolicyViolation;
import sailpoint.plugin.PluginContext;
import sailpoint.policy.BasePluginPolicyExecutor;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Message;

public class TodoPolicyExecutor
  extends BasePluginPolicyExecutor
{
  private static final String ARG_MAX_ACTIVE_TODOS = "maxActiveTodos";
  private static final String ARG_NUM_ACTIVE = "numActive";
  private static final String RENDERER = "ui/policy/renderer.xhtml";
  
  public String getPluginName() {
    return "TodoPlugin";
  }
  
  public List<PolicyViolation> evaluate(SailPointContext context, Policy policy, Identity id) throws GeneralException {
    List<PolicyViolation> violations = new ArrayList<>();
   
    int maxActiveTodos = policy.getInt("maxActiveTodos");
    if (maxActiveTodos <= 0) {
      return violations;
    }
    
    TodoService todoService = new TodoService((PluginContext)this);
    
    int numActive = todoService.getActiveTodosForUser(id.getId());
    if (numActive > maxActiveTodos) {
      violations.add(createViolation(context, policy, id, numActive));
    }
    
    return violations;
  }
  
  private PolicyViolation createViolation(SailPointContext context, Policy policy, Identity identity, int numActive) {
    PolicyViolation violation = new PolicyViolation();
    violation.setStatus(PolicyViolation.Status.Open);
    violation.setIdentity(identity);
    violation.setPolicy(policy);
    violation.setAlertable(true);
    violation.setOwner(policy.getViolationOwnerForIdentity(context, identity));
    violation.setConstraintName(Message.localize("TodoPlugin_max_todo_exceeded").getLocalizedMessage());
   
    violation.setArgument("numActive", Integer.valueOf(numActive));
    
    return formatViolation(context, identity, policy, null, violation);
  }
}