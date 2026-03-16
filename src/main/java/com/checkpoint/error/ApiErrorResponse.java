package com.checkpoint.error;

import java.util.List;

public class ApiErrorResponse {

    private final int status;
    private final String code;
    private final String message;
    private final List<String> errors;

    public ApiErrorResponse(int status, String code, String message, List<String> errors) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.errors = errors;
    }

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getErrors() {
        return errors;
    }
}

