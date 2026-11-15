package com.hostel.util;

import com.hostel.report.CategoryStat;
import com.hostel.report.MonthlyStat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class ViewFunctions {
    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("d MMM uuuu, h:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("d MMM uuuu", Locale.ENGLISH);

    private ViewFunctions() {
    }

    public static String dateTime(LocalDateTime value) {
        if (value == null) {
            return "-";
        }
        return DATE_TIME.format(value).replace("AM", "am").replace("PM", "pm");
    }

    public static String date(TemporalAccessor value) {
        if (value == null) {
            return "-";
        }
        if (value instanceof YearMonth yearMonth) {
            return yearMonth.format(DateTimeFormatter.ofPattern("MMM uuuu", Locale.ENGLISH));
        }
        if (value instanceof LocalDate localDate) {
            return DATE.format(localDate);
        }
        if (value instanceof LocalDateTime localDateTime) {
            return DATE.format(localDateTime);
        }
        return DATE.format(value);
    }

    public static String hours(Double value) {
        if (value == null || !Double.isFinite(value) || value < 0) {
            return "No data";
        }
        if (value >= 48) {
            long wholeHours = Math.round(value);
            return (wholeHours / 24) + " days " + (wholeHours % 24) + " hrs";
        }
        return String.format(Locale.ENGLISH, "%.1f hrs", value);
    }

    public static String squares(Number value, int max) {
        if (value == null || max < 1) {
            return "-";
        }
        double numeric = value.doubleValue();
        if (!Double.isFinite(numeric)) {
            return "-";
        }
        int filled = (int) Math.round(numeric);
        filled = Math.max(0, Math.min(max, filled));
        return "\u25a0".repeat(filled) + "\u25a1".repeat(max - filled);
    }

    public static int percent(long part, long whole) {
        if (whole <= 0 || part <= 0) {
            return 0;
        }
        return (int) Math.max(0, Math.min(100, Math.round((double) part * 100.0 / whole)));
    }

    public static String initial(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        int codePoint = value.trim().codePointAt(0);
        return new String(Character.toChars(codePoint)).toUpperCase(Locale.ROOT);
    }

    public static long maxMonthlyCount(List<MonthlyStat> stats) {
        if (stats == null || stats.isEmpty()) {
            return 1;
        }
        return Math.max(1, stats.stream().mapToLong(MonthlyStat::getCount).max().orElse(0));
    }

    public static CategoryStat lowestRated(List<CategoryStat> stats) {
        if (stats == null) {
            return null;
        }
        return stats.stream()
                .filter(stat -> stat.getRatingCount() > 0 && stat.getAvgRating() != null)
                .min(Comparator.comparing(CategoryStat::getAvgRating))
                .orElse(null);
    }
}
