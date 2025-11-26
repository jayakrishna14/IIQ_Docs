package com.eshiam.lifecycle.rest;

import com.eshiam.lifecycle.model.LifecycleInput;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import sailpoint.api.SailPointContext;
import sailpoint.object.Rule;
import sailpoint.object.Identity;
import sailpoint.rest.plugin.BasePluginResource;
import sailpoint.rest.plugin.AllowAll;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Util;

import java.util.HashMap;
import java.util.Map;

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
    // GET — MANAGER VALIDATION
    // ------------------------------------------------------
    @GET
    @Path("/iiq/user/{mgrName}")
    public Response checkIfManagerValid(@PathParam("mgrName") String mgrName) {
        log.error("### GET /iiq/user called for manager: " + mgrName);

        try {
            SailPointContext context = getContext();
            Identity mgr = context.getObjectByName(Identity.class, mgrName);

            boolean valid = (mgr != null && mgr.getManagerStatus());

            log.error("Manager validation result for [" + mgrName + "] = " + valid);

            Map<String, Object> result = new HashMap<>();
            result.put("manager", mgrName);
            result.put("validManager", valid);

            return Response.ok(result).build();

        } catch (Exception e) {
            log.error("Error while validating manager: " + mgrName, e);

            Map<String, Object> err = new HashMap<>();
            err.put("status", "ERROR");
            err.put("message", "Failed to validate manager");

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(err)
                    .build();
        }
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

        LifecycleInput input = mapToLifecycleInput(inputPayload);
        log.error("### JOINER triggered for identity: " + input.getIdentityName());
        Map<String, Object> result = executeLifecyclePath("Eshiam_LCE_Automation", input, "JOINER");
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

        LifecycleInput input = mapToLifecycleInput(inputPayload);
        log.error("### MOVER triggered for identity: " + input.getIdentityName());
        Map<String, Object> result = executeLifecyclePath("Eshiam_LCE_Automation", input, "MOVER");
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

        LifecycleInput input = mapToLifecycleInput(inputPayload);
        log.error("### LEAVER triggered for identity: " + input.getIdentityName());
        Map<String, Object> result = executeLifecyclePath("Eshiam_LCE_Automation", input, "LEAVER");
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

        return input;
    }

    // ------------------------------------------------------
    // RULE EXECUTION (CORE LOGIC)
    // ------------------------------------------------------
    private Map<String, Object> executeLifecyclePath(
            String ruleName,
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

            log.error(prefix + "Executing rule: " + ruleName);

            // Convert POJO → Map (RULES ONLY ACCEPT MAPS)
            Map<String, Object> lceData = new HashMap<>();
            lceData.put("identityName", input.getIdentityName());
            lceData.put("firstName", input.getFirstName());
            lceData.put("lastName", input.getLastName());
            lceData.put("department", input.getDepartment());
            lceData.put("email", input.getEmail());

            Map<String, Object> args = new HashMap<>();
            args.put("lceInput", lceData);
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
}
