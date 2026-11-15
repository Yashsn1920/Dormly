package com.hostel.model;

import com.hostel.model.enums.Category;
import com.hostel.model.enums.ComplaintStatus;
import com.hostel.model.enums.Priority;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComplaintTest {
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 11, 1, 12, 0);

    @Test
    void overdueUsesStrictBoundaryForEveryPriority() {
        for (Priority priority : Priority.values()) {
            Complaint complaint = complaint(priority, ComplaintStatus.SUBMITTED);
            LocalDateTime dueAt = CREATED_AT.plusDays(priority.getSlaDays());

            assertFalse(complaint.isOverdue(dueAt));
            assertTrue(complaint.isOverdue(dueAt.plusNanos(1)));
        }
    }

    @Test
    void closedAndRejectedComplaintsAreNeverOverdue() {
        for (ComplaintStatus status : new ComplaintStatus[]{ComplaintStatus.CLOSED, ComplaintStatus.REJECTED}) {
            Complaint complaint = complaint(Priority.HIGH, status);
            assertFalse(complaint.isOverdue(CREATED_AT.plusDays(100)));
        }
    }

    @Test
    void rejectsMissingCategoryAndInvalidRating() {
        assertThrows(IllegalArgumentException.class, () -> new Complaint(
                1, 1, null, "Broken light", "The light is not working.", Priority.HIGH,
                ComplaintStatus.SUBMITTED, null, null, CREATED_AT, null, null, null, null, null));

        Complaint complaint = complaint(Priority.HIGH, ComplaintStatus.SUBMITTED);
        assertThrows(IllegalArgumentException.class, () -> complaint.setRating(0));
        assertThrows(IllegalArgumentException.class, () -> complaint.setRating(6));
    }

    private static Complaint complaint(Priority priority, ComplaintStatus status) {
        return new Complaint(1, 1, Category.ELECTRICAL, "Broken light",
                "The room light is not working.", priority, status, null, null,
                CREATED_AT, null, null, null, null, null);
    }
}
