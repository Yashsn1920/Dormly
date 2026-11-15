package com.hostel.report;

public final class DashboardStats {
    private long total;
    private long pending;
    private long inProgress;
    private long resolved;
    private long rejected;
    private long overdue;

    public DashboardStats(long total, long pending, long inProgress, long resolved, long rejected, long overdue) {
        setTotal(total);
        setPending(pending);
        setInProgress(inProgress);
        setResolved(resolved);
        setRejected(rejected);
        setOverdue(overdue);
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = requireNonNegative(total, "total");
    }

    public long getPending() {
        return pending;
    }

    public void setPending(long pending) {
        this.pending = requireNonNegative(pending, "pending");
    }

    public long getInProgress() {
        return inProgress;
    }

    public void setInProgress(long inProgress) {
        this.inProgress = requireNonNegative(inProgress, "inProgress");
    }

    public long getResolved() {
        return resolved;
    }

    public void setResolved(long resolved) {
        this.resolved = requireNonNegative(resolved, "resolved");
    }

    public long getRejected() {
        return rejected;
    }

    public void setRejected(long rejected) {
        this.rejected = requireNonNegative(rejected, "rejected");
    }

    public long getOverdue() {
        return overdue;
    }

    public void setOverdue(long overdue) {
        this.overdue = requireNonNegative(overdue, "overdue");
    }

    private static long requireNonNegative(long value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative");
        }
        return value;
    }
}
