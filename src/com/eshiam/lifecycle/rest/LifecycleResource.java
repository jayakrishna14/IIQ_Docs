package com.eshiam.lifecycle.rest;

import com.eshiam.lifecycle.model.LifecycleInput;
import com.eshiam.lifecycle.utils.LifecycleUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import sailpoint.object.Identity;
import sailpoint.object.IdentityTrigger;
import sailpoint.object.QueryOptions;
import sailpoint.rest.plugin.BasePluginResource;
import sailpoint.rest.plugin.AllowAll;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Util;

import java.util.*;

@Path("AutomationLCE")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.WILDCARD })
@Produces(MediaType.APPLICATION_JSON)
@AllowAll
public class LifecycleResource extends BasePluginResource {

    private static final Log log = LogFactory.getLog(LifecycleResource.class);

    @Override
    public String getPluginName() {
        log.error("getPluginName called");
        String name = "AutomationLCE";
        log.error("getPluginName returning: " + name);
        return name;
    }

    // --------------------
    // 1. CREATE TEST IDENTITY
    // --------------------
    @POST
    @Path("test/identity/create")
    public Response createTestIdentity(Map<String, Object> input) {
        log.error("createTestIdentity called with input: " + input);
        if (input == null) {
            log.error("Input is null");
            return bad("Empty request body");
        }

        String ruleName = getSettingString("identityCreationRule");
        log.error("identityCreationRule setting: " + ruleName);
        if (Util.isNullOrEmpty(ruleName)) {
            log.error("identityCreationRule is empty, using default");
            ruleName = "IdentityCreationRule";
        }

        String requestId = Util.uuid();
        log.error("Generated requestId: " + requestId);

        LifecycleInput li = LifecycleUtils.mapToLifecycleInput(input);
        log.error("Mapped LifecycleInput: " + li);

        try {
            log.error("Executing LifecycleUtils.executeLifecyclePath for CREATE");
            Map<String, Object> out = LifecycleUtils.executeLifecyclePath(
                    getContext(),
                    ruleName,
                    getSettingString("identityCreationWorkflow"),
                    li,
                    "CREATE",
                    getInitiator(),
                    requestId
            );
            log.error("Lifecycle execution output: " + out);

            return Response.ok(map(
                    "status", "SUCCESS",
                    "identityName", out.get("identityName"),
                    "identityId", out.get("identityId"),
                    "requestId", requestId
            )).build();

        } catch (Exception e) {
            log.error("Identity creation failed", e);
            return error("FAILED", e.getMessage());
        }
    }

    // --------------------
    // 2. GET LCE TRIGGERS
    // --------------------
    @GET
    @Path("LCE/triggers")
    public Response getLceTriggers() throws GeneralException {
        log.error("getLceTriggers called");

        QueryOptions qo = new QueryOptions();
        log.error("Created QueryOptions: " + qo);

        Iterator<Object[]> it = getContext().search(IdentityTrigger.class, qo, "name,type,disabled");
        log.error("Search executed, iterator: " + it);

        Map<String, Object> out = new HashMap<>();
        int i = 1;

        if (it == null) {
            log.error("Iterator is null - no triggers found");
        } else if (!it.hasNext()) {
            log.error("Iterator has no elements - empty result");
        } else {
            while (it.hasNext()) {
                Object[] row = it.next();
                log.error("Iterator row: " + Arrays.toString(row));

                String name = row[0] != null ? row[0].toString() : "null";
                String type = row[1] != null ? row[1].toString() : "null";
                String disabled = row[2] != null ? row[2].toString() : "null";
                log.error("Trigger details - name: " + name + ", type: " + type + ", disabled: " + disabled);

                out.put("" + i++, ". name : " + name + "; type : " + type + ", disabled : " + disabled);
            }
        }

        log.error("getLceTriggers returning: " + out);
        return Response.ok(out).build();
    }

    // --------------------
    // 3. TRIGGER LCE
    // --------------------
    @POST
    @Path("LCE/trigger")
    public Response triggerLce(Map<String, Object> input) {
        log.error("triggerLce called with input: " + input);
        if (input == null) return bad("Empty request body");

        LifecycleInput li = LifecycleUtils.mapToLifecycleInput(input);
        log.error("Mapped LifecycleInput: " + li);

        List<String> errors = LifecycleUtils.validateLifecycleInput(li);
        log.error("Validation errors: " + errors);
        if (!errors.isEmpty()) return Response.status(Response.Status.BAD_REQUEST)
                .entity(map("errors", errors)).build();

        String eventType = li.getEventType();
        String triggerName = input.getOrDefault("triggerName", eventType).toString();
        String ruleName = getSettingString(eventType.toLowerCase() + "Rule");
        String workflowName = getSettingString(eventType.toLowerCase() + "Workflow");
        String requestId = Util.uuid();

        log.error("Trigger details - eventType: " + eventType + ", triggerName: " + triggerName +
                ", ruleName: " + ruleName + ", workflowName: " + workflowName + ", requestId: " + requestId);

        new Thread(() -> {
            try {
                log.error("Executing lifecycle path asynchronously");
                LifecycleUtils.executeLifecyclePath(
                        getContext(),
                        ruleName,
                        workflowName,
                        li,
                        triggerName,
                        getInitiator(),
                        requestId
                );
            } catch (Exception e) {
                log.error("Lifecycle execution failed", e);
            }
        }).start();

        return Response.ok(map(
                "status", "TRIGGERED",
                "requestId", requestId,
                "message", eventType + " event started"
        )).build();
    }

    // --------------------
    // 4. RUN TASKS
    // --------------------
    @POST
    @Path("tasks/run")
    public Response runTasks(Map<String, Object> input) {
        log.error("runTasks called with input: " + input);
        if (input == null) return bad("Empty request body");

        boolean runRefresh = Boolean.TRUE.equals(input.get("runIdentityRefresh"));
        log.error("runIdentityRefresh: " + runRefresh);

        Object aggObj = input.get("runAggregation");
        List<String> aggs = new ArrayList<>();
        if (aggObj instanceof List) {
            for (Object o : (List<?>) aggObj) aggs.add(o.toString());
        }
        log.error("Aggregations to run: " + aggs);

        String requestId = Util.uuid();
        log.error("Generated requestId: " + requestId);
        Map<String, Object> details = new HashMap<>();

        try {
            if (runRefresh) {
                log.error("Running identity refresh task");
                LifecycleUtils.executeLifecyclePath(
                        getContext(),
                        getSettingString("identityRefreshRule"),
                        getSettingString("identityRefreshWorkflow"),
                        LifecycleUtils.mapToLifecycleInput(args(requestId)),
                        "REFRESH",
                        getInitiator(),
                        requestId
                );
                details.put("identityRefresh", "Completed");
                log.error("Identity refresh completed");
            }

            Map<String, Object> aggr = new HashMap<>();
            for (String app : aggs) {
                log.error("Running aggregation for application: " + app);
                LifecycleUtils.executeLifecyclePath(
                        getContext(),
                        getSettingString("aggregationRule"),
                        getSettingString("aggregationWorkflow"),
                        LifecycleUtils.mapToLifecycleInput(args(requestId, "application", app)),
                        "AGGREGATION",
                        getInitiator(),
                        requestId
                );
                aggr.put(app, "Completed");
                log.error("Aggregation completed for application: " + app);
            }
            details.put("aggregations", aggr);

            log.error("runTasks completed, returning details: " + details);
            return Response.ok(map("status", "DONE", "details", details)).build();

        } catch (Exception e) {
            log.error("Task execution failed", e);
            return error("FAILED", e.getMessage());
        }
    }

    // --------------------
    // 5. VALIDATE OUTPUT
    // --------------------
    @POST
    @Path("LCE/validate")
    public Response validate(Map<String, Object> input) {
        log.error("validate called with input: " + input);
        String identityName = (String) input.get("identityName");
        String requestId = (String) input.get("requestId");
        log.error("identityName: " + identityName + ", requestId: " + requestId);

        Identity id = null;
        try {
            id = getContext().getObjectByName(Identity.class, identityName);
            log.error("Identity lookup result: " + id);
            if (id == null) return bad("Identity not found");
        } catch (Exception ex) {
            log.error("Identity lookup failed", ex);
            return bad("Identity lookup failed");
        }

        Map<String, Object> actual = LifecycleUtils.getResultForRequest(requestId);
        log.error("Actual results for requestId: " + actual);
        if (actual == null) return bad("No execution results found");

        Map<String, Object> expected = safeCastMap(input.get("expected"));
        log.error("Expected results: " + expected);
        Map<String, Object> results = new HashMap<>();
        boolean pass = true;

        for (String key : expected.keySet()) {
            List<?> exp = safeCastList(expected.get(key));
            List<?> act = safeCastList(actual.get(key));
            List<Object> missing = new ArrayList<>();
            if (exp != null && act != null) {
                for (Object e : exp) if (!act.contains(e)) missing.add(e);
            }

            Map<String, Object> row = new HashMap<>();
            row.put("expected", exp);
            row.put("actual", act);
            row.put("status", missing.isEmpty() ? "PASS" : "FAIL");
            if (!missing.isEmpty()) row.put("missing", missing);

            if (!missing.isEmpty()) pass = false;
            results.put(key, row);

            log.error("Validation result for key " + key + ": " + row);
        }

        log.error("Validation overallStatus: " + (pass ? "PASS" : "FAIL"));
        return Response.ok(map(
                "identityName", identityName,
                "eventType", input.get("eventType"),
                "validationResults", results,
                "overallStatus", pass ? "PASS" : "FAIL",
                "requestId", requestId
        )).build();
    }

    // --------------------
    // HELPERS
    // --------------------
    private String getInitiator() {
        try {
            String user = getLoggedInUser() != null ? getLoggedInUser().getName() : "system";
            log.error("getInitiator returned: " + user);
            return user;
        } catch (GeneralException e) {
            log.error("Failed to get initiator, defaulting to system", e);
            return "system";
        }
    }

    private Map<String, Object> args(String requestId, Object... kv) {
        Map<String, Object> m = new HashMap<>();
        m.put("requestId", requestId);
        m.put("initiator", getInitiator());
        for (int i = 0; i < kv.length; i += 2) m.put(kv[i].toString(), kv[i + 1]);
        log.error("args map created: " + m);
        return m;
    }

    private Response bad(String msg) {
        log.error("Bad request: " + msg);
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(map("status", "ERROR", "message", msg)).build();
    }

    private Response error(String status, String msg) {
        log.error("Error response: status=" + status + ", message=" + msg);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(map("status", status, "message", msg)).build();
    }

    private Map<String, Object> map(Object... kv) {
        Map<String, Object> m = new HashMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put(kv[i].toString(), kv[i + 1]);
        return m;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> safeCastMap(Object obj) {
        return obj instanceof Map ? (Map<String, Object>) obj : Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    private List<Object> safeCastList(Object obj) {
        return obj instanceof List ? (List<Object>) obj : Collections.emptyList();
    }
}
