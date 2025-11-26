package com.eshiam.lifecycle.rest;

import com.eshiam.lifecycle.model.LifecycleInput;
import com.eshiam.lifecycle.model.ApplicationAccess;
import com.eshiam.lifecycle.model.Access;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import sailpoint.api.SailPointContext;
import sailpoint.object.Rule;
import sailpoint.object.Workflow;
import sailpoint.rest.plugin.BasePluginResource;
import sailpoint.rest.plugin.AllowAll;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Path("AutomationLCE")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@AllowAll
public class LifecycleResource extends BasePluginResource {

    private static final Log log = LogFactory.getLog(LifecycleResource.class);

    @Override
    public String getPluginName() {
        return "AutomationLCE";
    }

    // ------------------------------------------------------
    // JOINER
    // ------------------------------------------------------

    @POST
    @Path("joiner")
    public Response runJoiner(Map<String, Object> inputPayload) throws GeneralException {
        log.error("### JOINER triggered. Raw payload class: " + (inputPayload != null ? inputPayload.getClass().getName() : "null"));
        if (inputPayload == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", "ERROR");
            err.put("message", "Empty request body");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(err)
                    .build();
        }

        // Support batch request containing an "identities" array
        Object batchObj = inputPayload.get("identities");
        if (batchObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> batch = (List<Map<String, Object>>) batchObj;
            List<Map<String, Object>> results = new ArrayList<>();
            for (Map<String, Object> row : batch) {
                LifecycleInput item = mapToLifecycleInput(row);
                String eventType = item.getEventType() != null ? item.getEventType() : "JOINER";
                String ruleName = ruleForEvent(eventType);
                String workflowName = workflowForEvent(eventType);
                Map<String, Object> r = executeLifecyclePath(ruleName, workflowName, item, eventType);
                // Include identityName & eventType in the response record
                r.put("identityName", item.getIdentityName());
                r.put("eventType", eventType);
                results.add(r);
            }
            Map<String, Object> response = new HashMap<>();
            response.put("status", "BATCH_SUCCESS");
            response.put("results", results);
            return Response.ok(response).build();
        }

        LifecycleInput input = mapToLifecycleInput(inputPayload);
        log.error("### JOINER triggered for identity: " + input.getIdentityName());
        Map<String, Object> result = executeLifecyclePath(getSettingString("joinerRule"), getSettingString("joinerWorkflow"), input, "JOINER");
        return Response.ok(result).build();
    }

    // ------------------------------------------------------
    // MOVER
    // ------------------------------------------------------
    @POST
    @Path("mover")
    public Response runMover(Map<String, Object> inputPayload) throws GeneralException {
        log.error("### MOVER triggered. Raw payload class: " + (inputPayload != null ? inputPayload.getClass().getName() : "null"));
        if (inputPayload == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", "ERROR");
            err.put("message", "Empty request body");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(err)
                    .build();
        }

        Object batchObj = inputPayload.get("identities");
        if (batchObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> batch = (List<Map<String, Object>>) batchObj;
            List<Map<String, Object>> results = new ArrayList<>();
            for (Map<String, Object> row : batch) {
                LifecycleInput item = mapToLifecycleInput(row);
                String eventType = item.getEventType() != null ? item.getEventType() : "MOVER";
                String ruleName = ruleForEvent(eventType);
                String workflowName = workflowForEvent(eventType);
                Map<String, Object> r = executeLifecyclePath(ruleName, workflowName, item, eventType);
                r.put("identityName", item.getIdentityName());
                r.put("eventType", eventType);
                results.add(r);
            }
            Map<String, Object> response = new HashMap<>();
            response.put("status", "BATCH_SUCCESS");
            response.put("results", results);
            return Response.ok(response).build();
        }

        LifecycleInput input = mapToLifecycleInput(inputPayload);
        log.error("### MOVER triggered for identity: " + input.getIdentityName());
        Map<String, Object> result = executeLifecyclePath(getSettingString("moverRule"), getSettingString("moverWorkflow"), input, "MOVER");
        return Response.ok(result).build();
    }

    // ------------------------------------------------------
    // LEAVER
    // ------------------------------------------------------
    @POST
    @Path("leaver")
    public Response runLeaver(Map<String, Object> inputPayload) throws GeneralException {
        log.error("### LEAVER triggered. Raw payload class: " + (inputPayload != null ? inputPayload.getClass().getName() : "null"));
        if (inputPayload == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", "ERROR");
            err.put("message", "Empty request body");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(err)
                    .build();
        }

        Object batchObj = inputPayload.get("identities");
        if (batchObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> batch = (List<Map<String, Object>>) batchObj;
            List<Map<String, Object>> results = new ArrayList<>();
            for (Map<String, Object> row : batch) {
                LifecycleInput item = mapToLifecycleInput(row);
                String eventType = item.getEventType() != null ? item.getEventType() : "LEAVER";
                String ruleName = ruleForEvent(eventType);
                String workflowName = workflowForEvent(eventType);
                Map<String, Object> r = executeLifecyclePath(ruleName, workflowName, item, eventType);
                r.put("identityName", item.getIdentityName());
                r.put("eventType", eventType);
                results.add(r);
            }
            Map<String, Object> response = new HashMap<>();
            response.put("status", "BATCH_SUCCESS");
            response.put("results", results);
            return Response.ok(response).build();
        }

        LifecycleInput input = mapToLifecycleInput(inputPayload);
        log.error("### LEAVER triggered for identity: " + input.getIdentityName());
        Map<String, Object> result = executeLifecyclePath(getSettingString("leaverRule"),getSettingString("leaverWorkflow"), input, "LEAVER");
        return Response.ok(result).build();
    }

    /**
     * Convert a generic payload Map to the strongly typed LifecycleInput POJO.
     * Using a Map here avoids type mismatch issues caused by classloader differences
     * when Jersey/JSON providers deserialize into classes loaded by different classloaders.
     */
    private LifecycleInput mapToLifecycleInput(Map<String, Object> inputPayload) {
        LifecycleInput input = new LifecycleInput();
        if (inputPayload == null) {
            return input;
        }
        // Support both a flat payload and a nested payload under the "lceInput" key
        Map<String, Object> payload = inputPayload;
        Object nested = inputPayload.get("lceInput");
        if (nested instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> nestedMap = (Map<String, Object>) nested;
            payload = nestedMap;
        }

        Object o;
        o = payload.get("eventType");
        if (o != null) input.setEventType(o.toString());
        o = payload.get("identityName");
        if (o != null) input.setIdentityName(o.toString());
        o = payload.get("firstName");
        if (o != null) input.setFirstName(o.toString());
        o = payload.get("lastName");
        if (o != null) input.setLastName(o.toString());
        o = payload.get("department");
        if (o != null) input.setDepartment(o.toString());
        o = payload.get("email");
        if (o != null) input.setEmail(o.toString());

        // Parse applications if present
        Object apps = payload.get("applications");
        if (apps instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> appsList = (List<Map<String, Object>>) apps;
            List<ApplicationAccess> appObjects = new ArrayList<>();
            for (Map<String, Object> appMap : appsList) {
                ApplicationAccess aa = new ApplicationAccess();
                Object nameVal = appMap.get("name");
                if (nameVal != null) aa.setName(nameVal.toString());
                Object opVal = appMap.get("operation");
                if (opVal != null) aa.setOperation(opVal.toString());
                Object accessObj = appMap.get("access");
                if (accessObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> accessMap = (Map<String, Object>) accessObj;
                    Access a = new Access();
                    Object adds = accessMap.get("add");
                    if (adds instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Object> addList = (List<Object>) adds;
                        for (Object entry : addList) {
                            if (entry != null) a.getAdd().add(entry.toString());
                        }
                    }
                    Object removes = accessMap.get("remove");
                    if (removes instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Object> remList = (List<Object>) removes;
                        for (Object entry : remList) {
                            if (entry != null) a.getRemove().add(entry.toString());
                        }
                    }
                    aa.setAccess(a);
                }
                appObjects.add(aa);
            }
            input.setApplications(appObjects);
        }

        return input;
    }

    // ------------------------------------------------------
    // RULE EXECUTION (CORE LOGIC)
    // ------------------------------------------------------
    private Map<String, Object> executeLifecyclePath(
            String ruleName, String workflowName,
            LifecycleInput input,
            String eventType) throws GeneralException {

        String prefix = "### LCE EXECUTION [" + eventType + "] — ";
        log.error(prefix + "Start for identity: " + input.getIdentityName());

        SailPointContext context = getContext();

        try {

            Rule rule = context.getObjectByName(Rule.class, ruleName);

            if (rule == null) {
                log.error(prefix + "Rule not found: " + ruleName);
                throw new GeneralException("Rule not found: " + ruleName);
            }
            
            Workflow workflow = context.getObjectByName(Workflow.class, workflowName);

            if (workflow == null) {
                log.error(prefix + "Workflow not found: " + workflowName);
                throw new GeneralException("Workflow not found: " + workflowName);
            }

            log.error(prefix + "Executing rule: " + ruleName);

            // Convert POJO → Map (RULES ONLY ACCEPT MAPS)
            Map<String, Object> lceData = new HashMap<>();
            lceData.put("identityName", input.getIdentityName());
            lceData.put("firstName", input.getFirstName());
            lceData.put("lastName", input.getLastName());
            lceData.put("department", input.getDepartment());
            lceData.put("email", input.getEmail());
            // Include applications if present
            if (input.getApplications() != null && !input.getApplications().isEmpty()) {
                List<Map<String, Object>> apps = new ArrayList<>();
                for (ApplicationAccess aa : input.getApplications()) {
                    Map<String, Object> appMap = new HashMap<>();
                    appMap.put("name", aa.getName());
                    appMap.put("operation", aa.getOperation());
                    Map<String, Object> accessMap = new HashMap<>();
                    if (aa.getAccess() != null) {
                        accessMap.put("add", aa.getAccess().getAdd());
                        accessMap.put("remove", aa.getAccess().getRemove());
                    }
                    appMap.put("access", accessMap);
                    apps.add(appMap);
                }
                lceData.put("applications", apps);
            }

            Map<String, Object> args = new HashMap<>();
            args.put("lceInput", lceData);
            args.put("lceWorkflow", workflowName);
            args.put("eventType", eventType);
            args.put("requestId", Util.uuid());
            args.put("initiator", getLoggedInUser().getName());

            log.error(prefix + "Arguments sent to rule: " + args);

            Object result = context.runRule(rule, args);

            log.error(prefix + "Rule execution completed. Result = " + result);

            Map<String, Object> response = new HashMap<>();
            response.put("eventType", eventType);
            response.put("rule", ruleName);
            response.put("requestId", args.get("requestId"));
            response.put("initiator", args.get("initiator"));
            response.put("status", "SUCCESS");
            response.put("result", result);

            log.error(prefix + "Returning response JSON: " + response);

            return response;

        } catch (Exception e) {
            log.error(prefix + "ERROR during execution", e);

            Map<String, Object> err = new HashMap<>();
            err.put("status", "FAILED");
            err.put("eventType", eventType);
            err.put("error", e.getMessage());

            return err;
        }
    }

    private String ruleForEvent(String eventType) {
        if (eventType == null) return getSettingString("joinerRule");
        switch (eventType.toUpperCase()) {
            case "MOVER":
                return getSettingString("moverRule");
            case "LEAVER":
                return getSettingString("leaverRule");
            case "JOINER":
            default:
                return getSettingString("joinerRule");
        }
    }

    private String workflowForEvent(String eventType) {
        if (eventType == null) return getSettingString("joinerWorkflow");
        switch (eventType.toUpperCase()) {
            case "MOVER":
                return getSettingString("moverWorkflow");
            case "LEAVER":
                return getSettingString("leaverWorkflow");
            case "JOINER":
            default:
                return getSettingString("joinerWorkflow");
        }
    }
}
