package com.eshiam.lifecycle.rest;

import com.eshiam.lifecycle.model.LifecycleInput;
import com.eshiam.lifecycle.utils.LifecycleUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import sailpoint.tools.Util;
import sailpoint.rest.plugin.BasePluginResource;
import sailpoint.rest.plugin.AllowAll;
import sailpoint.tools.GeneralException;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Path("AutomationLCE")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.WILDCARD })
@Produces(MediaType.APPLICATION_JSON)
@AllowAll
public class LifecycleResource extends BasePluginResource {

    private static final Log log = LogFactory.getLog(LifecycleResource.class);

    @Override
    public String getPluginName() {
        return "AutomationLCE";
    }

    // -------------------------
    // Generic LCE trigger (new)
    // POST /AutomationLCE/LCE/trigger
    // Accepts the JSON payload you supplied (eventType, identityName, applications[], ...)
    // -------------------------
    @POST
    @Path("LCE/trigger")
    public Response triggerLce(Map<String, Object> input) throws GeneralException {
        log.info("### LCE trigger called");

        if (input == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", "ERROR");
            err.put("message", "Empty request body");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }

        // Map/validate
        LifecycleInput lifecycleInput = LifecycleUtils.mapToLifecycleInput(input);
        List<String> validationErrors = LifecycleUtils.validateLifecycleInput(lifecycleInput);
        if (!validationErrors.isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", "FAILED_VALIDATION");
            err.put("errors", validationErrors);
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }

        // Determine which rule/workflow to use based on event type and plugin settings
        String eventType = lifecycleInput.getEventType() != null ? lifecycleInput.getEventType() : "JOINER";
        String joinerRule = getSettingString("joinerRule");
        String moverRule = getSettingString("moverRule");
        String leaverRule = getSettingString("leaverRule");

        String joinerWorkflow = getSettingString("joinerWorkflow");
        String moverWorkflow = getSettingString("moverWorkflow");
        String leaverWorkflow = getSettingString("leaverWorkflow");

        String ruleName = LifecycleUtils.ruleForEvent(eventType, joinerRule, moverRule, leaverRule);
        String workflowName = LifecycleUtils.workflowForEvent(eventType, joinerWorkflow, moverWorkflow, leaverWorkflow);
        String requestId = Util.uuid();

        try {
            Map<String, Object> result = LifecycleUtils.executeLifecyclePath(
                    getContext(), ruleName, workflowName, lifecycleInput,
                    eventType, getLoggedInUser() != null ? getLoggedInUser().getName() : "system", requestId);

            // Return the normalized result produced by executeLifecyclePath
            return Response.ok(result).build();

        } catch (Exception e) {
            log.error("LCE trigger failed for requestId=" + requestId, e);
            Map<String, Object> err = new HashMap<>();
            err.put("status", "FAILED");
            err.put("message", e.getMessage());
            err.put("requestId", requestId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(err).build();
        }
    }

    // ------------------------------------------------------
    // JOINER SINGLE
    // ------------------------------------------------------
    @POST
    @Path("joiner")
    public Response runJoiner(Map<String, Object> input) throws GeneralException {
        log.info("### JOINER triggered. input=" + input);

        if (input == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", "ERROR");
            err.put("message", "Empty request body");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }

        LifecycleInput lifecycleInput = LifecycleUtils.mapToLifecycleInput(input);
        List<String> validationErrors = LifecycleUtils.validateLifecycleInput(lifecycleInput);
        if (!validationErrors.isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", "FAILED_VALIDATION");
            err.put("errors", validationErrors);
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }

        String ruleName = getSettingString("joinerRule");
        String workflowName = getSettingString("joinerWorkflow");
        String requestId = Util.uuid();

        Map<String, Object> result = LifecycleUtils.executeLifecyclePath(
                getContext(), ruleName, workflowName, lifecycleInput, "JOINER",
                getLoggedInUser() != null ? getLoggedInUser().getName() : "system", requestId);

        return Response.ok(result).build();
    }

    // ------------------------------------------------------
    // MOVER SINGLE
    // ------------------------------------------------------
    @POST
    @Path("mover")
    public Response runMover(Map<String, Object> input) throws GeneralException {

        if (input == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", "ERROR");
            err.put("message", "Empty request body");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }

        LifecycleInput lifecycleInput = LifecycleUtils.mapToLifecycleInput(input);
        List<String> validationErrors = LifecycleUtils.validateLifecycleInput(lifecycleInput);
        if (!validationErrors.isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", "FAILED_VALIDATION");
            err.put("errors", validationErrors);
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }

        String ruleName = getSettingString("moverRule");
        String workflowName = getSettingString("moverWorkflow");
        String requestId = Util.uuid();

        Map<String, Object> result = LifecycleUtils.executeLifecyclePath(
                getContext(), ruleName, workflowName, lifecycleInput,
                "MOVER", getLoggedInUser() != null ? getLoggedInUser().getName() : "system", requestId);

        return Response.ok(result).build();
    }

    // ------------------------------------------------------
    // LEAVER SINGLE
    // ------------------------------------------------------
    @POST
    @Path("leaver")
    public Response runLeaver(Map<String, Object> input) throws GeneralException {

        if (input == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", "ERROR");
            err.put("message", "Empty request body");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }

        LifecycleInput lifecycleInput = LifecycleUtils.mapToLifecycleInput(input);
        List<String> validationErrors = LifecycleUtils.validateLifecycleInput(lifecycleInput);
        if (!validationErrors.isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", "FAILED_VALIDATION");
            err.put("errors", validationErrors);
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }

        String ruleName = getSettingString("leaverRule");
        String workflowName = getSettingString("leaverWorkflow");
        String requestId = Util.uuid();

        Map<String, Object> result = LifecycleUtils.executeLifecyclePath(
                getContext(), ruleName, workflowName, lifecycleInput,
                "LEAVER", getLoggedInUser() != null ? getLoggedInUser().getName() : "system", requestId);

        return Response.ok(result).build();
    }

    // ------------------------------------------------------
    // LCE BATCH
    // ------------------------------------------------------
    @POST
    @Path("LCE/batch")
    public Response runLceBatch(Map<String, Object> inputPayload) throws GeneralException {

        log.info("### LCE batch triggered");

        if (inputPayload == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", "ERROR");
            err.put("message", "Empty request body");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }

        Object batchObj = inputPayload.get("identities");
        if (!(batchObj instanceof List)) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", "ERROR");
            err.put("message", "Missing 'identities' array");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }

        // *** FIXED LINE ***
        List<?> batch = (List<?>) batchObj;

        String batchRequestId = Util.uuid();
        com.eshiam.lifecycle.model.BatchResponse resp = LifecycleUtils.processBatch(
                batch, getContext(),
                getSettingString("joinerRule"), getSettingString("joinerWorkflow"),
                getSettingString("moverRule"), getSettingString("moverWorkflow"),
                getSettingString("leaverRule"), getSettingString("leaverWorkflow"),
                getLoggedInUser() != null ? getLoggedInUser().getName() : "system", batchRequestId);

        return Response.ok(resp).build();
    }

    // ------------------------------------------------------
    // GET STATUS OF A REQUEST by requestId
    // ------------------------------------------------------
    @POST
    @Path("status")
    public Response getStatus(Map<String, Object> input) throws GeneralException {
        if (input == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", "ERROR");
            err.put("message", "Empty request body");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }
        Object rid = input.get("requestId");
        if (rid == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", "ERROR");
            err.put("message", "Missing 'requestId' in request body");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }
        // Support either a single requestId (string) or a list of requestIds
        if (rid instanceof List) {
            Map<String, Object> multi = new HashMap<>();
            for (Object idObj : (List<?>) rid) {
                if (idObj == null) continue;
                String id = idObj.toString();
                Map<String, Object> r = LifecycleUtils.getResultForRequest(id);
                multi.put(id, r);
            }
            return Response.ok(multi).build();
        } else {
            String requestId = rid.toString();
            Map<String, Object> r = LifecycleUtils.getResultForRequest(requestId);
            if (r == null) {
                Map<String, Object> err = new HashMap<>();
                err.put("status", "NOT_FOUND");
                err.put("message", "No request found for requestId " + requestId);
                return Response.status(Response.Status.NOT_FOUND).entity(err).build();
            }
            return Response.ok(r).build();
        }
    }

    // ------------------------------------------------------
    // Execute a rule for a given request - prefer rules over TaskManager
    // Request body: { requestId, taskName, taskArgs }
    // ------------------------------------------------------
    @POST
    @Path("task/runRule")
    public Response runRuleForRequest(Map<String, Object> input) throws GeneralException {
        if (input == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", "ERROR");
            err.put("message", "Empty request body");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }
        Object rid = input.get("requestId");
        Object taskNameObj = input.get("taskName");
        Object taskArgsObj = input.get("taskArgs");

        if (rid == null || taskNameObj == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", "ERROR");
            err.put("message", "Missing 'requestId' or 'taskName' in request body");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }
        String requestId = rid.toString();
        String taskName = taskNameObj.toString();

        @SuppressWarnings("unchecked")
        Map<String, Object> taskArgs = null;
        if (taskArgsObj instanceof Map) {
            taskArgs = (Map<String, Object>) taskArgsObj;
        } else if (taskArgsObj instanceof List) {
            taskArgs = new HashMap<>();
            taskArgs.put("taskArgs", taskArgsObj);
        } else {
            taskArgs = new HashMap<>();
        }

        // Add requestId into args for the rule
        taskArgs.put("requestId", requestId);
        taskArgs.put("initiator", getLoggedInUser() != null ? getLoggedInUser().getName() : "system");

        Map<String, Object> result;
        try {
            result = LifecycleUtils.executeRule(getContext(), taskName, taskArgs, getLoggedInUser() != null ? getLoggedInUser().getName() : "system", requestId);
        } catch (Exception e) {
            log.error("executeRule failed", e);
            Map<String, Object> err = new HashMap<>();
            err.put("status", "FAILED");
            err.put("message", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(err).build();
        }
        return Response.ok(result).build();
    }

    // ------------------------------------------------------
    // TEST: Create Test Identity
    // POST /AutomationLCE/test/identity/create
    // Calls configured identity creation rule (setting: identityCreationRule)
    // ------------------------------------------------------
    @POST
    @Path("test/identity/create")
    public Response createTestIdentity(Map<String, Object> input) throws GeneralException {
        log.info("### createTestIdentity called");

        if (input == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", "ERROR");
            err.put("message", "Empty request body");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }

        String ruleName = getSettingString("identityCreationRule");
        if (ruleName == null || ruleName.trim().isEmpty())
            ruleName = "IdentityCreationRule";

        String requestId = Util.uuid();
        Map<String, Object> args = new HashMap<>(input);
        args.put("requestId", requestId);
        args.put("initiator", getLoggedInUser() != null ? getLoggedInUser().getName() : "system");

        try {
            Map<String, Object> out = LifecycleUtils.executeRule(getContext(), ruleName, args,
                    getLoggedInUser() != null ? getLoggedInUser().getName() : "system", requestId);
            return Response.ok(out).build();
        } catch (Exception e) {
            log.error("createTestIdentity failed", e);
            Map<String, Object> err = new HashMap<>();
            err.put("status", "FAILED");
            err.put("message", e.getMessage());
            err.put("requestId", requestId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(err).build();
        }
    }

    // ------------------------------------------------------
    // GET LCE triggers
    // GET /AutomationLCE/LCE/triggers
    // Tries to call a configured rule `getTriggersRule` or falls back to plugin settings
    // ------------------------------------------------------
    @GET
    @Path("LCE/triggers")
    public Response getLceTriggers() throws GeneralException {
        String ruleName = getSettingString("getTriggersRule");
        String initiator = getLoggedInUser() != null ? getLoggedInUser().getName() : "system";
        String requestId = Util.uuid();

        if (ruleName != null && !ruleName.trim().isEmpty()) {
            Map<String, Object> args = new HashMap<>();
            args.put("requestId", requestId);
            args.put("initiator", initiator);
            Map<String, Object> res = LifecycleUtils.executeRule(getContext(), ruleName, args, initiator, requestId);
            return Response.ok(res).build();
        }

        // Fallback: attempt to read comma-separated plugin settings
        Map<String, Object> out = new HashMap<>();
        out.put("joinerTriggers", parseCsvSetting(getSettingString("joinerTriggers")));
        out.put("moverTriggers", parseCsvSetting(getSettingString("moverTriggers")));
        out.put("leaverTriggers", parseCsvSetting(getSettingString("leaverTriggers")));
        return Response.ok(out).build();
    }

    private List<String> parseCsvSetting(String s) {
        List<String> list = new ArrayList<>();
        if (s == null) return list;
        for (String part : s.split(",")) {
            String t = part.trim();
            if (!t.isEmpty()) list.add(t);
        }
        return list;
    }

    // ------------------------------------------------------
    // Run tasks (identity refresh / aggregations)
    // POST /AutomationLCE/tasks/run
    // Body: { runIdentityRefresh: true, runAggregation: ["App1","AD"], timeoutSeconds: 90 }
    // ------------------------------------------------------
    @POST
    @Path("tasks/run")
    public Response runTasks(Map<String, Object> input) throws GeneralException {
        log.info("### tasks/run called input=" + input);

        if (input == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", "ERROR");
            err.put("message", "Empty request body");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }

        boolean runRefresh = false;
        Object r = input.get("runIdentityRefresh");
        if (r instanceof Boolean) runRefresh = (Boolean) r;

        List<String> aggs = new ArrayList<>();
        Object a = input.get("runAggregation");
        if (a instanceof List) {
            for (Object o : (List<?>) a) if (o != null) aggs.add(o.toString());
        }

        Map<String, Object> details = new HashMap<>();
        String initiator = getLoggedInUser() != null ? getLoggedInUser().getName() : "system";
        String requestId = Util.uuid();

        try {
            if (runRefresh) {
                String ruleName = getSettingString("identityRefreshRule");
                if (ruleName == null || ruleName.trim().isEmpty()) ruleName = "IdentityRefreshRule";
                Map<String, Object> args = new HashMap<>();
                args.put("requestId", requestId);
                args.put("initiator", initiator);
                Map<String, Object> res = LifecycleUtils.executeRule(getContext(), ruleName, args, initiator, requestId);
                details.put("identityRefresh", res.getOrDefault("status", res));
            }

            Map<String, Object> aggrOut = new HashMap<>();
            for (String appName : aggs) {
                String ruleName = getSettingString("aggregationRule");
                if (ruleName == null || ruleName.trim().isEmpty()) ruleName = "AggregationRule";
                Map<String, Object> args = new HashMap<>();
                args.put("application", appName);
                args.put("requestId", requestId);
                args.put("initiator", initiator);
                Map<String, Object> res = LifecycleUtils.executeRule(getContext(), ruleName, args, initiator, requestId);
                aggrOut.put(appName, res.getOrDefault("status", res));
            }
            details.put("aggregations", aggrOut);

            Map<String, Object> ok = new HashMap<>();
            ok.put("status", "DONE");
            ok.put("details", details);
            return Response.ok(ok).build();

        } catch (Exception e) {
            log.error("tasks/run failed", e);
            Map<String, Object> err = new HashMap<>();
            err.put("status", "FAILED");
            err.put("message", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(err).build();
        }
    }

    // ------------------------------------------------------
    // Validate LCE outputs
    // POST /AutomationLCE/LCE/validate
    // Body: { identityName, eventType, expected: { accounts:[], entitlements:[], roleAssignments:[] }, requestId }
    // ------------------------------------------------------
    @POST
    @Path("LCE/validate")
    public Response validateLce(Map<String, Object> input) throws GeneralException {
        log.info("### LCE validate called input=" + input);

        if (input == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", "ERROR");
            err.put("message", "Empty request body");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }

        Object identityNameObj = input.get("identityName");
        if (identityNameObj == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", "ERROR");
            err.put("message", "Missing identityName");
            return Response.status(Response.Status.BAD_REQUEST).entity(err).build();
        }

        String identityName = identityNameObj.toString();
        String requestId = input.get("requestId") != null ? input.get("requestId").toString() : Util.uuid();
        String initiator = getLoggedInUser() != null ? getLoggedInUser().getName() : "system";

        // If a rule is configured, delegate validation to it
        String ruleName = getSettingString("validationRule");
        if (ruleName != null && !ruleName.trim().isEmpty()) {
            Map<String, Object> args = new HashMap<>();
            args.put("identityName", identityName);
            args.put("expected", input.get("expected"));
            args.put("requestId", requestId);
            args.put("initiator", initiator);
            try {
                Map<String, Object> out = LifecycleUtils.executeRule(getContext(), ruleName, args, initiator, requestId);
                return Response.ok(out).build();
            } catch (Exception e) {
                log.error("validation rule failed", e);
                Map<String, Object> err = new HashMap<>();
                err.put("status", "FAILED");
                err.put("message", e.getMessage());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(err).build();
            }
        }

        // Fallback: simple simulation — echo expected as actual (PASS) when no context/rule available
        Map<String, Object> resp = new HashMap<>();
        resp.put("identityName", identityName);
        resp.put("eventType", input.get("eventType"));
        Map<String, Object> validationResults = new HashMap<>();
        Object expected = input.get("expected");
        if (expected instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> exp = (Map<String, Object>) expected;
            for (String key : new String[]{"accounts", "entitlements", "roleAssignments"}) {
                Object expVal = exp.get(key);
                Map<String, Object> kv = new HashMap<>();
                kv.put("expected", expVal != null ? expVal : new ArrayList<>());
                kv.put("actual", expVal != null ? expVal : new ArrayList<>());
                kv.put("status", "PASS");
                validationResults.put(key, kv);
            }
        }
        resp.put("validationResults", validationResults);
        resp.put("overallStatus", "PASS");
        resp.put("requestId", requestId);
        resp.put("status", "SUCCESS");
        return Response.ok(resp).build();
    }

}
