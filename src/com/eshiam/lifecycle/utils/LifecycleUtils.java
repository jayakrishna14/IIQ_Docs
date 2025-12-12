package com.eshiam.lifecycle.utils;

import com.eshiam.lifecycle.model.LifecycleInput;
import com.eshiam.lifecycle.model.ApplicationAccess;
import com.eshiam.lifecycle.model.Access;
import com.eshiam.lifecycle.model.BatchResult;
import com.eshiam.lifecycle.model.BatchResponse;
import com.eshiam.lifecycle.model.BatchStatus;
import com.eshiam.lifecycle.model.ErrorCode;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sailpoint.api.SailPointContext;
import sailpoint.object.Rule;
import sailpoint.object.Workflow;
import sailpoint.tools.GeneralException;
import sailpoint.tools.Util;
import java.util.UUID;

import org.glassfish.jersey.server.ResourceConfig;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class LifecycleUtils {

    private static final Log log;
    static {
        Log tmp = null;
        try {
            tmp = LogFactory.getLog(LifecycleUtils.class);
        } catch (Throwable t) {
            // Logging implementation missing in the test runtime; fallback to no-op logger
            tmp = new Log() {
                @Override
                public void debug(Object message) {
                }

                @Override
                public void debug(Object message, Throwable t) {
                }

                @Override
                public void error(Object message) {
                }

                @Override
                public void error(Object message, Throwable t) {
                }

                @Override
                public void fatal(Object message) {
                }

                @Override
                public void fatal(Object message, Throwable t) {
                }

                @Override
                public void info(Object message) {
                }

                @Override
                public void info(Object message, Throwable t) {
                }

                @Override
                public boolean isDebugEnabled() {
                    return false;
                }

                @Override
                public boolean isErrorEnabled() {
                    return false;
                }

                @Override
                public boolean isFatalEnabled() {
                    return false;
                }

                @Override
                public boolean isInfoEnabled() {
                    return false;
                }

                @Override
                public boolean isTraceEnabled() {
                    return false;
                }

                @Override
                public void trace(Object message) {
                }

                @Override
                public void trace(Object message, Throwable t) {
                }

                @Override
                public void warn(Object message) {
                }

                @Override
                public void warn(Object message, Throwable t) {
                }
            };
        }
        log = tmp;
    }
    private static final Gson gson = new Gson();
    private static final ConcurrentMap<String, Map<String, Object>> lastRunInfo = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Map<String, Object>> requestResults = new ConcurrentHashMap<>();

    private LifecycleUtils() {
    }

    // ---------------------------------------------------------------------
    // Rule + Workflow Selection Logic
    // ---------------------------------------------------------------------
    public static String ruleForEvent(String eventType,
            String joinerRule,
            String moverRule,
            String leaverRule) {
        if (eventType == null)
            return joinerRule;

        switch (eventType.toUpperCase()) {
            case "MOVER":
                return moverRule;
            case "LEAVER":
                return leaverRule;
            default:
                return joinerRule;
        }
    }

    public static String workflowForEvent(String eventType,
            String joinerWf,
            String moverWf,
            String leaverWf) {
        if (eventType == null)
            return joinerWf;

        switch (eventType.toUpperCase()) {
            case "MOVER":
                return moverWf;
            case "LEAVER":
                return leaverWf;
            default:
                return joinerWf;
        }
    }

    // ---------------------------------------------------------------------
    // Execute Rule + Workflow
    // ---------------------------------------------------------------------
    public static Map<String, Object> executeLifecyclePath(
            SailPointContext context,
            String ruleName,
            String workflowName,
            LifecycleInput input,
            String eventType,
            String initiator,
            String requestId) throws GeneralException {

        String prefix = "### LCE EXECUTION [" + eventType + "] — ";

        Rule rule = null;
        Workflow wf = null;

        if (context != null) {
            rule = context.getObjectByName(Rule.class, ruleName);
            if (rule == null) {
                throw new LceExecutionException(ErrorCode.RULE_NOT_FOUND,
                        "Rule not found: " + ruleName);
            }

            wf = context.getObjectByName(Workflow.class, workflowName);
            if (wf == null) {
                throw new LceExecutionException(ErrorCode.WORKFLOW_NOT_FOUND,
                        "Workflow not found: " + workflowName);
            }
        }

        Map<String, Object> lceData = new HashMap<>();
        lceData.put("identityName", input.getIdentityName());
        lceData.put("firstName", input.getFirstName());
        lceData.put("lastName", input.getLastName());
        lceData.put("department", input.getDepartment());
        lceData.put("email", input.getEmail());

        if (input.getApplications() != null) {
            // Consolidate application access entries so multiple entries for the
            // same application+operation are merged
            List<ApplicationAccess> consolidated = consolidateApplications(input.getApplications());
            List<Map<String, Object>> apps = new ArrayList<>();
            for (ApplicationAccess aa : consolidated) {
                Map<String, Object> appMap = new HashMap<>();
                appMap.put("name", aa.getName());
                if (aa.getOperation() != null)
                    appMap.put("operation", aa.getOperation());

                Map<String, Object> access = new HashMap<>();
                if (aa.getAccess() != null) {
                    access.put("add", new ArrayList<>(aa.getAccess().getAdd()));
                    access.put("remove", new ArrayList<>(aa.getAccess().getRemove()));
                }
                appMap.put("access", access);

                apps.add(appMap);
            }
            lceData.put("applications", apps);
        }

        Map<String, Object> args = new HashMap<>();
        args.put("lceInput", lceData);
        args.put("lceWorkflow", workflowName);
        args.put("eventType", eventType);
        args.put("initiator", initiator);
        args.put("requestId", requestId != null ? requestId : generateUuid());

        Object result;
        if (context == null) {
            // Simulation mode
            Map<String, Object> sim = new HashMap<>();
            sim.put("result", "SIMULATED");
            sim.put("workflow", workflowName);
            sim.put("rule", ruleName);
            sim.put("requestId", requestId);
            sim.put("initiator", initiator);
            result = sim;
        } else {
            result = context.runRule(rule, args);
        }

        Map<String, Object> normalized = normalizeResultToMap(result);

        Map<String, Object> response = new HashMap<>();
        response.put("eventType", eventType);
        response.put("rule", ruleName);
        response.put("workflow", workflowName);
        response.put("requestId", requestId);
        response.put("initiator", initiator);
        response.put("result", normalized);
        response.put("status", "SUCCESS");

        // Persist a lookup entry for the request so callers can poll /status
        try {
            Map<String, Object> last = new HashMap<>();
            last.put("identityName", input.getIdentityName());
            last.put("eventType", eventType);
            last.put("status", "SUCCESS");
            last.put("requestId", requestId);
            last.put("rule", ruleName);
            last.put("workflow", workflowName);
            last.put("initiator", initiator);
            last.put("result", normalized);
            last.put("timestamp", new Date().toString());
            lastRunInfo.put(eventType != null ? eventType.toUpperCase() : "JOINER", last);
            requestResults.put(requestId, last);
        } catch (Throwable t) {
            // ignore persistence failures
        }

        return response;
    }

    // ---------------------------------------------------------------------
    // Consolidate list of ApplicationAccess entries
    // - Merge entries that have the same application name AND the same operation
    // - Add/Remove lists are unioned while preserving uniqueness
    // ---------------------------------------------------------------------
    private static List<ApplicationAccess> consolidateApplications(List<ApplicationAccess> apps) {
        Map<String, ApplicationAccess> keyToApp = new HashMap<>();

        for (ApplicationAccess aa : apps) {
            String name = aa.getName() != null ? aa.getName() : "";
            String op = aa.getOperation() != null ? aa.getOperation() : "";
            String key = name + "::" + op;

            ApplicationAccess existing = keyToApp.get(key);
            if (existing == null) {
                // Deep copy to avoid mutating caller's objects
                ApplicationAccess copy = new ApplicationAccess();
                copy.setName(name);
                copy.setOperation(op);
                Access acc = new Access();
                if (aa.getAccess() != null) {
                    acc.getAdd().addAll(aa.getAccess().getAdd());
                    acc.getRemove().addAll(aa.getAccess().getRemove());
                }
                copy.setAccess(acc);
                keyToApp.put(key, copy);
            } else {
                // merge add/remove lists
                Access existingAcc = existing.getAccess();
                if (existingAcc == null) {
                    existingAcc = new Access();
                    existing.setAccess(existingAcc);
                }
                if (aa.getAccess() != null) {
                    for (String add : aa.getAccess().getAdd()) {
                        if (!existingAcc.getAdd().contains(add))
                            existingAcc.getAdd().add(add);
                    }
                    for (String rem : aa.getAccess().getRemove()) {
                        if (!existingAcc.getRemove().contains(rem))
                            existingAcc.getRemove().add(rem);
                    }
                }
            }
        }

        return new ArrayList<>(keyToApp.values());
    }

    // ---------------------------------------------------------------------
    // Convert Map → LifecycleInput
    // ---------------------------------------------------------------------
    public static LifecycleInput mapToLifecycleInput(Map<String, Object> inputPayload) {
        LifecycleInput input = new LifecycleInput();
        if (inputPayload == null)
            return input;

        Map<String, Object> payload = inputPayload;

        if (payload.get("lceInput") instanceof Map) {
            payload = (Map<String, Object>) payload.get("lceInput");
        }

        if (payload.get("eventType") != null)
            input.setEventType(payload.get("eventType").toString());

        if (payload.get("identityName") != null)
            input.setIdentityName(payload.get("identityName").toString());

        if (payload.get("firstName") != null)
            input.setFirstName(payload.get("firstName").toString());

        if (payload.get("lastName") != null)
            input.setLastName(payload.get("lastName").toString());

        if (payload.get("department") != null)
            input.setDepartment(payload.get("department").toString());

        if (payload.get("email") != null)
            input.setEmail(payload.get("email").toString());

        if (payload.get("requestId") != null)
            input.setRequestId(payload.get("requestId").toString());

        Object apps = payload.get("applications");
        if (apps instanceof List) {
            List<ApplicationAccess> appList = new ArrayList<>();
            for (Object o : (List<?>) apps) {
                if (!(o instanceof Map))
                    continue;

                Map<String, Object> appMap = (Map<String, Object>) o;
                ApplicationAccess aa = new ApplicationAccess();

                if (appMap.get("name") != null)
                    aa.setName(appMap.get("name").toString());

                if (appMap.get("operation") != null)
                    aa.setOperation(appMap.get("operation").toString());

                if (appMap.get("access") instanceof Map) {
                    Map<String, Object> accMap = (Map<String, Object>) appMap.get("access");
                    Access a = new Access();

                    if (accMap.get("add") instanceof List) {
                        for (Object add : (List<?>) accMap.get("add"))
                            a.getAdd().add(add.toString());
                    }
                    if (accMap.get("remove") instanceof List) {
                        for (Object rm : (List<?>) accMap.get("remove"))
                            a.getRemove().add(rm.toString());
                    }

                    aa.setAccess(a);
                }

                appList.add(aa);
            }
            // Consolidate similar entries so callers (and rules) see neat merged access lists
            input.setApplications(consolidateApplications(appList));
        }

        return input;
    }

    // ---------------------------------------------------------------------
    // Validate LifecycleInput
    // ---------------------------------------------------------------------
    public static List<String> validateLifecycleInput(LifecycleInput input) {
        List<String> errors = new ArrayList<>();

        if (input == null) {
            errors.add("input is null");
            return errors;
        }
        if (input.getIdentityName() == null || input.getIdentityName().trim().isEmpty())
            errors.add("identityName is required");

        if (input.getEventType() != null) {
            String et = input.getEventType().toUpperCase();
            if (!et.equals("JOINER") && !et.equals("MOVER") && !et.equals("LEAVER"))
                errors.add("eventType must be JOINER, MOVER, or LEAVER");
        }

        if (input.getEmail() != null &&
                !input.getEmail().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            errors.add("email is invalid");
        }

        return errors;
    }

    // ---------------------------------------------------------------------
    // Convert rule result to a Map<String,Object> for consistent client responses
    // ---------------------------------------------------------------------
    public static Map<String, Object> normalizeResultToMap(Object resultObj) {
        if (resultObj == null)
            return null;
        if (resultObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> m = (Map<String, Object>) resultObj;
            return m;
        }
        Map<String, Object> single = new HashMap<>();
        single.put("result", resultObj);
        return single;
    }

    public static Map<String, Object> getLastRunInfo(String eventType) {
        if (eventType == null)
            eventType = "JOINER";
        return lastRunInfo.get(eventType.toUpperCase());
    }

    public static Map<String, Object> getResultForRequest(String requestId) {
        if (requestId == null)
            return null;
        return requestResults.get(requestId);
    }

    public static java.util.Set<String> getAllRequestIds() {
        return requestResults.keySet();
    }

    // ---------------------------------------------------------------------
    // Generate UUID with fallback to java.util.UUID if IdentityIQ Util is missing
    // ---------------------------------------------------------------------
    public static String generateUuid() {
        try {
            return Util.uuid();
        } catch (Throwable t) {
            return UUID.randomUUID().toString();
        }
    }

    // ---------------------------------------------------------------------
    // Batch Processing (FULLY FIXED)
    // ---------------------------------------------------------------------
    public static BatchResponse processBatch(
            List<?> batch,
            SailPointContext context,
            String joinerRule,
            String joinerWorkflow,
            String moverRule,
            String moverWorkflow,
            String leaverRule,
            String leaverWorkflow,
            String initiator,
            String batchRequestId) {

        BatchResponse response = new BatchResponse();
        List<BatchResult> results = new ArrayList<>();

        if (batch == null || batch.isEmpty()) {
            response.setStatus("EMPTY_BATCH");
            response.setResults(results);
            return response;
        }

        log.info("Processing batch items=" + batch.size() + " batchId=" + batchRequestId);

        Type mapType = new TypeToken<Map<String, Object>>() {
        }.getType();

        int index = 0;
        for (Object entry : batch) {
            log.debug("Batch item[" + index++ + "] class=" +
                    (entry != null ? entry.getClass().getName() : "null") +
                    " value=" + entry);

            Map<String, Object> row = null;

            try {

                // Case 1: real Map
                if (entry instanceof Map) {
                    row = (Map<String, Object>) entry;
                }

                // Case 2: JSON string
                else if (entry instanceof String) {
                    String raw = ((String) entry).trim();

                    // Try real JSON first
                    try {
                        row = gson.fromJson(raw, mapType);
                    } catch (Exception ignored) {
                    }

                    // IIQ flattened ={ } format
                    if (row == null || row.isEmpty()) {
                        String converted = raw
                                .replace("{", "{\"")
                                .replace("}", "\"}")
                                .replace("=", "\":\"")
                                .replace(", ", "\", \"");

                        row = gson.fromJson(converted, mapType);
                    }
                }

                // Case 3: fallback → toString → try JSON parse
                else if (entry != null) {
                    String raw = entry.toString();
                    try {
                        row = gson.fromJson(raw, mapType);
                    } catch (Exception ex) {
                        throw new RuntimeException(
                                "Unsupported batch item type: " +
                                        entry.getClass().getName(),
                                ex);
                    }
                }

                if (row == null) {
                    throw new RuntimeException("Unable to parse batch entry; null after parsing");
                }

            } catch (Exception ex) {
                log.error("Batch parse failure: " + entry, ex);

                BatchResult br = new BatchResult();
                br.setStatus(BatchStatus.FAILED);
                br.setErrorCode(ErrorCode.EXECUTION_ERROR);
                br.setErrors(List.of("Invalid batch item: " + ex.getMessage()));
                results.add(br);

                continue;
            }

            // Execute processing
            try {
                LifecycleInput li = mapToLifecycleInput(row);
                List<String> validation = validateLifecycleInput(li);

                if (!validation.isEmpty()) {
                    BatchResult br = new BatchResult();
                    br.setIdentityName(li.getIdentityName());
                    br.setEventType(li.getEventType());
                    br.setStatus(BatchStatus.FAILED_VALIDATION);
                    br.setErrors(validation);
                    br.setErrorCode(ErrorCode.VALIDATION_ERROR);
                    results.add(br);
                    continue;
                }

                String eventType = li.getEventType() != null ? li.getEventType() : "JOINER";

                String rule = ruleForEvent(eventType, joinerRule, moverRule, leaverRule);
                String workflow = workflowForEvent(eventType, joinerWorkflow, moverWorkflow, leaverWorkflow);
                String requestId = generateUuid();

                // Insert a pending entry so requestId lookups find something immediately
                try {
                    Map<String, Object> pending = new HashMap<>();
                    pending.put("identityName", li.getIdentityName());
                    pending.put("eventType", eventType);
                    pending.put("status", "PENDING");
                    pending.put("requestId", requestId);
                    pending.put("rule", rule);
                    pending.put("workflow", workflow);
                    pending.put("timestamp", new Date().toString());
                    requestResults.put(requestId, pending);
                } catch (Throwable t) {
                    // ignore
                }

                Map<String, Object> exec;
                if (context == null) {
                    // In unit tests or contexts where a SailPointContext isn't available we don't
                    // actually execute the rule - simulate a minimal response so tests can run
                    exec = new HashMap<>();
                    exec.put("result", "SIMULATED");
                    exec.put("workflow", workflow);
                    exec.put("rule", rule);
                    exec.put("requestId", requestId);
                    exec.put("initiator", initiator);
                } else {
                    exec = executeLifecyclePath(context, rule, workflow, li, eventType, initiator, requestId);
                }
                log.debug("executeLifecyclePath returned result type="
                        + (exec.get("result") != null ? exec.get("result").getClass().getName() : "null") + " value="
                        + exec.get("result"));

                BatchResult br = new BatchResult();
                br.setIdentityName(li.getIdentityName());
                br.setEventType(eventType);
                br.setStatus(BatchStatus.SUCCESS);
                br.setWorkflow(workflow);
                br.setRule(rule);
                br.setRequestId(requestId);

                Object resultObj = exec.get("result");
                log.debug("resultObj class=" + (resultObj != null ? resultObj.getClass().getName() : "null") + " value="
                        + resultObj);
                // Preserve original return type from rule: Map or simple String/primitive etc.
                br.setResult(resultObj);

                // Store last-run info and result per requestId
                try {
                    Map<String, Object> last = new HashMap<>();
                    last.put("identityName", li.getIdentityName());
                    last.put("eventType", eventType);
                    last.put("status", br.getStatus() != null ? br.getStatus().toString() : "UNKNOWN");
                    last.put("requestId", requestId);
                    last.put("rule", rule);
                    last.put("workflow", workflow);
                    last.put("initiator", initiator);
                    last.put("result", resultObj);
                    last.put("timestamp", new Date().toString());
                    lastRunInfo.put(eventType != null ? eventType.toUpperCase() : "JOINER", last);
                    requestResults.put(requestId, last);
                } catch (Throwable t) {
                    // ignore
                }
                results.add(br);

            } catch (Exception e) {
                log.error("Batch item failed for batchId=" + batchRequestId, e);

                BatchResult br = new BatchResult();
                br.setStatus(BatchStatus.FAILED);
                br.setErrors(List.of(e.getMessage()));
                br.setErrorCode(ErrorCode.EXECUTION_ERROR);
                results.add(br);
            }
        }

        response.setStatus("BATCH_COMPLETE");
        response.setResults(results);
        return response;
    }

    // ---------------------------------------------------------------------
    // JSON Helpers
    // ---------------------------------------------------------------------
    public static Map<String, Object> jsonStreamToMap(InputStream is) {
        return gson.fromJson(new InputStreamReader(is),
                new TypeToken<Map<String, Object>>() {
                }.getType());
    }

    public static Map<String, Object> executeRule(SailPointContext context, String ruleName,
            Map<String, Object> args, String initiator, String requestId) throws GeneralException {
        Map<String, Object> out = new HashMap<>();
        if (context == null) {
            out.put("status", "SIMULATED");
            out.put("rule", ruleName);
            out.put("requestId", requestId);
            out.put("arguments", args);
            out.put("message", "No SailPointContext available; simulation only");
            return out;
        }
        if (ruleName == null || ruleName.trim().isEmpty()) {
            out.put("status", "ERROR");
            out.put("message", "ruleName cannot be null");
            return out;
        }
        Rule rule = context.getObjectByName(Rule.class, ruleName);
        if (rule == null) {
            out.put("status", "NOT_FOUND");
            out.put("message", "Rule not found: " + ruleName);
            return out;
        }

        Map<String, Object> ruleArgs = args != null ? new HashMap<>(args) : new HashMap<>();
        ruleArgs.put("requestId", requestId);
        ruleArgs.put("initiator", initiator);
        ruleArgs.put("taskName", ruleName);

        Object result = context.runRule(rule, ruleArgs);
        out.put("status", "SUCCESS");
        out.put("rule", ruleName);
        out.put("requestId", requestId);
        out.put("result", result);
        return out;
    }

}
