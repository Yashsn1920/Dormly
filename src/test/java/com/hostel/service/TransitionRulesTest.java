package com.hostel.service;

import com.hostel.model.enums.ComplaintStatus;
import com.hostel.model.enums.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransitionRulesTest {
    @Test
    void enforcesBothStatusFlowAndActorPermissions() {
        assertTrue(TransitionRules.canTransition(Role.WARDEN,
                ComplaintStatus.SUBMITTED, ComplaintStatus.IN_PROGRESS));
        assertTrue(TransitionRules.canTransition(Role.WARDEN,
                ComplaintStatus.SUBMITTED, ComplaintStatus.REJECTED));
        assertTrue(TransitionRules.canTransition(Role.WARDEN,
                ComplaintStatus.IN_PROGRESS, ComplaintStatus.RESOLVED));
        assertTrue(TransitionRules.canTransition(Role.WARDEN,
                ComplaintStatus.REOPENED, ComplaintStatus.IN_PROGRESS));
        assertTrue(TransitionRules.canTransition(Role.STUDENT,
                ComplaintStatus.RESOLVED, ComplaintStatus.CLOSED));
        assertTrue(TransitionRules.canTransition(Role.STUDENT,
                ComplaintStatus.RESOLVED, ComplaintStatus.REOPENED));

        assertFalse(TransitionRules.canTransition(Role.STUDENT,
                ComplaintStatus.SUBMITTED, ComplaintStatus.IN_PROGRESS));
        assertFalse(TransitionRules.canTransition(Role.STUDENT,
                ComplaintStatus.RESOLVED, ComplaintStatus.IN_PROGRESS));
        assertFalse(TransitionRules.canTransition(Role.WARDEN,
                ComplaintStatus.RESOLVED, ComplaintStatus.CLOSED));
        assertFalse(TransitionRules.canTransition(Role.WARDEN,
                ComplaintStatus.SUBMITTED, ComplaintStatus.RESOLVED));
        assertFalse(TransitionRules.canTransition(null,
                ComplaintStatus.SUBMITTED, ComplaintStatus.IN_PROGRESS));
    }
}
