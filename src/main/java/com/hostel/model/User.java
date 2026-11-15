package com.hostel.model;

import com.hostel.model.enums.Role;
import com.hostel.model.enums.UserStatus;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public abstract class User implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String email;
    private String phone;
    private String passwordHash;
    private final Role role;
    private UserStatus status;
    private LocalDateTime createdAt;

    protected User(int id, String name, String email, String phone, String passwordHash,
                   Role role, UserStatus status, LocalDateTime createdAt) {
        setId(id);
        setName(name);
        setEmail(email);
        setPhone(phone);
        setPasswordHash(passwordHash);
        this.role = Objects.requireNonNull(role, "role cannot be null");
        setStatus(status);
        setCreatedAt(createdAt);
    }

    public abstract String getDashboardPath();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("id cannot be negative");
        }
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = requireText(name, "name");
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = requireText(email, "email");
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = requireText(phone, "phone");
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = requireText(passwordHash, "passwordHash");
    }

    public Role getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = Objects.requireNonNull(status, "status cannot be null");
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
    }

    protected static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be blank");
        }
        return value.trim();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + id + ", name='" + name
                + "', email='" + email + "', role=" + role + ", status=" + status + '}';
    }
}
