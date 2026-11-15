package com.hostel.model.enums;

public enum Role {
    STUDENT("Student"),
    WARDEN("Warden");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
