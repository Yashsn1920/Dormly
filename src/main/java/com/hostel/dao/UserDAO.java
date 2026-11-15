package com.hostel.dao;

import com.hostel.model.User;
import com.hostel.model.enums.Role;
import com.hostel.model.enums.UserStatus;

import java.util.List;
import java.util.Optional;

public interface UserDAO {
    User insert(User user);

    Optional<User> findById(int id);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsn(String usn);

    List<User> findByStatus(Role role, UserStatus status);

    void updateStatus(int id, UserStatus status);

    void updateProfile(User user);

    void updatePassword(int id, String passwordHash);

    boolean existsWardenAccount();
}
