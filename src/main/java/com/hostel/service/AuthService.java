package com.hostel.service;

import com.hostel.dao.UserDAO;
import com.hostel.exception.AuthenticationException;
import com.hostel.exception.ValidationException;
import com.hostel.model.Student;
import com.hostel.model.User;
import com.hostel.model.enums.Role;
import com.hostel.model.enums.UserStatus;
import com.hostel.util.PasswordUtil;
import com.hostel.util.Validator;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class AuthService {
    private static final String INVALID_CREDENTIALS = "Invalid email or password.";
    private final UserDAO userDAO;

    public AuthService(UserDAO userDAO) {
        this.userDAO = Objects.requireNonNull(userDAO, "userDAO cannot be null");
    }

    public Student register(String name, String usn, String email, String phone, String block,
                            String roomNo, String password, String confirmPassword) {
        Validator validator = new Validator()
                .required("name", name, "Name").maxLength("name", name, 100, "Name")
                .required("usn", usn, "USN").usn("usn", usn).maxLength("usn", usn, 20, "USN")
                .required("email", email, "Email").email("email", email).maxLength("email", email, 120, "Email")
                .required("phone", phone, "Phone").phone("phone", phone)
                .required("block", block, "Block").maxLength("block", block, 10, "Block")
                .required("roomNo", roomNo, "Room number").maxLength("roomNo", roomNo, 10, "Room number")
                .required("password", password, "Password").password("password", password)
                .required("confirmPassword", confirmPassword, "Confirm password");
        if (password != null && confirmPassword != null && !password.equals(confirmPassword)) {
            validator.custom("confirmPassword", false, "Passwords do not match.");
        }
        validator.throwIfInvalid();

        Map<String, String> duplicateErrors = new LinkedHashMap<>();
        if (userDAO.findByEmail(email.trim()).isPresent()) {
            duplicateErrors.put("email", "An account with this email already exists.");
        }
        if (userDAO.findByUsn(usn.trim()).isPresent()) {
            duplicateErrors.put("usn", "An account with this USN already exists.");
        }
        if (!duplicateErrors.isEmpty()) {
            throw new ValidationException(duplicateErrors);
        }

        Student student = new Student(0, name, email.trim(), phone.trim(),
                PasswordUtil.hash(password), UserStatus.PENDING, LocalDateTime.now(),
                usn.trim(), block.trim(), roomNo.trim());
        return (Student) userDAO.insert(student);
    }

    public User login(String email, String password) {
        if (email == null || password == null) {
            throw new AuthenticationException(INVALID_CREDENTIALS);
        }
        User user = userDAO.findByEmail(email.trim()).orElse(null);
        if (user == null || !PasswordUtil.verify(password, user.getPasswordHash())) {
            throw new AuthenticationException(INVALID_CREDENTIALS);
        }
        if (user.getStatus() == UserStatus.PENDING) {
            throw new AuthenticationException("Your account is awaiting warden approval.");
        }
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new AuthenticationException("Your account has been deactivated.");
        }
        return user;
    }

    public void changePassword(int userId, String currentPassword, String newPassword,
                               String confirmPassword) {
        User user = userDAO.findById(userId)
                .orElseThrow(() -> new AuthenticationException(INVALID_CREDENTIALS));
        if (!PasswordUtil.verify(currentPassword, user.getPasswordHash())) {
            throw new ValidationException(Map.of("currentPassword", "Current password is incorrect."));
        }
        Validator validator = new Validator().password("newPassword", newPassword)
                .required("confirmPassword", confirmPassword, "Confirm password");
        if (newPassword != null && confirmPassword != null && !newPassword.equals(confirmPassword)) {
            validator.custom("confirmPassword", false, "Passwords do not match.");
        }
        validator.throwIfInvalid();
        userDAO.updatePassword(userId, PasswordUtil.hash(newPassword));
    }

    public User updateProfile(int userId, String name, String phone, String block, String roomNo) {
        User user = userDAO.findById(userId)
                .orElseThrow(() -> new AuthenticationException(INVALID_CREDENTIALS));
        Validator validator = new Validator()
                .required("name", name, "Name").maxLength("name", name, 100, "Name")
                .required("phone", phone, "Phone").phone("phone", phone);
        if (user instanceof Student) {
            validator.required("block", block, "Block").maxLength("block", block, 10, "Block")
                    .required("roomNo", roomNo, "Room number").maxLength("roomNo", roomNo, 10, "Room number");
        }
        validator.throwIfInvalid();
        user.setName(name);
        user.setPhone(phone);
        if (user instanceof Student student) {
            student.setBlock(block);
            student.setRoomNo(roomNo);
        }
        userDAO.updateProfile(user);
        return user;
    }

    public User requireRole(User user, Role role) {
        if (user == null || user.getRole() != role || user.getStatus() != UserStatus.ACTIVE) {
            throw new AuthenticationException("An active " + role.getDisplayName().toLowerCase()
                    + " account is required.");
        }
        return user;
    }
}
