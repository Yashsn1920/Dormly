package com.hostel.service;

import com.hostel.dao.UserDAO;
import com.hostel.exception.AccessDeniedException;
import com.hostel.exception.BusinessRuleException;
import com.hostel.exception.NotFoundException;
import com.hostel.model.Student;
import com.hostel.model.User;
import com.hostel.model.Warden;
import com.hostel.model.enums.Role;
import com.hostel.model.enums.UserStatus;

import java.util.List;
import java.util.Objects;

public final class UserService {
    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = Objects.requireNonNull(userDAO, "userDAO cannot be null");
    }

    public List<Student> listStudents(UserStatus status) {
        Objects.requireNonNull(status, "status cannot be null");
        return userDAO.findByStatus(Role.STUDENT, status).stream()
                .map(UserService::asStudent)
                .toList();
    }

    public void approve(int studentId, Warden actor) {
        requireWarden(actor);
        Student student = findStudent(studentId);
        if (student.getStatus() != UserStatus.PENDING) {
            throw new BusinessRuleException("Only a pending student can be approved.");
        }
        userDAO.updateStatus(studentId, UserStatus.ACTIVE);
    }

    public void deactivate(int studentId, Warden actor) {
        requireWarden(actor);
        Student student = findStudent(studentId);
        if (student.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessRuleException("Only an active student can be deactivated.");
        }
        userDAO.updateStatus(studentId, UserStatus.INACTIVE);
    }

    public void reactivate(int studentId, Warden actor) {
        requireWarden(actor);
        Student student = findStudent(studentId);
        if (student.getStatus() != UserStatus.INACTIVE) {
            throw new BusinessRuleException("Only an inactive student can be reactivated.");
        }
        userDAO.updateStatus(studentId, UserStatus.ACTIVE);
    }

    private Student findStudent(int studentId) {
        User user = userDAO.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student was not found."));
        if (!(user instanceof Student student)) {
            throw new NotFoundException("Student was not found.");
        }
        return student;
    }

    private static Student asStudent(User user) {
        if (user instanceof Student student) {
            return student;
        }
        throw new IllegalStateException("Student DAO query returned a non-student user.");
    }

    private static void requireWarden(Warden actor) {
        if (actor == null || actor.getStatus() != UserStatus.ACTIVE) {
            throw new AccessDeniedException("An active warden account is required.");
        }
    }
}
