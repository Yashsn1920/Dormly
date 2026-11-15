package com.hostel.model.enums;

public enum UserStatus {
    PENDING("Pending"),
    ACTIVE("Active"),
    INACTIVE("Inactive");

    private final String displayName;

    UserStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
