package com.hostel.exception;

import java.io.Serial;

public final class NotFoundException extends HostelException {
    @Serial
    private static final long serialVersionUID = 1L;

    public NotFoundException(String message) {
        super(message);
    }
}
