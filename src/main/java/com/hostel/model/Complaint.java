package com.hostel.model;

import com.hostel.model.enums.Category;
import com.hostel.model.enums.ComplaintStatus;
import com.hostel.model.enums.Priority;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Complaint {
    private int id;
    private int studentId;
    private Category category;
    private String title;
    private String description;
    private Priority priority;
    private ComplaintStatus status;
    private String imagePath;
    private String assignedTo;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private Integer rating;
    private String feedback;
    private Student student;
    private List<ComplaintUpdate> timeline;

    public Complaint(int id, int studentId, Category category, String title, String description,
                     Priority priority, ComplaintStatus status, String imagePath, String assignedTo,
                     LocalDateTime createdAt, LocalDateTime resolvedAt, Integer rating, String feedback,
                     Student student, List<ComplaintUpdate> timeline) {
        setId(id);
        setStudentId(studentId);
        setCategory(category);
        setTitle(title);
        setDescription(description);
        setPriority(priority);
        this.status = Objects.requireNonNull(status, "status cannot be null");
        this.imagePath = imagePath;
        this.assignedTo = assignedTo;
        setCreatedAt(createdAt);
        this.resolvedAt = resolvedAt;
        setRating(rating);
        this.feedback = feedback;
        this.student = student;
        setTimeline(timeline);
    }

    public Complaint(int id, int studentId, Category category, String title, String description,
                     Priority priority, LocalDateTime createdAt) {
        this(id, studentId, category, title, description, priority, ComplaintStatus.SUBMITTED,
                null, null, createdAt, null, null, null, null, List.of());
    }

    public boolean isOverdue(LocalDateTime now) {
        Objects.requireNonNull(now, "now cannot be null");
        return status.isOpen() && now.isAfter(getDueAt());
    }

    public boolean isOverdue() {
        return isOverdue(LocalDateTime.now());
    }

    public boolean canBeConfirmedByStudent() {
        return status == ComplaintStatus.RESOLVED;
    }

    public boolean canBeReopenedByStudent() {
        return status == ComplaintStatus.RESOLVED;
    }

    public boolean canBeRated() {
        return status == ComplaintStatus.CLOSED && rating == null;
    }

    public LocalDateTime getDueAt() {
        return createdAt.plusDays(priority.getSlaDays());
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

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        if (studentId < 0) {
            throw new IllegalArgumentException("studentId cannot be negative");
        }
        this.studentId = studentId;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("category cannot be null");
        }
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = requireText(title, "title");
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = requireText(description, "description");
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        if (priority == null) {
            throw new IllegalArgumentException("priority cannot be null");
        }
        this.priority = priority;
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        Objects.requireNonNull(status, "status cannot be null");
        if (this.status != status && !this.status.canTransitionTo(status)) {
            throw new IllegalArgumentException("Illegal complaint status transition: " + this.status + " -> " + status);
        }
        this.status = status;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new IllegalArgumentException("rating must be between 1 and 5");
        }
        this.rating = rating;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public List<ComplaintUpdate> getTimeline() {
        return Collections.unmodifiableList(timeline);
    }

    public void setTimeline(List<ComplaintUpdate> timeline) {
        this.timeline = timeline == null ? new ArrayList<>() : new ArrayList<>(timeline);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
        return value.trim();
    }

    @Override
    public String toString() {
        return "Complaint{id=" + id + ", studentId=" + studentId + ", category=" + category
                + ", title='" + title + "', priority=" + priority + ", status=" + status + '}';
    }
}
