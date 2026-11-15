package com.hostel.exception;

import java.io.Serial;

public final class DataAccessException extends HostelException {
    @Serial
    private static final long serialVersionUID = 1L;

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
