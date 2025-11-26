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
@Consumes({MediaType.APPLICATION_JSON, MediaType.WILDCARD})
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
    // JOINER BATCH
    // ------------------------------------------------------
    @POST
    @Path("joiner/batch")
    public Response runJoinerBatch(Map<String, Object> inputPayload) throws GeneralException {

        log.info("### JOINER batch triggered");

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
    // MOVER BATCH
    // ------------------------------------------------------
    @POST
    @Path("mover/batch")
    public Response runMoverBatch(Map<String, Object> inputPayload) throws GeneralException {

        log.info("### MOVER batch triggered");

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
    // LEAVER BATCH
    // ------------------------------------------------------
    @POST
    @Path("leaver/batch")
    public Response runLeaverBatch(Map<String, Object> inputPayload) throws GeneralException {

        log.info("### LEAVER batch triggered");

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
}
