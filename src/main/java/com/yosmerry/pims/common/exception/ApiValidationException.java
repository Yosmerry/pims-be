package com.yosmerry.pims.common.exception;

import java.util.List;
import java.util.Map;

public class ApiValidationException extends RuntimeException {

    private final Map<String, List<String>> errors;

    public ApiValidationException(Map<String, List<String>> errors) {
        super("API validation failed");
        this.errors = errors;
    }

    public Map<String, List<String>> getErrors() {
        return errors;
    }
}
