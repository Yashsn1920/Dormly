package com.hostel.controller;

import com.hostel.exception.ValidationException;
import com.hostel.report.ComplaintFilter;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WardenComplaintServletTest {
    @Test
    void acceptsAnOrderedDateRange() {
        ComplaintFilter filter = new ComplaintFilter();
        LocalDate from = LocalDate.of(2026, 11, 14);
        LocalDate to = LocalDate.of(2026, 11, 15);

        WardenComplaintServlet.setDateRange(filter, from, to);

        assertEquals(from, filter.getFromDate());
        assertEquals(to, filter.getToDate());
    }

    @Test
    void rejectsAReversedDateRangeAsValidationError() {
        ComplaintFilter filter = new ComplaintFilter();
        LocalDate from = LocalDate.of(2026, 11, 15);
        LocalDate to = LocalDate.of(2026, 11, 14);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> WardenComplaintServlet.setDateRange(filter, from, to));

        assertEquals("Start date must be on or before the end date.",
                exception.getErrors().get("from"));
    }
}
