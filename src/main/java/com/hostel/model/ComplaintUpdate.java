package com.hostel.model;

import com.hostel.model.enums.ComplaintStatus;

import java.time.LocalDateTime;
import java.util.Objects;

public class ComplaintUpdate {
    private int id;
    private int complaintId;
    private ComplaintStatus status;
    private String remark;
    private int updatedBy;
    private String updatedByName;
    private LocalDateTime updatedAt;

    public ComplaintUpdate(int id, int complaintId, ComplaintStatus status, String remark,
                          int updatedBy, String updatedByName, LocalDateTime updatedAt) {
        setId(id);
        setComplaintId(complaintId);
        setStatus(status);
        this.remark = remark;
        setUpdatedBy(updatedBy);
        this.updatedByName = requireText(updatedByName, "updatedByName");
        setUpdatedAt(updatedAt);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("id cannot be negative");
        }
        this.id = id;
    }

    public int getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(int complaintId) {
        if (complaintId < 0) {
            throw new IllegalArgumentException("complaintId cannot be negative");
        }
        this.complaintId = complaintId;
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = Objects.requireNonNull(status, "status cannot be null");
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(int updatedBy) {
        if (updatedBy < 0) {
            throw new IllegalArgumentException("updatedBy cannot be negative");
        }
        this.updatedBy = updatedBy;
    }

    public String getUpdatedByName() {
        return updatedByName;
    }

    public void setUpdatedByName(String updatedByName) {
        this.updatedByName = requireText(updatedByName, "updatedByName");
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt cannot be null");
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
        return value.trim();
    }

    @Override
    public String toString() {
        return "ComplaintUpdate{id=" + id + ", complaintId=" + complaintId + ", status=" + status
                + ", updatedBy=" + updatedBy + ", updatedAt=" + updatedAt + '}';
    }
}
