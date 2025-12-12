package com.eshiam.lifecycle.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility to convert exceptions into structured error maps the REST API returns.
 */
public final class ErrorUtils {

    private ErrorUtils() {}

    /**
     * Convert an exception into a JSON-friendly Map.
     * If requestId is provided it will be included for correlation.
     */
    public static Map<String, Object> toErrorMap(Throwable t, String requestId) {
        Map<String, Object> out = new HashMap<>();
        if (requestId != null) out.put("requestId", requestId);

        if (t == null) {
            out.put("status", "ERROR");
            out.put("message", "Unknown error");
            return out;
        }

        // LCE-specific exception with typed ErrorCode
        if (t instanceof LceExecutionException) {
            LceExecutionException le = (LceExecutionException) t;
            out.put("status", "FAILED_LCE");
            out.put("errorCode", le.getErrorCode() != null ? le.getErrorCode().name() : "NONE");
            out.put("message", le.getMessage());
            // optionally include cause info
            if (le.getCause() != null) {
                Map<String, Object> cause = new HashMap<>();
                cause.put("type", le.getCause().getClass().getSimpleName());
                cause.put("message", le.getCause().getMessage());
                out.put("cause", cause);
            }
            return out;
        }

        // General exception path
        out.put("status", "FAILED");
        out.put("exception", t.getClass().getSimpleName());
        out.put("message", t.getMessage() != null ? t.getMessage() : "No message");

        // include nested cause info if present
        if (t.getCause() != null) {
            Map<String, Object> cause = new HashMap<>();
            cause.put("type", t.getCause().getClass().getSimpleName());
            cause.put("message", t.getCause().getMessage());
            out.put("cause", cause);
        }

        return out;
    }
}
