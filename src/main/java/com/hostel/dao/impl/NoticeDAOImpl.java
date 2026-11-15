package com.hostel.dao.impl;

import com.hostel.dao.NoticeDAO;
import com.hostel.exception.DataAccessException;
import com.hostel.exception.NotFoundException;
import com.hostel.model.Notice;
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

public final class NoticeDAOImpl implements NoticeDAO {
    @Override
    public Notice insert(Notice notice) {
        String sql = "INSERT INTO notices (title, message, posted_by, posted_at) VALUES (?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, notice.getTitle());
            statement.setString(2, notice.getMessage());
            statement.setInt(3, notice.getPostedBy());
            statement.setTimestamp(4, Timestamp.valueOf(notice.getPostedAt()));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Notice insert did not return a generated key.");
                }
                return new Notice(keys.getInt(1), notice.getTitle(), notice.getMessage(),
                        notice.getPostedBy(), notice.getPostedAt());
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not save notice.", exception);
        }
    }

    @Override
    public List<Notice> findAll() {
        return findMany("SELECT * FROM notices ORDER BY posted_at DESC, id DESC", null);
    }

    @Override
    public List<Notice> findLatest(int count) {
        if (count < 1) {
            throw new IllegalArgumentException("count must be positive");
        }
        return findMany("SELECT * FROM notices ORDER BY posted_at DESC, id DESC LIMIT ?", count);
    }

    @Override
    public Optional<Notice> findById(int id) {
        String sql = "SELECT * FROM notices WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapNotice(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not find notice.", exception);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM notices WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            if (statement.executeUpdate() == 0) {
                throw new NotFoundException("Notice was not found.");
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not delete notice.", exception);
        }
    }

    private List<Notice> findMany(String sql, Integer limit) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (limit != null) {
                statement.setInt(1, limit);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Notice> notices = new ArrayList<>();
                while (resultSet.next()) {
                    notices.add(mapNotice(resultSet));
                }
                return List.copyOf(notices);
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not list notices.", exception);
        }
    }

    private static Notice mapNotice(ResultSet resultSet) throws SQLException {
        return new Notice(resultSet.getInt("id"), resultSet.getString("title"),
                resultSet.getString("message"), resultSet.getInt("posted_by"),
                UserFactory.toLocalDateTime(resultSet.getTimestamp("posted_at")));
    }
}
