package com.eshiam.lifecycle.rest;

import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import sailpoint.api.SailPointContext;
import sailpoint.api.SailPointFactory;
import sailpoint.object.Rule;
import sailpoint.rest.plugin.BasePluginResource;
import sailpoint.rest.plugin.AllowAll;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Util;

/**
 * REST API for triggering Joiner/Mover/Leaver lifecycle events from JSON input.
 *
 * This plugin exposes:
 *   POST /LifecyclePlugin/joiner
 *   POST /LifecyclePlugin/mover
 *   POST /LifecyclePlugin/leaver
 *
 * Each API:
 *   - Receives JSON data from Postman
 *   - Calls a specific SailPoint Rule
 *   - Returns execution status JSON
 *
 * JSON example:
 * {
 *    "identityName": "jsmith",
 *    "firstName": "John",
 *    "lastName": "Smith",
 *    "department": "IT",
 *    "email": "john.smith@example.com"
 * }
 */
@Path("LifecyclePlugin")
@Produces({"application/json"})
@Consumes({"application/json"})
public class LifecycleResource extends BasePluginResource {

    @Override
    public String getPluginName() {
        return "LifecyclePlugin";
    }

    // ==============================
    // JOINER API
    // ==============================
    @POST
    @Path("joiner")
    @AllowAll
    public Map<String, Object> runJoiner(Map<String, Object> json) throws GeneralException {
        return executeLifecyclePath("LCE_Joiner_Rule", json, "JOINER");
    }

    // ==============================
    // MOVER API
    // ==============================
    @POST
    @Path("mover")
    @AllowAll
    public Map<String, Object> runMover(Map<String, Object> json) throws GeneralException {
        return executeLifecyclePath("LCE_Mover_Rule", json, "MOVER");
    }

    // ==============================
    // LEAVER API
    // ==============================
    @POST
    @Path("leaver")
    @AllowAll
    public Map<String, Object> runLeaver(Map<String, Object> json) throws GeneralException {
        return executeLifecyclePath("LCE_Leaver_Rule", json, "LEAVER");
    }

    // ========================================================================
    // CORE RULE EXECUTION FUNCTION FOR ALL THREE LCE CALLS
    // ========================================================================
    private Map<String, Object> executeLifecyclePath(String ruleName, Map<String, Object> input, String eventType)
            throws GeneralException {

        SailPointContext context = SailPointFactory.getCurrentContext();

        if (Util.isEmpty(ruleName)) {
            throw new GeneralException("Missing SailPoint Rule name for path: " + eventType);
        }

        Rule rule = context.getObject(Rule.class, ruleName);

if (rule == null) {
    throw new GeneralException("Rule not found in IIQ: " + ruleName);
}

Map<String, Object> args = new java.util.HashMap<>();
args.put("lceInput", input);
args.put("eventType", eventType);
args.put("requestId", Util.uuid());
args.put("initiator", getLoggedInUser().getName());

Object result = context.runRule(rule, args);

        // Build response
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("eventType", eventType);
        response.put("rule", ruleName);
        response.put("requestId", args.get("requestId"));
        response.put("status", "SUCCESS");
        response.put("result", result);

        return response;
    }
}
