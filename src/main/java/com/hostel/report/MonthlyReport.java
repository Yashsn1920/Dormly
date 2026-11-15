package com.hostel.report;

import com.hostel.model.enums.ComplaintStatus;

import java.time.YearMonth;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class MonthlyReport {
    private YearMonth month;
    private long total;
    private Map<ComplaintStatus, Long> statusCounts;
    private List<CategoryStat> categoryStats;
    private Double averageRating;
    private Double averageResolutionHours;

    public MonthlyReport(YearMonth month, long total, Map<ComplaintStatus, Long> statusCounts,
                         List<CategoryStat> categoryStats, Double averageRating,
                         Double averageResolutionHours) {
        setMonth(month);
        setTotal(total);
        setStatusCounts(statusCounts);
        setCategoryStats(categoryStats);
        setAverageRating(averageRating);
        setAverageResolutionHours(averageResolutionHours);
    }

    public YearMonth getMonth() {
        return month;
    }

    public void setMonth(YearMonth month) {
        this.month = Objects.requireNonNull(month, "month cannot be null");
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        if (total < 0) {
            throw new IllegalArgumentException("total cannot be negative");
        }
        this.total = total;
    }

    public Map<ComplaintStatus, Long> getStatusCounts() {
        return statusCounts;
    }

    public void setStatusCounts(Map<ComplaintStatus, Long> statusCounts) {
        EnumMap<ComplaintStatus, Long> copy = new EnumMap<>(ComplaintStatus.class);
        if (statusCounts != null) {
            statusCounts.forEach((status, count) -> {
                Objects.requireNonNull(status, "statusCounts cannot contain a null status");
                if (count == null || count < 0) {
                    throw new IllegalArgumentException("status counts must be non-negative");
                }
                copy.put(status, count);
            });
        }
        this.statusCounts = Collections.unmodifiableMap(copy);
    }

    public List<CategoryStat> getCategoryStats() {
        return categoryStats;
    }

    public void setCategoryStats(List<CategoryStat> categoryStats) {
        this.categoryStats = categoryStats == null ? List.of() : List.copyOf(categoryStats);
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = requireAverage(averageRating, 1, 5, "averageRating");
    }

    public Double getAverageResolutionHours() {
        return averageResolutionHours;
    }

    public void setAverageResolutionHours(Double averageResolutionHours) {
        this.averageResolutionHours = requireAverage(averageResolutionHours, 0, Double.MAX_VALUE,
                "averageResolutionHours");
    }

    public long getCountForStatus(ComplaintStatus status) {
        return statusCounts.getOrDefault(Objects.requireNonNull(status, "status cannot be null"), 0L);
    }

    private static Double requireAverage(Double value, double min, double max, String fieldName) {
        if (value != null && (!Double.isFinite(value) || value < min || value > max)) {
            throw new IllegalArgumentException(fieldName + " is outside its valid range");
        }
        return value;
    }
}
