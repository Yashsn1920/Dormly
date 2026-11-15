package com.hostel.dao.impl;

import com.hostel.dao.ComplaintDAO;
import com.hostel.exception.BusinessRuleException;
import com.hostel.exception.DataAccessException;
import com.hostel.exception.NotFoundException;
import com.hostel.model.Complaint;
import com.hostel.model.ComplaintUpdate;
import com.hostel.model.Student;
import com.hostel.model.enums.Category;
import com.hostel.model.enums.ComplaintStatus;
import com.hostel.model.enums.Priority;
import com.hostel.model.enums.UserStatus;
import com.hostel.report.CategoryStat;
import com.hostel.report.ComplaintFilter;
import com.hostel.report.MonthlyStat;
import com.hostel.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ComplaintDAOImpl implements ComplaintDAO {
    private static final String COMPLAINT_SELECT = "SELECT c.*, "
            + "u.id AS student_obj_id, u.name AS student_name, u.email AS student_email, "
            + "u.phone AS student_phone, u.password_hash AS student_password_hash, "
            + "u.status AS student_status, u.created_at AS student_created_at, "
            + "u.usn AS student_usn, u.block AS student_block, u.room_no AS student_room_no "
            + "FROM complaints c JOIN users u ON u.id = c.student_id";

    @Override
    public Complaint insert(Complaint complaint) {
        String insertComplaint = "INSERT INTO complaints "
                + "(student_id, category, title, description, priority, status, image_path, assigned_to, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String insertUpdate = "INSERT INTO complaint_updates (complaint_id, status, remark, updated_by) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try (PreparedStatement complaintStatement = connection.prepareStatement(
                    insertComplaint, Statement.RETURN_GENERATED_KEYS)) {
                complaintStatement.setInt(1, complaint.getStudentId());
                complaintStatement.setString(2, complaint.getCategory().name());
                complaintStatement.setString(3, complaint.getTitle());
                complaintStatement.setString(4, complaint.getDescription());
                complaintStatement.setString(5, complaint.getPriority().name());
                complaintStatement.setString(6, complaint.getStatus().name());
                complaintStatement.setString(7, complaint.getImagePath());
                complaintStatement.setString(8, complaint.getAssignedTo());
                complaintStatement.setTimestamp(9, Timestamp.valueOf(complaint.getCreatedAt()));
                complaintStatement.executeUpdate();
                try (ResultSet keys = complaintStatement.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("Complaint insert did not return a generated key.");
                    }
                    complaint.setId(keys.getInt(1));
                }
                try (PreparedStatement updateStatement = connection.prepareStatement(insertUpdate)) {
                    updateStatement.setInt(1, complaint.getId());
                    updateStatement.setString(2, complaint.getStatus().name());
                    updateStatement.setNull(3, Types.VARCHAR);
                    updateStatement.setInt(4, complaint.getStudentId());
                    updateStatement.executeUpdate();
                }
                connection.commit();
                return complaint;
            } catch (SQLException | RuntimeException exception) {
                rollback(connection, exception);
                throw exception;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not save complaint and initial timeline entry.", exception);
        }
    }

    @Override
    public Optional<Complaint> findById(int id) {
        String sql = COMPLAINT_SELECT + " WHERE c.id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                Complaint complaint = mapComplaint(resultSet);
                complaint.setTimeline(findTimeline(id));
                return Optional.of(complaint);
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not find complaint.", exception);
        }
    }

    @Override
    public List<Complaint> findByFilter(ComplaintFilter filter) {
        ComplaintFilter actual = filter == null ? new ComplaintFilter() : filter;
        StringBuilder sql = new StringBuilder(COMPLAINT_SELECT).append(" WHERE 1 = 1");
        List<Object> parameters = new ArrayList<>();
        if (actual.getStatus() != null) {
            sql.append(" AND c.status = ?");
            parameters.add(actual.getStatus().name());
        }
        if (actual.getCategory() != null) {
            sql.append(" AND c.category = ?");
            parameters.add(actual.getCategory().name());
        }
        if (actual.getPriority() != null) {
            sql.append(" AND c.priority = ?");
            parameters.add(actual.getPriority().name());
        }
        if (actual.getBlock() != null) {
            sql.append(" AND u.block = ?");
            parameters.add(actual.getBlock());
        }
        if (actual.getRoomNo() != null) {
            sql.append(" AND u.room_no = ?");
            parameters.add(actual.getRoomNo());
        }
        if (actual.getFromDate() != null) {
            sql.append(" AND c.created_at >= ?");
            parameters.add(Timestamp.valueOf(actual.getFromDate().atStartOfDay()));
        }
        if (actual.getToDate() != null) {
            sql.append(" AND c.created_at < ?");
            parameters.add(Timestamp.valueOf(actual.getToDate().plusDays(1).atStartOfDay()));
        }
        if (actual.getStudentId() != null) {
            sql.append(" AND c.student_id = ?");
            parameters.add(actual.getStudentId());
        }
        if (actual.getKeyword() != null) {
            sql.append(" AND (c.title LIKE ? OR c.description LIKE ? OR u.name LIKE ? OR u.usn LIKE ?)");
            String pattern = "%" + actual.getKeyword() + "%";
            parameters.add(pattern);
            parameters.add(pattern);
            parameters.add(pattern);
            parameters.add(pattern);
        }
        sql.append(" ORDER BY c.created_at DESC, c.id DESC");

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bindValues(statement, parameters);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Complaint> complaints = new ArrayList<>();
                while (resultSet.next()) {
                    complaints.add(mapComplaint(resultSet));
                }
                return List.copyOf(complaints);
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not search complaints.", exception);
        }
    }

    @Override
    public Optional<Complaint> findOpenByRoomAndCategory(String block, String roomNo, Category category) {
        String sql = COMPLAINT_SELECT + " WHERE u.block = ? AND u.room_no = ? AND c.category = ? "
                + "AND c.status IN ('SUBMITTED', 'IN_PROGRESS', 'REOPENED') "
                + "ORDER BY c.created_at DESC LIMIT 1";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, block);
            statement.setString(2, roomNo);
            statement.setString(3, category.name());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapComplaint(resultSet)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not check for an open duplicate complaint.", exception);
        }
    }

    @Override
    public void updateStatus(int complaintId, ComplaintStatus newStatus, String remark, int actorId) {
        String lockComplaint = "SELECT status FROM complaints WHERE id = ? FOR UPDATE";
        String updateComplaint = "UPDATE complaints SET status = ?, "
                + "resolved_at = CASE WHEN ? = 'RESOLVED' THEN CURRENT_TIMESTAMP "
                + "WHEN ? = 'REOPENED' THEN NULL ELSE resolved_at END WHERE id = ?";
        String insertUpdate = "INSERT INTO complaint_updates (complaint_id, status, remark, updated_by) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try (PreparedStatement lockStatement = connection.prepareStatement(lockComplaint);
                 PreparedStatement complaintStatement = connection.prepareStatement(updateComplaint);
                 PreparedStatement updateStatement = connection.prepareStatement(insertUpdate)) {
                lockStatement.setInt(1, complaintId);
                ComplaintStatus currentStatus;
                try (ResultSet resultSet = lockStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        throw new NotFoundException("Complaint was not found.");
                    }
                    currentStatus = ComplaintStatus.valueOf(resultSet.getString("status"));
                }
                if (!currentStatus.canTransitionTo(newStatus)) {
                    throw new BusinessRuleException("The complaint status changed or the transition is not allowed.");
                }
                complaintStatement.setString(1, newStatus.name());
                complaintStatement.setString(2, newStatus.name());
                complaintStatement.setString(3, newStatus.name());
                complaintStatement.setInt(4, complaintId);
                if (complaintStatement.executeUpdate() == 0) {
                    throw new NotFoundException("Complaint was not found.");
                }
                updateStatement.setInt(1, complaintId);
                updateStatement.setString(2, newStatus.name());
                updateStatement.setString(3, remark);
                updateStatement.setInt(4, actorId);
                updateStatement.executeUpdate();
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                rollback(connection, exception);
                throw exception;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not update complaint status and timeline.", exception);
        }
    }

    @Override
    public void updateAssignedTo(int complaintId, String assignedTo) {
        String sql = "UPDATE complaints SET assigned_to = ? WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, assignedTo);
            statement.setInt(2, complaintId);
            if (statement.executeUpdate() == 0) {
                throw new NotFoundException("Complaint was not found.");
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not assign complaint.", exception);
        }
    }

    @Override
    public void saveFeedback(int id, int rating, String comment) {
        String sql = "UPDATE complaints SET rating = ?, feedback = ? "
                + "WHERE id = ? AND status = 'CLOSED' AND rating IS NULL";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, rating);
            statement.setString(2, comment);
            statement.setInt(3, id);
            if (statement.executeUpdate() == 0) {
                throw new BusinessRuleException("Feedback is only accepted once for a closed complaint.");
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not save complaint feedback.", exception);
        }
    }

    @Override
    public List<ComplaintUpdate> findTimeline(int complaintId) {
        String sql = "SELECT cu.id, cu.complaint_id, cu.status, cu.remark, cu.updated_by, "
                + "u.name AS updated_by_name, cu.updated_at FROM complaint_updates cu "
                + "JOIN users u ON u.id = cu.updated_by WHERE cu.complaint_id = ? "
                + "ORDER BY cu.updated_at, cu.id";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, complaintId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<ComplaintUpdate> updates = new ArrayList<>();
                while (resultSet.next()) {
                    updates.add(new ComplaintUpdate(
                            resultSet.getInt("id"),
                            resultSet.getInt("complaint_id"),
                            ComplaintStatus.valueOf(resultSet.getString("status")),
                            resultSet.getString("remark"),
                            resultSet.getInt("updated_by"),
                            resultSet.getString("updated_by_name"),
                            UserFactory.toLocalDateTime(resultSet.getTimestamp("updated_at"))));
                }
                return List.copyOf(updates);
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not load complaint timeline.", exception);
        }
    }

    @Override
    public Map<ComplaintStatus, Long> countByStatusForStudent(int studentId) {
        String sql = "SELECT status, COUNT(*) AS status_count FROM complaints "
                + "WHERE student_id = ? GROUP BY status";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, studentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                Map<ComplaintStatus, Long> counts = new EnumMap<>(ComplaintStatus.class);
                while (resultSet.next()) {
                    counts.put(ComplaintStatus.valueOf(resultSet.getString("status")),
                            resultSet.getLong("status_count"));
                }
                return Map.copyOf(counts);
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not count student complaints.", exception);
        }
    }

    @Override
    public List<Complaint> findOpenComplaints() {
        String sql = COMPLAINT_SELECT + " WHERE c.status IN ('SUBMITTED', 'IN_PROGRESS', 'REOPENED') "
                + "ORDER BY c.created_at";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Complaint> complaints = new ArrayList<>();
            while (resultSet.next()) {
                complaints.add(mapComplaint(resultSet));
            }
            return List.copyOf(complaints);
        } catch (SQLException exception) {
            throw new DataAccessException("Could not load open complaints.", exception);
        }
    }

    @Override
    public Map<Category, Long> countByCategory() {
        String sql = "SELECT category, COUNT(*) AS category_count FROM complaints GROUP BY category";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            Map<Category, Long> counts = new EnumMap<>(Category.class);
            while (resultSet.next()) {
                counts.put(Category.valueOf(resultSet.getString("category")),
                        resultSet.getLong("category_count"));
            }
            return Map.copyOf(counts);
        } catch (SQLException exception) {
            throw new DataAccessException("Could not count complaints by category.", exception);
        }
    }

    @Override
    public Map<Category, Double> avgRatingByCategory() {
        return categoryAverage("AVG(rating)", "rating_avg");
    }

    @Override
    public Map<Category, Double> avgResolutionHoursByCategory() {
        return categoryAverage("AVG(TIMESTAMPDIFF(SECOND, created_at, resolved_at) / 3600.0)",
                "resolution_avg");
    }

    @Override
    public List<CategoryStat> categoryStatistics() {
        String sql = "SELECT category, COUNT(*) AS category_count, AVG(rating) AS avg_rating, "
                + "COUNT(rating) AS rating_count, "
                + "AVG(CASE WHEN resolved_at IS NOT NULL "
                + "THEN TIMESTAMPDIFF(SECOND, created_at, resolved_at) / 3600.0 END) AS avg_resolution "
                + "FROM complaints GROUP BY category";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            Map<Category, CategoryStat> byCategory = new EnumMap<>(Category.class);
            while (resultSet.next()) {
                Category category = Category.valueOf(resultSet.getString("category"));
                byCategory.put(category, new CategoryStat(
                        category,
                        resultSet.getLong("category_count"),
                        getNullableDouble(resultSet, "avg_rating"),
                        resultSet.getLong("rating_count"),
                        getNullableDouble(resultSet, "avg_resolution")));
            }
            List<CategoryStat> stats = new ArrayList<>();
            for (Category category : Category.values()) {
                stats.add(byCategory.getOrDefault(category,
                        new CategoryStat(category, 0, null, 0, null)));
            }
            return List.copyOf(stats);
        } catch (SQLException exception) {
            throw new DataAccessException("Could not load category statistics.", exception);
        }
    }

    @Override
    public List<MonthlyStat> countByMonth(LocalDateTime from, LocalDateTime to) {
        String sql = "SELECT DATE_FORMAT(created_at, '%Y-%m') AS year_month, COUNT(*) AS month_count "
                + "FROM complaints WHERE created_at >= ? AND created_at < ? "
                + "GROUP BY DATE_FORMAT(created_at, '%Y-%m') ORDER BY year_month";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(from));
            statement.setTimestamp(2, Timestamp.valueOf(to));
            try (ResultSet resultSet = statement.executeQuery()) {
                List<MonthlyStat> stats = new ArrayList<>();
                while (resultSet.next()) {
                    stats.add(new MonthlyStat(YearMonth.parse(resultSet.getString("year_month")),
                            resultSet.getLong("month_count")));
                }
                return List.copyOf(stats);
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not count complaints by month.", exception);
        }
    }

    @Override
    public List<CategoryStat> categoryStatistics(LocalDateTime from, LocalDateTime to) {
        String sql = "SELECT category, COUNT(*) AS category_count, AVG(rating) AS avg_rating, "
                + "COUNT(rating) AS rating_count, "
                + "AVG(CASE WHEN resolved_at IS NOT NULL "
                + "THEN TIMESTAMPDIFF(SECOND, created_at, resolved_at) / 3600.0 END) AS avg_resolution "
                + "FROM complaints WHERE created_at >= ? AND created_at < ? GROUP BY category";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(from));
            statement.setTimestamp(2, Timestamp.valueOf(to));
            try (ResultSet resultSet = statement.executeQuery()) {
                List<CategoryStat> stats = new ArrayList<>();
                while (resultSet.next()) {
                    stats.add(new CategoryStat(
                            Category.valueOf(resultSet.getString("category")),
                            resultSet.getLong("category_count"),
                            getNullableDouble(resultSet, "avg_rating"),
                            resultSet.getLong("rating_count"),
                            getNullableDouble(resultSet, "avg_resolution")));
                }
                return List.copyOf(stats);
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not load category statistics.", exception);
        }
    }

    @Override
    public Map<ComplaintStatus, Long> countByStatus(LocalDateTime from, LocalDateTime to) {
        String sql = "SELECT status, COUNT(*) AS status_count FROM complaints "
                + "WHERE created_at >= ? AND created_at < ? GROUP BY status";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(from));
            statement.setTimestamp(2, Timestamp.valueOf(to));
            try (ResultSet resultSet = statement.executeQuery()) {
                Map<ComplaintStatus, Long> counts = new EnumMap<>(ComplaintStatus.class);
                while (resultSet.next()) {
                    counts.put(ComplaintStatus.valueOf(resultSet.getString("status")),
                            resultSet.getLong("status_count"));
                }
                return Map.copyOf(counts);
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not count complaints by status.", exception);
        }
    }

    @Override
    public long countTotal(LocalDateTime from, LocalDateTime to) {
        return scalarLong("SELECT COUNT(*) FROM complaints WHERE created_at >= ? AND created_at < ?",
                from, to);
    }

    @Override
    public Double averageRating(LocalDateTime from, LocalDateTime to) {
        return scalarDouble("SELECT AVG(rating) FROM complaints WHERE created_at >= ? AND created_at < ?",
                from, to);
    }

    @Override
    public Double averageResolutionHours(LocalDateTime from, LocalDateTime to) {
        return scalarDouble("SELECT AVG(TIMESTAMPDIFF(SECOND, created_at, resolved_at) / 3600.0) "
                        + "FROM complaints WHERE created_at >= ? AND created_at < ? AND resolved_at IS NOT NULL",
                from, to);
    }

    private Map<Category, Double> categoryAverage(String expression, String column) {
        String sql = "SELECT category, " + expression + " AS " + column
                + " FROM complaints GROUP BY category";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            Map<Category, Double> averages = new EnumMap<>(Category.class);
            while (resultSet.next()) {
                Double value = getNullableDouble(resultSet, column);
                if (value != null) {
                    averages.put(Category.valueOf(resultSet.getString("category")), value);
                }
            }
            return Map.copyOf(averages);
        } catch (SQLException exception) {
            throw new DataAccessException("Could not calculate category averages.", exception);
        }
    }

    private long scalarLong(String sql, LocalDateTime from, LocalDateTime to) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(from));
            statement.setTimestamp(2, Timestamp.valueOf(to));
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not calculate complaint count.", exception);
        }
    }

    private Double scalarDouble(String sql, LocalDateTime from, LocalDateTime to) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(from));
            statement.setTimestamp(2, Timestamp.valueOf(to));
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return getNullableDouble(resultSet, 1);
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Could not calculate complaint average.", exception);
        }
    }

    private static Complaint mapComplaint(ResultSet resultSet) throws SQLException {
        Student student = null;
        int studentObjectId = resultSet.getInt("student_obj_id");
        if (!resultSet.wasNull()) {
            student = new Student(
                    studentObjectId,
                    resultSet.getString("student_name"),
                    resultSet.getString("student_email"),
                    resultSet.getString("student_phone"),
                    resultSet.getString("student_password_hash"),
                    UserStatus.valueOf(resultSet.getString("student_status")),
                    UserFactory.toLocalDateTime(resultSet.getTimestamp("student_created_at")),
                    resultSet.getString("student_usn"),
                    resultSet.getString("student_block"),
                    resultSet.getString("student_room_no"));
        }
        int ratingValue = resultSet.getInt("rating");
        Integer rating = resultSet.wasNull() ? null : ratingValue;
        return new Complaint(
                resultSet.getInt("id"),
                resultSet.getInt("student_id"),
                Category.valueOf(resultSet.getString("category")),
                resultSet.getString("title"),
                resultSet.getString("description"),
                Priority.valueOf(resultSet.getString("priority")),
                ComplaintStatus.valueOf(resultSet.getString("status")),
                resultSet.getString("image_path"),
                resultSet.getString("assigned_to"),
                UserFactory.toLocalDateTime(resultSet.getTimestamp("created_at")),
                UserFactory.toLocalDateTime(resultSet.getTimestamp("resolved_at")),
                rating,
                resultSet.getString("feedback"),
                student,
                List.of());
    }

    private static void bindValues(PreparedStatement statement, List<Object> values) throws SQLException {
        for (int index = 0; index < values.size(); index++) {
            Object value = values.get(index);
            if (value instanceof Timestamp timestamp) {
                statement.setTimestamp(index + 1, timestamp);
            } else if (value instanceof Integer integer) {
                statement.setInt(index + 1, integer);
            } else {
                statement.setString(index + 1, value.toString());
            }
        }
    }

    private static Double getNullableDouble(ResultSet resultSet, String column) throws SQLException {
        double value = resultSet.getDouble(column);
        return resultSet.wasNull() ? null : value;
    }

    private static Double getNullableDouble(ResultSet resultSet, int column) throws SQLException {
        double value = resultSet.getDouble(column);
        return resultSet.wasNull() ? null : value;
    }

    private static void rollback(Connection connection, Exception original) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            original.addSuppressed(rollbackException);
        }
    }
}
