package com.hostel.report;

import java.time.YearMonth;
import java.util.Objects;

public final class MonthlyStat {
    private YearMonth yearMonth;
    private long count;

    public MonthlyStat(YearMonth yearMonth, long count) {
        setYearMonth(yearMonth);
        setCount(count);
    }

    public YearMonth getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(YearMonth yearMonth) {
        this.yearMonth = Objects.requireNonNull(yearMonth, "yearMonth cannot be null");
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        if (count < 0) {
            throw new IllegalArgumentException("count cannot be negative");
        }
        this.count = count;
    }
}
