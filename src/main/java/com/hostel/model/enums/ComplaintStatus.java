package com.hostel.model.enums;

public enum ComplaintStatus {
    SUBMITTED("Submitted", true),
    IN_PROGRESS("In Progress", true),
    RESOLVED("Resolved", false),
    CLOSED("Closed", false),
    REOPENED("Reopened", true),
    REJECTED("Rejected", false);

    private final String displayName;
    private final boolean open;

    ComplaintStatus(String displayName, boolean open) {
        this.displayName = displayName;
        this.open = open;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isOpen() {
        return open;
    }

    public boolean canTransitionTo(ComplaintStatus next) {
        if (next == null || next == this) {
            return false;
        }
        return switch (this) {
            case SUBMITTED -> next == IN_PROGRESS || next == REJECTED;
            case IN_PROGRESS -> next == RESOLVED;
            case RESOLVED -> next == CLOSED || next == REOPENED;
            case REOPENED -> next == IN_PROGRESS;
            case CLOSED, REJECTED -> false;
        };
    }
}
