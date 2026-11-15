package com.hostel.dao;

import com.hostel.model.Complaint;
import com.hostel.model.ComplaintUpdate;
import com.hostel.model.enums.Category;
import com.hostel.model.enums.ComplaintStatus;
import com.hostel.report.CategoryStat;
import com.hostel.report.ComplaintFilter;
import com.hostel.report.MonthlyStat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ComplaintDAO {
    Complaint insert(Complaint complaint);

    Optional<Complaint> findById(int id);

    List<Complaint> findByFilter(ComplaintFilter filter);

    Optional<Complaint> findOpenByRoomAndCategory(String block, String roomNo, Category category);

    void updateStatus(int complaintId, ComplaintStatus newStatus, String remark, int actorId);

    void updateAssignedTo(int complaintId, String assignedTo);

    void saveFeedback(int id, int rating, String comment);

    List<ComplaintUpdate> findTimeline(int complaintId);

    Map<ComplaintStatus, Long> countByStatusForStudent(int studentId);

    List<Complaint> findOpenComplaints();

    Map<Category, Long> countByCategory();

    Map<Category, Double> avgRatingByCategory();

    Map<Category, Double> avgResolutionHoursByCategory();

    List<CategoryStat> categoryStatistics();

    List<MonthlyStat> countByMonth(LocalDateTime from, LocalDateTime to);

    List<CategoryStat> categoryStatistics(LocalDateTime from, LocalDateTime to);

    Map<ComplaintStatus, Long> countByStatus(LocalDateTime from, LocalDateTime to);

    long countTotal(LocalDateTime from, LocalDateTime to);

    Double averageRating(LocalDateTime from, LocalDateTime to);

    Double averageResolutionHours(LocalDateTime from, LocalDateTime to);
}
