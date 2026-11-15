package com.hostel.util;

import com.hostel.model.enums.Category;
import com.hostel.report.CategoryStat;
import com.hostel.report.MonthlyStat;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ViewFunctionsTest {
    @Test
    void formatsDatesAndHandlesNull() {
        assertEquals("15 Nov 2026, 3:09 pm",
                ViewFunctions.dateTime(LocalDateTime.of(2026, 11, 15, 15, 9)));
        assertEquals("15 Nov 2026", ViewFunctions.date(LocalDateTime.of(2026, 11, 15, 15, 9)));
        assertEquals("-", ViewFunctions.dateTime(null));
        assertEquals("-", ViewFunctions.date(null));
    }

    @Test
    void formatsHoursAroundDayBoundaryAndNull() {
        assertEquals("0.0 hrs", ViewFunctions.hours(0.0));
        assertEquals("47.0 hrs", ViewFunctions.hours(47.0));
        assertEquals("2 days 0 hrs", ViewFunctions.hours(48.0));
        assertEquals("No data", ViewFunctions.hours(null));
    }

    @Test
    void roundsAndBoundsRatingSquares() {
        assertEquals("\u25a0\u25a0\u25a1\u25a1\u25a1", ViewFunctions.squares(2.4, 5));
        assertEquals("\u25a0\u25a0\u25a0\u25a1\u25a1", ViewFunctions.squares(2.5, 5));
        assertEquals("\u25a0\u25a0\u25a0\u25a0\u25a0", ViewFunctions.squares(8, 5));
        assertEquals("-", ViewFunctions.squares(null, 5));
    }

    @Test
    void calculatesPercentAndMonthlyMaximumSafely() {
        assertEquals(0, ViewFunctions.percent(4, 0));
        assertEquals(50, ViewFunctions.percent(1, 2));
        assertEquals(100, ViewFunctions.percent(5, 2));
        assertEquals(1, ViewFunctions.maxMonthlyCount(null));
        assertEquals(7, ViewFunctions.maxMonthlyCount(List.of(
                new MonthlyStat(YearMonth.of(2026, 1), 2),
                new MonthlyStat(YearMonth.of(2026, 2), 7))));
    }

    @Test
    void findsLowestRatedCategoryOnlyAmongRatedEntries() {
        CategoryStat unrated = new CategoryStat(Category.SECURITY, 3, null, 0, null);
        CategoryStat lower = new CategoryStat(Category.CLEANING, 5, 2.4, 3, 10.0);
        CategoryStat higher = new CategoryStat(Category.WIFI, 2, 4.1, 2, 4.0);

        assertEquals(lower, ViewFunctions.lowestRated(List.of(unrated, higher, lower)));
        assertNull(ViewFunctions.lowestRated(List.of(unrated)));
        assertNull(ViewFunctions.lowestRated(null));
    }
}
