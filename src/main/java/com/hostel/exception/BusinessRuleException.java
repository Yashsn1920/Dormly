package com.hostel.exception;

import java.io.Serial;

public class BusinessRuleException extends HostelException {
    @Serial
    private static final long serialVersionUID = 1L;

    public BusinessRuleException(String message) {
        super(message);
    }
}
