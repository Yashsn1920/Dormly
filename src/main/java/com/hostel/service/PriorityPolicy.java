package com.hostel.service;

import com.hostel.model.enums.Category;
import com.hostel.model.enums.Priority;

public interface PriorityPolicy {
    Priority priorityFor(Category category);
}
