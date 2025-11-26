package com.eshiam.lifecycle.model;

import java.util.Map;

public class BatchResult {
    private String identityName;
    private String eventType;
    private BatchStatus status; // SUCCESS, FAILED_VALIDATION, FAILED
    private String requestId;
    private String rule;
    private String workflow;
    private Map<String, Object> result;
    private java.util.List<String> errors;
    private ErrorCode errorCode = ErrorCode.NONE;

    public BatchResult() {
    }

    public String getIdentityName() {
        return identityName;
    }

    public void setIdentityName(String identityName) {
        this.identityName = identityName;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public BatchStatus getStatus() {
        return status;
    }

    public void setStatus(BatchStatus status) {
        this.status = status;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public String getWorkflow() {
        return workflow;
    }

    public void setWorkflow(String workflow) {
        this.workflow = workflow;
    }

    public Map<String, Object> getResult() {
        return result;
    }

    public void setResult(Map<String, Object> result) {
        this.result = result;
    }

    public java.util.List<String> getErrors() {
        return errors;
    }

    public void setErrors(java.util.List<String> errors) {
        this.errors = errors;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
