package com.hostel.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Notice {
    private int id;
    private String title;
    private String message;
    private int postedBy;
    private LocalDateTime postedAt;

    public Notice(int id, String title, String message, int postedBy, LocalDateTime postedAt) {
        setId(id);
        setTitle(title);
        setMessage(message);
        setPostedBy(postedBy);
        setPostedAt(postedAt);
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = requireText(title, "title");
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = requireText(message, "message");
    }

    public int getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(int postedBy) {
        if (postedBy < 0) {
            throw new IllegalArgumentException("postedBy cannot be negative");
        }
        this.postedBy = postedBy;
    }

    public LocalDateTime getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(LocalDateTime postedAt) {
        this.postedAt = Objects.requireNonNull(postedAt, "postedAt cannot be null");
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
        return value.trim();
    }

    @Override
    public String toString() {
        return "Notice{id=" + id + ", title='" + title + "', postedBy=" + postedBy
                + ", postedAt=" + postedAt + '}';
    }
}
