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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class LifecycleUtils {

    private static final Log log;
    static {
        Log tmp = null;
        try {
            tmp = LogFactory.getLog(LifecycleUtils.class);
        } catch (Throwable t) {
            tmp = new NoOpLog();
        }
        log = tmp;
    }

    private static final Gson gson = new Gson();
    private static final ConcurrentMap<String, Map<String, Object>> lastRunInfo = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Map<String, Object>> requestResults = new ConcurrentHashMap<>();

    private LifecycleUtils() { }

    // ----------------- Rule + Workflow Selection -----------------
    public static String ruleForEvent(String eventType, String joinerRule, String moverRule, String leaverRule) {
        if (eventType == null) return joinerRule;
        switch (eventType.toUpperCase()) {
            case "MOVER": return moverRule;
            case "LEAVER": return leaverRule;
            default: return joinerRule;
        }
    }

    public static String workflowForEvent(String eventType, String joinerWf, String moverWf, String leaverWf) {
        if (eventType == null) return joinerWf;
        switch (eventType.toUpperCase()) {
            case "MOVER": return moverWf;
            case "LEAVER": return leaverWf;
            default: return joinerWf;
        }
    }

    // ----------------- Execute Lifecycle Path -----------------
    public static Map<String, Object> executeLifecyclePath(
            SailPointContext context,
            String ruleName,
            String workflowName,
            LifecycleInput input,
            String eventType,
            String initiator,
            String requestId) throws GeneralException {

        Rule rule = null;
        Workflow wf = null;

        if (context != null) {
            rule = context.getObjectByName(Rule.class, ruleName);
            if (rule == null)
                throw new RuntimeException("Rule not found: " + ruleName);

            wf = context.getObjectByName(Workflow.class, workflowName);
            if (wf == null)
                throw new RuntimeException("Workflow not found: " + workflowName);
        }

        Map<String, Object> lceData = new HashMap<>();
        lceData.put("identityName", input.getIdentityName());
        lceData.put("firstName", input.getFirstName());
        lceData.put("lastName", input.getLastName());
        lceData.put("department", input.getDepartment());
        lceData.put("email", input.getEmail());

        if (input.getApplications() != null) {
            List<ApplicationAccess> consolidated = consolidateApplications(input.getApplications());
            List<Map<String, Object>> apps = new ArrayList<>();
            for (ApplicationAccess aa : consolidated) {
                Map<String, Object> appMap = new HashMap<>();
                appMap.put("name", aa.getName());
                if (aa.getOperation() != null) appMap.put("operation", aa.getOperation());

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
            result = Map.of(
                    "result", "SIMULATED",
                    "workflow", workflowName,
                    "rule", ruleName,
                    "requestId", requestId,
                    "initiator", initiator
            );
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
        } catch (Throwable ignored) { }

        return response;
    }

    // ----------------- Consolidate Applications -----------------
    private static List<ApplicationAccess> consolidateApplications(List<ApplicationAccess> apps) {
        Map<String, ApplicationAccess> keyToApp = new HashMap<>();
        for (ApplicationAccess aa : apps) {
            String key = (aa.getName() != null ? aa.getName() : "") + "::" + (aa.getOperation() != null ? aa.getOperation() : "");
            ApplicationAccess existing = keyToApp.get(key);
            if (existing == null) {
                ApplicationAccess copy = new ApplicationAccess();
                copy.setName(aa.getName());
                copy.setOperation(aa.getOperation());
                Access acc = new Access();
                if (aa.getAccess() != null) {
                    acc.getAdd().addAll(aa.getAccess().getAdd());
                    acc.getRemove().addAll(aa.getAccess().getRemove());
                }
                copy.setAccess(acc);
                keyToApp.put(key, copy);
            } else {
                Access existingAcc = existing.getAccess();
                if (existingAcc == null) {
                    existingAcc = new Access();
                    existing.setAccess(existingAcc);
                }
                if (aa.getAccess() != null) {
                    for (String add : aa.getAccess().getAdd())
                        if (!existingAcc.getAdd().contains(add)) existingAcc.getAdd().add(add);
                    for (String rem : aa.getAccess().getRemove())
                        if (!existingAcc.getRemove().contains(rem)) existingAcc.getRemove().add(rem);
                }
            }
        }
        return new ArrayList<>(keyToApp.values());
    }

    // ----------------- Map to LifecycleInput -----------------
    @SuppressWarnings("unchecked")
    public static LifecycleInput mapToLifecycleInput(Map<String, Object> inputPayload) {
        LifecycleInput input = new LifecycleInput();
        if (inputPayload == null) return input;

        Map<String, Object> payload = inputPayload;

        Object lceInputObj = payload.get("lceInput");
        if (lceInputObj instanceof Map) {
            payload = (Map<String, Object>) lceInputObj;
        }

        input.setEventType(payload.getOrDefault("eventType", "").toString());
        input.setIdentityName(payload.getOrDefault("identityName", "").toString());
        input.setFirstName((payload.get("firstName") != null) ? payload.get("firstName").toString() : null);
        input.setLastName((payload.get("lastName") != null) ? payload.get("lastName").toString() : null);
        input.setDepartment((payload.get("department") != null) ? payload.get("department").toString() : null);
        input.setEmail((payload.get("email") != null) ? payload.get("email").toString() : null);
        input.setRequestId((payload.get("requestId") != null) ? payload.get("requestId").toString() : null);

        Object appsObj = payload.get("applications");
        if (appsObj instanceof List) {
            List<ApplicationAccess> appList = new ArrayList<>();
            for (Object o : (List<?>) appsObj) {
                if (!(o instanceof Map)) continue;
                Map<String, Object> appMap = (Map<String, Object>) o;
                ApplicationAccess aa = new ApplicationAccess();
                aa.setName((String) appMap.get("name"));
                aa.setOperation((String) appMap.get("operation"));

                Object accessObj = appMap.get("access");
                if (accessObj instanceof Map) {
                    Map<String, Object> accMap = (Map<String, Object>) accessObj;
                    Access a = new Access();
                    Object addList = accMap.get("add");
                    if (addList instanceof List) for (Object add : (List<?>) addList) a.getAdd().add(add.toString());
                    Object removeList = accMap.get("remove");
                    if (removeList instanceof List) for (Object rm : (List<?>) removeList) a.getRemove().add(rm.toString());
                    aa.setAccess(a);
                }

                appList.add(aa);
            }
            input.setApplications(consolidateApplications(appList));
        }

        return input;
    }

    // ----------------- Validation -----------------
    public static List<String> validateLifecycleInput(LifecycleInput input) {
        List<String> errors = new ArrayList<>();
        if (input == null) { errors.add("input is null"); return errors; }
        if (input.getIdentityName() == null || input.getIdentityName().trim().isEmpty())
            errors.add("identityName is required");
        if (input.getEventType() != null) {
            String et = input.getEventType().toUpperCase();
            if (!et.equals("JOINER") && !et.equals("MOVER") && !et.equals("LEAVER"))
                errors.add("eventType must be JOINER, MOVER, or LEAVER");
        }
        if (input.getEmail() != null &&
            !input.getEmail().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
            errors.add("email is invalid");
        return errors;
    }

    // ----------------- Result Normalization -----------------
    @SuppressWarnings("unchecked")
    public static Map<String, Object> normalizeResultToMap(Object resultObj) {
        if (resultObj == null) return null;
        if (resultObj instanceof Map) return (Map<String, Object>) resultObj;
        return Map.of("result", resultObj);
    }

    // ----------------- Last Run / Request Lookup -----------------
    public static Map<String, Object> getLastRunInfo(String eventType) {
        if (eventType == null) eventType = "JOINER";
        return lastRunInfo.get(eventType.toUpperCase());
    }

    public static Map<String, Object> getResultForRequest(String requestId) {
        if (requestId == null) return null;
        return requestResults.get(requestId);
    }

    public static Set<String> getAllRequestIds() {
        return requestResults.keySet();
    }

    // ----------------- UUID -----------------
    public static String generateUuid() {
        try { return Util.uuid(); } catch (Throwable t) { return UUID.randomUUID().toString(); }
    }

    // ----------------- JSON Helpers -----------------
    public static Map<String, Object> jsonStreamToMap(InputStream is) {
        return gson.fromJson(new InputStreamReader(is), new TypeToken<Map<String, Object>>(){}.getType());
    }

    // ----------------- No-Op Logger -----------------
    private static class NoOpLog implements Log {
        public void debug(Object message) { }
        public void debug(Object message, Throwable t) { }
        public void error(Object message) { }
        public void error(Object message, Throwable t) { }
        public void fatal(Object message) { }
        public void fatal(Object message, Throwable t) { }
        public void info(Object message) { }
        public void info(Object message, Throwable t) { }
        public boolean isDebugEnabled() { return false; }
        public boolean isErrorEnabled() { return false; }
        public boolean isFatalEnabled() { return false; }
        public boolean isInfoEnabled() { return false; }
        public boolean isTraceEnabled() { return false; }
        public void trace(Object message) { }
        public void trace(Object message, Throwable t) { }
        public void warn(Object message) { }
        public void warn(Object message, Throwable t) { }
        public boolean isWarnEnabled() { return false; }
    }
}
