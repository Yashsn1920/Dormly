package com.hostel.service;

import com.hostel.model.enums.Category;
import com.hostel.model.enums.Priority;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CategoryPriorityPolicyTest {
    private final CategoryPriorityPolicy policy = new CategoryPriorityPolicy();

    @Test
    void mapsEveryCategoryToItsConfiguredPriority() {
        assertEquals(Priority.HIGH, policy.priorityFor(Category.ELECTRICAL));
        assertEquals(Priority.HIGH, policy.priorityFor(Category.PLUMBING_WATER));
        assertEquals(Priority.HIGH, policy.priorityFor(Category.SECURITY));
        assertEquals(Priority.MEDIUM, policy.priorityFor(Category.CLEANING));
        assertEquals(Priority.MEDIUM, policy.priorityFor(Category.FURNITURE));
        assertEquals(Priority.LOW, policy.priorityFor(Category.WIFI));
        assertEquals(Priority.LOW, policy.priorityFor(Category.MESS_FOOD));
        assertEquals(Priority.LOW, policy.priorityFor(Category.OTHER));
    }

    @Test
    void rejectsNullCategory() {
        assertThrows(NullPointerException.class, () -> policy.priorityFor(null));
    }
}
