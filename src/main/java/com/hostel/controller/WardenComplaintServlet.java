package com.hostel.controller;

import com.hostel.exception.HostelException;
import com.hostel.exception.ValidationException;
import com.hostel.model.Warden;
import com.hostel.model.enums.Category;
import com.hostel.model.enums.ComplaintStatus;
import com.hostel.model.enums.Priority;
import com.hostel.model.enums.Role;
import com.hostel.report.ComplaintFilter;
import com.hostel.service.TransitionRules;
import com.hostel.util.FlashMessage;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/warden/complaints", "/warden/complaints/view",
        "/warden/complaints/update", "/warden/complaints/reject", "/warden/complaints/assign"})
public final class WardenComplaintServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String path = request.getServletPath();
            if (path.endsWith("/view")) {
                showDetail(request, response);
            } else {
                showList(request, response);
            }
        } catch (HostelException exception) {
            handle(request, response, exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Warden warden = (Warden) getCurrentUser(request);
            int id = parseIntParam(request, "id");
            switch (request.getServletPath()) {
                case "/warden/complaints/update" -> {
                    ComplaintStatus next = parseStatus(trim(request, "status"));
                    complaintService().wardenUpdateStatus(id, next, trim(request, "remark"), warden);
                    setFlash(request, FlashMessage.Type.SUCCESS, "Complaint status updated.");
                }
                case "/warden/complaints/reject" -> {
                    complaintService().reject(id, trim(request, "reason"), warden);
                    setFlash(request, FlashMessage.Type.SUCCESS, "Complaint rejected.");
                }
                case "/warden/complaints/assign" -> {
                    complaintService().assign(id, trim(request, "assignedTo"), warden);
                    setFlash(request, FlashMessage.Type.SUCCESS, "Complaint assignment updated.");
                }
                default -> response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
            if (!response.isCommitted()) {
                redirect(request, response, "/warden/complaints/view?id=" + id);
            }
        } catch (HostelException exception) {
            handle(request, response, exception);
        }
    }

    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ComplaintFilter filter = new ComplaintFilter();
        filter.setStatus(parseOptionalStatus(trim(request, "status")));
        filter.setCategory(parseOptionalCategory(trim(request, "category")));
        filter.setPriority(parseOptionalPriority(trim(request, "priority")));
        filter.setBlock(trim(request, "block"));
        filter.setRoomNo(trim(request, "roomNo"));
        setDateRange(filter, parseDate(trim(request, "from")), parseDate(trim(request, "to")));
        filter.setKeyword(trim(request, "q"));
        request.setAttribute("complaints", complaintService().search(filter));
        request.setAttribute("filter", filter);
        request.setAttribute("categories", Category.values());
        request.setAttribute("statuses", ComplaintStatus.values());
        request.setAttribute("priorities", Priority.values());
        consumeFlash(request);
        forward(request, response, "warden/complaints.jsp");
    }

    private void showDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id = parseIntParam(request, "id");
        var complaint = complaintService().getForWarden(id);
        List<ComplaintStatus> allowed = new ArrayList<>();
        for (ComplaintStatus status : ComplaintStatus.values()) {
            if (TransitionRules.canTransition(Role.WARDEN, complaint.getStatus(), status)) {
                allowed.add(status);
            }
        }
        request.setAttribute("complaint", complaint);
        request.setAttribute("timeline", complaint.getTimeline());
        request.setAttribute("allowedNextStatuses", List.copyOf(allowed));
        request.setAttribute("canReject", allowed.contains(ComplaintStatus.REJECTED));
        request.setAttribute("overdue", complaint.isOverdue(LocalDateTime.now()));
        consumeFlash(request);
        forward(request, response, "warden/update-complaint.jsp");
    }

    private static ComplaintStatus parseOptionalStatus(String value) {
        return value == null || value.isBlank() ? null : parseEnum(ComplaintStatus.class, value, "status");
    }

    private static Category parseOptionalCategory(String value) {
        return value == null || value.isBlank() ? null : parseEnum(Category.class, value, "category");
    }

    private static Priority parseOptionalPriority(String value) {
        return value == null || value.isBlank() ? null : parseEnum(Priority.class, value, "priority");
    }

    private static ComplaintStatus parseStatus(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(java.util.Map.of("status", "Choose a valid next status."));
        }
        return parseEnum(ComplaintStatus.class, value, "status");
    }

    private static <T extends Enum<T>> T parseEnum(Class<T> type, String value, String field) {
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException exception) {
            throw new ValidationException(java.util.Map.of(field, "Choose a valid value."));
        }
    }

    private static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new ValidationException(java.util.Map.of("date", "Enter a valid date."));
        }
    }

    static void setDateRange(ComplaintFilter filter, LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new ValidationException(Map.of("from", "Start date must be on or before the end date."));
        }
        filter.setFromDate(fromDate);
        filter.setToDate(toDate);
    }
}
