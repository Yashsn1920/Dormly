package com.hostel.exception;

import java.io.Serial;

public final class AccessDeniedException extends HostelException {
    @Serial
    private static final long serialVersionUID = 1L;

    public AccessDeniedException(String message) {
        super(message);
    }
}
