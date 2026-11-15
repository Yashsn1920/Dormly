package com.hostel.model.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComplaintStatusTest {
    @Test
    void allowsExactlyTheTransitionsInTheSpecifiedFlow() {
        for (ComplaintStatus current : ComplaintStatus.values()) {
            for (ComplaintStatus next : ComplaintStatus.values()) {
                boolean expected = switch (current) {
                    case SUBMITTED -> next == ComplaintStatus.IN_PROGRESS || next == ComplaintStatus.REJECTED;
                    case IN_PROGRESS -> next == ComplaintStatus.RESOLVED;
                    case RESOLVED -> next == ComplaintStatus.CLOSED || next == ComplaintStatus.REOPENED;
                    case REOPENED -> next == ComplaintStatus.IN_PROGRESS;
                    case CLOSED, REJECTED -> false;
                };
                if (expected) {
                    assertTrue(current.canTransitionTo(next), current + " -> " + next);
                } else {
                    assertFalse(current.canTransitionTo(next), current + " -> " + next);
                }
            }
        }
        assertFalse(ComplaintStatus.SUBMITTED.canTransitionTo(null));
    }
}
