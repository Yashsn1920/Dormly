package com.hostel.dao.impl;

import com.hostel.dao.UserDAO;
import com.hostel.exception.DataAccessException;
import com.hostel.exception.NotFoundException;
import com.hostel.model.Student;
import com.hostel.model.User;
import com.hostel.model.enums.Role;
import com.hostel.model.enums.UserStatus;
import com.hostel.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class UserDAOImpl implements UserDAO {
    @Override
    public User insert(User user) {
        String sql = "INSERT INTO users (name, usn, email, phone, password_hash, role, block, room_no, status, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getName());
            statement.setString(2, user instanceof Student student ? student.getUsn() : null);
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPhone());
            statement.setString(5, user.getPasswordHash());
            statement.setString(6, user.getRole().name());
            statement.setString(7, user instanceof Student student ? student.getBlock() : null);
            statement.setString(8, user instanceof Student student ? student.getRoomNo() : null);
            statement.setString(9, user.getStatus().name());
            statement.setTimestamp(10, Timestamp.valueOf(user.getCreatedAt()));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("User insert did not return a generated key.");
                }
                user.setId(keys.getInt(1));
            }
            return user;
        } catch (SQLException exception) {
            throw new DataAccessException("Could not save user.", exception);
        }
    }

    @Override
    public Optional<User> findById(int id) {
        return findOne("SELECT * FROM users WHERE id = ?", statement -> statement.setInt(1, id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return findOne("SELECT * FROM users WHERE email = ?", statement -> statement.setString(1, email));
    }

    @Override
    public Optional<User> findByUsn(String usn) {
        return findOne("SELECT * FROM users WHERE usn = ?", statement -> statement.setString(1, usn));
    }

    @Override
    public List<User> findByStatus(Role role, UserStatus status) {
        String sql = "SELECT * FROM users WHERE role = ? AND status = ? ORDER BY created_at DESC";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, role.name());
            statement.setString(2, status.name());
            try (ResultSet resultSet = statement.executeQuery()) {
                List<User> users = new ArrayList<>();
                while (resultSet.next()) {
                    users.add(UserFactory.fromResultSet(resultSet));
                }
                return List.copyOf(users);
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not list users.", exception);
        }
    }

    @Override
    public void updateStatus(int id, UserStatus status) {
        String sql = "UPDATE users SET status = ? WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setInt(2, id);
            requireUpdated(statement.executeUpdate(), "User");
        } catch (SQLException exception) {
            throw new DataAccessException("Could not update user status.", exception);
        }
    }

    @Override
    public void updateProfile(User user) {
        String sql = user instanceof Student
                ? "UPDATE users SET name = ?, phone = ?, block = ?, room_no = ? WHERE id = ?"
                : "UPDATE users SET name = ?, phone = ? WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getPhone());
            if (user instanceof Student student) {
                statement.setString(3, student.getBlock());
                statement.setString(4, student.getRoomNo());
                statement.setInt(5, user.getId());
            } else {
                statement.setInt(3, user.getId());
            }
            requireUpdated(statement.executeUpdate(), "User");
        } catch (SQLException exception) {
            throw new DataAccessException("Could not update user profile.", exception);
        }
    }

    @Override
    public void updatePassword(int id, String passwordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, passwordHash);
            statement.setInt(2, id);
            requireUpdated(statement.executeUpdate(), "User");
        } catch (SQLException exception) {
            throw new DataAccessException("Could not update password.", exception);
        }
    }

    @Override
    public boolean existsWardenAccount() {
        String sql = "SELECT 1 FROM users WHERE role = ? LIMIT 1";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, Role.WARDEN.name());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not check for a warden account.", exception);
        }
    }

    private Optional<User> findOne(String sql, StatementBinder binder) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            binder.bind(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(UserFactory.fromResultSet(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not find user.", exception);
        }
    }

    private static void requireUpdated(int count, String entity) {
        if (count == 0) {
            throw new NotFoundException(entity + " was not found.");
        }
    }

    @FunctionalInterface
    private interface StatementBinder {
        void bind(PreparedStatement statement) throws SQLException;
    }
}
