package com.hostel.exception;

import java.io.Serial;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ValidationException extends HostelException {
    @Serial
    private static final long serialVersionUID = 1L;

    private final Map<String, String> errors;

    public ValidationException(Map<String, String> errors) {
        super("Validation failed");
        if (errors == null || errors.isEmpty()) {
            throw new IllegalArgumentException("errors cannot be null or empty");
        }
        this.errors = Collections.unmodifiableMap(new LinkedHashMap<>(errors));
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
