package com.hostel.service;

import com.hostel.model.enums.Category;
import com.hostel.model.enums.Priority;

import java.util.Objects;

public final class CategoryPriorityPolicy implements PriorityPolicy {
    @Override
    public Priority priorityFor(Category category) {
        Objects.requireNonNull(category, "category cannot be null");
        return switch (category) {
            case ELECTRICAL, PLUMBING_WATER, SECURITY -> Priority.HIGH;
            case CLEANING, FURNITURE -> Priority.MEDIUM;
            case WIFI, MESS_FOOD, OTHER -> Priority.LOW;
        };
    }
}
