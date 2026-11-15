package com.hostel.model;

import com.hostel.model.enums.Role;
import com.hostel.model.enums.UserStatus;

import java.time.LocalDateTime;

public final class Warden extends User {
    public Warden(int id, String name, String email, String phone, String passwordHash,
                  UserStatus status, LocalDateTime createdAt) {
        super(id, name, email, phone, passwordHash, Role.WARDEN, status, createdAt);
    }

    @Override
    public String getDashboardPath() {
        return "/warden/dashboard";
    }
}
