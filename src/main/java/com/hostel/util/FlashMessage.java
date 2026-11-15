package com.hostel.util;

import java.io.Serial;
import java.io.Serializable;

public final class FlashMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public enum Type {
        SUCCESS,
        ERROR,
        INFO
    }

    private final Type type;
    private final String message;

    public FlashMessage(Type type, String message) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message cannot be blank");
        }
        this.type = type;
        this.message = message.trim();
    }

    public Type getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}
