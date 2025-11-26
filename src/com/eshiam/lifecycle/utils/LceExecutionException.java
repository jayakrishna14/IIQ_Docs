package com.eshiam.lifecycle.utils;

import sailpoint.tools.GeneralException;
import com.eshiam.lifecycle.model.ErrorCode;

public class LceExecutionException extends GeneralException {
    private ErrorCode code;

    public LceExecutionException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ErrorCode getErrorCode() {
        return code;
    }
}
