package com.hostel.dao.impl;

import com.hostel.model.Student;
import com.hostel.model.User;
import com.hostel.model.Warden;
import com.hostel.model.enums.Role;
import com.hostel.model.enums.UserStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public final class UserFactory {
    private UserFactory() {
    }

    public static User fromResultSet(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String name = resultSet.getString("name");
        String email = resultSet.getString("email");
        String phone = resultSet.getString("phone");
        String passwordHash = resultSet.getString("password_hash");
        Role role = Role.valueOf(resultSet.getString("role"));
        UserStatus status = UserStatus.valueOf(resultSet.getString("status"));
        LocalDateTime createdAt = toLocalDateTime(resultSet.getTimestamp("created_at"));
        if (role == Role.STUDENT) {
            return new Student(id, name, email, phone, passwordHash, status, createdAt,
                    resultSet.getString("usn"), resultSet.getString("block"),
                    resultSet.getString("room_no"));
        }
        return new Warden(id, name, email, phone, passwordHash, status, createdAt);
    }

    static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
