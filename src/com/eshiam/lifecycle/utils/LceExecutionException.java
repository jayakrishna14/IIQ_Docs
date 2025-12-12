package com.eshiam.lifecycle.utils;

import sailpoint.tools.GeneralException;
import com.eshiam.lifecycle.model.ErrorCode;
import java.util.HashMap;
import java.util.Map;

/**
 * Lightweight exception used to signal LCE-specific failures with a typed ErrorCode.
 */
public class LceExecutionException extends GeneralException {
    private final ErrorCode code;

    public LceExecutionException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public LceExecutionException(ErrorCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public ErrorCode getErrorCode() {
        return code;
    }

    /**
     * Convert exception into a serializable Map suitable for REST responses.
     */
    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("errorCode", code != null ? code.name() : ErrorCode.NONE.name());
        m.put("message", getMessage());
        if (getCause() != null) {
            Map<String, Object> c = new HashMap<>();
            c.put("cause", getCause().getClass().getSimpleName());
            c.put("causeMessage", getCause().getMessage());
            m.put("cause", c);
        }
        return m;
    }

    @Override
    public String toString() {
        return "LceExecutionException{" +
                "code=" + (code != null ? code.name() : "NONE") +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}
