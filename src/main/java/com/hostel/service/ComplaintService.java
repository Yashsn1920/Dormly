package com.hostel.service;

import com.hostel.dao.ComplaintDAO;
import com.hostel.exception.AccessDeniedException;
import com.hostel.exception.BusinessRuleException;
import com.hostel.exception.DuplicateComplaintException;
import com.hostel.exception.NotFoundException;
import com.hostel.exception.ValidationException;
import com.hostel.model.Complaint;
import com.hostel.model.Student;
import com.hostel.model.Warden;
import com.hostel.model.enums.Category;
import com.hostel.model.enums.ComplaintStatus;
import com.hostel.model.enums.Priority;
import com.hostel.model.enums.Role;
import com.hostel.model.enums.UserStatus;
import com.hostel.report.CategoryStat;
import com.hostel.report.ComplaintFilter;
import com.hostel.report.DashboardStats;
import com.hostel.util.Validator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ComplaintService {
    private final ComplaintDAO complaintDAO;
    private final PriorityPolicy priorityPolicy;

    public ComplaintService(ComplaintDAO complaintDAO, PriorityPolicy priorityPolicy) {
        this.complaintDAO = Objects.requireNonNull(complaintDAO, "complaintDAO cannot be null");
        this.priorityPolicy = Objects.requireNonNull(priorityPolicy, "priorityPolicy cannot be null");
    }

    public Complaint raiseComplaint(Student student, Category category, String title, String description,
                                    String imagePath, boolean confirmDuplicate) {
        requireActiveStudent(student);
        Validator validator = new Validator()
                .required("category", category == null ? null : category.name(), "Category")
                .required("title", title, "Title").maxLength("title", title, 150, "Title")
                .required("description", description, "Description")
                .maxLength("description", description, 10_000, "Description")
                .maxLength("image", imagePath, 255, "Image path");
        validator.throwIfInvalid();

        Complaint duplicate = complaintDAO.findOpenByRoomAndCategory(
                student.getBlock(), student.getRoomNo(), category).orElse(null);
        if (duplicate != null && !confirmDuplicate) {
            throw new DuplicateComplaintException(duplicate);
        }

        Priority priority = priorityPolicy.priorityFor(category);
        Complaint complaint = new Complaint(0, student.getId(), category, title, description,
                priority, LocalDateTime.now());
        complaint.setImagePath(imagePath);
        complaint.setStudent(student);
        return complaintDAO.insert(complaint);
    }

    public Complaint getForStudent(int complaintId, Student student) {
        requireActiveStudent(student);
        Complaint complaint = findComplaint(complaintId);
        if (complaint.getStudentId() != student.getId()) {
            throw new AccessDeniedException("You do not have access to this complaint.");
        }
        return complaint;
    }

    public Complaint getForWarden(int complaintId) {
        return findComplaint(complaintId);
    }

    public List<Complaint> listForStudent(int studentId, ComplaintStatus status) {
        ComplaintFilter filter = new ComplaintFilter();
        filter.setStudentId(studentId);
        filter.setStatus(status);
        return complaintDAO.findByFilter(filter);
    }

    public List<Complaint> recentForStudent(int studentId, int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be positive");
        }
        List<Complaint> complaints = listForStudent(studentId, null);
        return complaints.size() <= limit ? complaints : List.copyOf(complaints.subList(0, limit));
    }

    public List<Complaint> search(ComplaintFilter filter) {
        return complaintDAO.findByFilter(filter);
    }

    public void wardenUpdateStatus(int id, ComplaintStatus newStatus, String remark, Warden warden) {
        requireActiveWarden(warden);
        if (newStatus == null || newStatus == ComplaintStatus.REJECTED) {
            throw new BusinessRuleException("Choose a valid next status; rejection uses the reject action.");
        }
        new Validator().maxLength("remark", remark, 500, "Remark").throwIfInvalid();
        Complaint complaint = findComplaint(id);
        requireAllowedTransition(Role.WARDEN, complaint.getStatus(), newStatus);
        complaintDAO.updateStatus(id, newStatus, trimToNull(remark), warden.getId());
    }

    public void reject(int id, String reason, Warden warden) {
        requireActiveWarden(warden);
        new Validator().required("reason", reason, "Reason")
                .maxLength("reason", reason, 500, "Reason").throwIfInvalid();
        Complaint complaint = findComplaint(id);
        requireAllowedTransition(Role.WARDEN, complaint.getStatus(), ComplaintStatus.REJECTED);
        complaintDAO.updateStatus(id, ComplaintStatus.REJECTED, reason.trim(), warden.getId());
    }

    public void assign(int id, String assignedTo, Warden warden) {
        requireActiveWarden(warden);
        new Validator().maxLength("assignedTo", assignedTo, 100, "Assignee").throwIfInvalid();
        findComplaint(id);
        complaintDAO.updateAssignedTo(id, trimToNull(assignedTo));
    }

    public void studentConfirm(int id, Student student) {
        requireActiveStudent(student);
        Complaint complaint = getForStudent(id, student);
        requireAllowedTransition(Role.STUDENT, complaint.getStatus(), ComplaintStatus.CLOSED);
        complaintDAO.updateStatus(id, ComplaintStatus.CLOSED, null, student.getId());
    }

    public void studentReopen(int id, String remark, Student student) {
        requireActiveStudent(student);
        new Validator().required("remark", remark, "Remark").maxLength("remark", remark, 500, "Remark")
                .throwIfInvalid();
        Complaint complaint = getForStudent(id, student);
        requireAllowedTransition(Role.STUDENT, complaint.getStatus(), ComplaintStatus.REOPENED);
        complaintDAO.updateStatus(id, ComplaintStatus.REOPENED, remark.trim(), student.getId());
    }

    public void submitFeedback(int id, int rating, String comment, Student student) {
        requireActiveStudent(student);
        if (rating < 1 || rating > 5) {
            throw new ValidationException(Map.of("rating", "Rating must be between 1 and 5."));
        }
        new Validator().maxLength("comment", comment, 500, "Comment").throwIfInvalid();
        Complaint complaint = getForStudent(id, student);
        if (complaint.getStatus() != ComplaintStatus.CLOSED) {
            throw new BusinessRuleException("Feedback can be submitted only for a closed complaint.");
        }
        if (complaint.getRating() != null) {
            throw new BusinessRuleException("Feedback has already been submitted for this complaint.");
        }
        complaintDAO.saveFeedback(id, rating, trimToNull(comment));
    }

    public DashboardStats studentStats(int studentId) {
        List<Complaint> complaints = listForStudent(studentId, null);
        return statistics(complaints);
    }

    public DashboardStats wardenStats() {
        return statistics(complaintDAO.findByFilter(new ComplaintFilter()));
    }

    public List<Complaint> overdueComplaints(int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be positive");
        }
        LocalDateTime now = LocalDateTime.now();
        List<Complaint> overdue = new ArrayList<>();
        for (Complaint complaint : complaintDAO.findOpenComplaints()) {
            if (complaint.isOverdue(now)) {
                overdue.add(complaint);
            }
        }
        overdue.sort(Comparator.comparing(Complaint::getDueAt));
        return List.copyOf(overdue.subList(0, Math.min(limit, overdue.size())));
    }

    public List<CategoryStat> categoryCounts() {
        return complaintDAO.categoryStatistics();
    }

    private DashboardStats statistics(List<Complaint> complaints) {
        long pending = 0;
        long inProgress = 0;
        long resolved = 0;
        long rejected = 0;
        long overdue = 0;
        LocalDateTime now = LocalDateTime.now();
        for (Complaint complaint : complaints) {
            switch (complaint.getStatus()) {
                case SUBMITTED, REOPENED -> pending++;
                case IN_PROGRESS -> inProgress++;
                case RESOLVED, CLOSED -> resolved++;
                case REJECTED -> rejected++;
            }
            if (complaint.isOverdue(now)) {
                overdue++;
            }
        }
        return new DashboardStats(complaints.size(), pending, inProgress, resolved, rejected, overdue);
    }

    private Complaint findComplaint(int id) {
        return complaintDAO.findById(id)
                .orElseThrow(() -> new NotFoundException("Complaint was not found."));
    }

    private static void requireAllowedTransition(Role role, ComplaintStatus current, ComplaintStatus next) {
        if (!TransitionRules.canTransition(role, current, next)) {
            throw new BusinessRuleException("The complaint cannot be moved from "
                    + current.getDisplayName() + " to " + next.getDisplayName() + " by this user.");
        }
    }

    private static void requireActiveStudent(Student student) {
        if (student == null || student.getStatus() != UserStatus.ACTIVE) {
            throw new AccessDeniedException("An active student account is required.");
        }
    }

    private static void requireActiveWarden(Warden warden) {
        if (warden == null || warden.getStatus() != UserStatus.ACTIVE) {
            throw new AccessDeniedException("An active warden account is required.");
        }
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
