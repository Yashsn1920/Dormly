package com.hostel.exception;

import java.io.Serial;

public final class AuthenticationException extends HostelException {
    @Serial
    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) {
        super(message);
    }
}
