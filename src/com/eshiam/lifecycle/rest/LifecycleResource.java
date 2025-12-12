package com.eshiam.lifecycle.rest;

import com.eshiam.lifecycle.model.LifecycleInput;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.eshiam.lifecycle.utils.LifecycleUtils;
import sailpoint.tools.Util;
import sailpoint.rest.plugin.BasePluginResource;
import sailpoint.rest.plugin.AllowAll;
import sailpoint.tools.GeneralException;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

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
                getLoggedInUser().getName(), requestId);

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
                "MOVER", getLoggedInUser().getName(), requestId);

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
                "LEAVER", getLoggedInUser().getName(), requestId);

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
                getLoggedInUser().getName(), batchRequestId);

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
        taskArgs.put("initiator", getLoggedInUser().getName());

        Map<String, Object> result;
        try {
            result = LifecycleUtils.executeRule(getContext(), taskName, taskArgs, getLoggedInUser().getName(), requestId);
        } catch (Exception e) {
            log.error("executeRule failed", e);
            Map<String, Object> err = new HashMap<>();
            err.put("status", "FAILED");
            err.put("message", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(err).build();
        }
        return Response.ok(result).build();
    }

}
