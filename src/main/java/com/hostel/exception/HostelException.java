package com.hostel.exception;

import java.io.Serial;

public class HostelException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public HostelException(String message) {
        super(message);
    }

    public HostelException(String message, Throwable cause) {
        super(message, cause);
    }
}
