package com.hostel.report;

import com.hostel.model.enums.Category;
import com.hostel.model.enums.ComplaintStatus;
import com.hostel.model.enums.Priority;

import java.time.LocalDate;

public final class ComplaintFilter {
    private ComplaintStatus status;
    private Category category;
    private Priority priority;
    private String block;
    private String roomNo;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String keyword;
    private Integer studentId;

    public ComplaintFilter() {
    }

    public ComplaintFilter(ComplaintStatus status, Category category, Priority priority, String block,
                          String roomNo, LocalDate fromDate, LocalDate toDate, String keyword,
                          Integer studentId) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("fromDate cannot be after toDate");
        }
        setStatus(status);
        setCategory(category);
        setPriority(priority);
        setBlock(block);
        setRoomNo(roomNo);
        setFromDate(fromDate);
        setToDate(toDate);
        setKeyword(keyword);
        setStudentId(studentId);
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = normalizeOptional(block);
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = normalizeOptional(roomNo);
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("fromDate cannot be after toDate");
        }
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        if (toDate != null && fromDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("fromDate cannot be after toDate");
        }
        this.toDate = toDate;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = normalizeOptional(keyword);
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        if (studentId != null && studentId < 0) {
            throw new IllegalArgumentException("studentId cannot be negative");
        }
        this.studentId = studentId;
    }

    private static String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
