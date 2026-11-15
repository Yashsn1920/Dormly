package com.hostel.report;

import com.hostel.model.enums.Category;

import java.util.Objects;

public final class CategoryStat {
    private Category category;
    private long count;
    private Double avgRating;
    private long ratingCount;
    private Double avgResolutionHours;

    public CategoryStat(Category category, long count, Double avgRating,
                        long ratingCount, Double avgResolutionHours) {
        setCategory(category);
        setCount(count);
        setAvgRating(avgRating);
        setRatingCount(ratingCount);
        setAvgResolutionHours(avgResolutionHours);
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = Objects.requireNonNull(category, "category cannot be null");
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = requireNonNegative(count, "count");
    }

    public Double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(Double avgRating) {
        this.avgRating = requireAverage(avgRating, 1, 5, "avgRating");
    }

    public long getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(long ratingCount) {
        this.ratingCount = requireNonNegative(ratingCount, "ratingCount");
    }

    public Double getAvgResolutionHours() {
        return avgResolutionHours;
    }

    public void setAvgResolutionHours(Double avgResolutionHours) {
        this.avgResolutionHours = requireAverage(avgResolutionHours, 0, Double.MAX_VALUE,
                "avgResolutionHours");
    }

    private static long requireNonNegative(long value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative");
        }
        return value;
    }

    private static Double requireAverage(Double value, double min, double max, String fieldName) {
        if (value != null && (!Double.isFinite(value) || value < min || value > max)) {
            throw new IllegalArgumentException(fieldName + " is outside its valid range");
        }
        return value;
    }
}
