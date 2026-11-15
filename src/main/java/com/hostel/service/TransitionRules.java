package com.hostel.service;

import com.hostel.model.enums.ComplaintStatus;
import com.hostel.model.enums.Role;

import java.util.Objects;

public final class TransitionRules {
    private TransitionRules() {
    }

    public static boolean canTransition(Role actor, ComplaintStatus current, ComplaintStatus next) {
        if (actor == null || current == null || next == null || !current.canTransitionTo(next)) {
            return false;
        }
        return switch (actor) {
            case WARDEN -> (current == ComplaintStatus.SUBMITTED
                    && (next == ComplaintStatus.IN_PROGRESS || next == ComplaintStatus.REJECTED))
                    || (current == ComplaintStatus.IN_PROGRESS && next == ComplaintStatus.RESOLVED)
                    || (current == ComplaintStatus.REOPENED && next == ComplaintStatus.IN_PROGRESS);
            case STUDENT -> current == ComplaintStatus.RESOLVED
                    && (next == ComplaintStatus.CLOSED || next == ComplaintStatus.REOPENED);
        };
    }

    public static void requireTransition(Role actor, ComplaintStatus current, ComplaintStatus next) {
        Objects.requireNonNull(actor, "actor cannot be null");
        if (!canTransition(actor, current, next)) {
            throw new IllegalArgumentException("Role " + actor + " cannot move complaint from "
                    + current + " to " + next + '.');
        }
    }
}
