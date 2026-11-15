package com.hostel.model.enums;

public enum Category {
    ELECTRICAL("Electrical"),
    PLUMBING_WATER("Plumbing/Water"),
    CLEANING("Cleaning"),
    FURNITURE("Furniture"),
    WIFI("Wi-Fi"),
    MESS_FOOD("Mess/Food"),
    SECURITY("Security"),
    OTHER("Other");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
