package com.hostel.model.enums;

public enum Priority {
    HIGH("High", 1),
    MEDIUM("Medium", 3),
    LOW("Low", 5);

    private final String displayName;
    private final int slaDays;

    Priority(String displayName, int slaDays) {
        this.displayName = displayName;
        this.slaDays = slaDays;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getSlaDays() {
        return slaDays;
    }
}
