package com.hostel.controller;

import com.hostel.exception.DuplicateComplaintException;
import com.hostel.exception.HostelException;
import com.hostel.exception.ValidationException;
import com.hostel.model.Student;
import com.hostel.model.enums.Category;
import com.hostel.model.enums.ComplaintStatus;
import com.hostel.util.FileStorage;
import com.hostel.util.FlashMessage;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet(urlPatterns = {"/student/complaints/new", "/student/complaints",
        "/student/complaints/view", "/student/complaints/confirm",
        "/student/complaints/reopen", "/student/complaints/feedback"})
@MultipartConfig(fileSizeThreshold = 0, maxFileSize = 2L * 1024 * 1024,
        maxRequestSize = 2L * 1024 * 1024 + 64 * 1024)
public final class StudentComplaintServlet extends BaseServlet {
    private FileStorage fileStorage;

    @Override
    public void init() throws ServletException {
        super.init();
        fileStorage = new FileStorage();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String path = request.getServletPath();
            Student student = getStudent(request);
            if (path.endsWith("/new")) {
                request.setAttribute("categories", Category.values());
                forward(request, response, "student/raise-complaint.jsp");
            } else if (path.endsWith("/view")) {
                showDetail(request, response, student);
            } else {
                String statusValue = trim(request, "status");
                ComplaintStatus status = parseStatus(statusValue);
                request.setAttribute("complaints", complaintService().listForStudent(student.getId(), status));
                request.setAttribute("statuses", ComplaintStatus.values());
                request.setAttribute("selectedStatus", status);
                consumeFlash(request);
                forward(request, response, "student/my-complaints.jsp");
            }
        } catch (HostelException exception) {
            handle(request, response, exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        try {
            Student student = getStudent(request);
            switch (path) {
                case "/student/complaints" -> createComplaint(request, response, student);
                case "/student/complaints/confirm" -> {
                    int id = parseIntParam(request, "id");
                    complaintService().studentConfirm(id, student);
                    setFlash(request, FlashMessage.Type.SUCCESS, "Resolution confirmed.");
                    redirect(request, response, "/student/complaints/view?id=" + id);
                }
                case "/student/complaints/reopen" -> {
                    int id = parseIntParam(request, "id");
                    complaintService().studentReopen(id, trim(request, "remark"), student);
                    setFlash(request, FlashMessage.Type.SUCCESS, "The complaint has been reopened.");
                    redirect(request, response, "/student/complaints/view?id=" + id);
                }
                case "/student/complaints/feedback" -> {
                    int id = parseIntParam(request, "id");
                    int rating = parseIntParam(request, "rating");
                    complaintService().submitFeedback(id, rating, trim(request, "comment"), student);
                    setFlash(request, FlashMessage.Type.SUCCESS, "Your feedback has been submitted.");
                    redirect(request, response, "/student/complaints/view?id=" + id);
                }
                default -> response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        } catch (HostelException exception) {
            handle(request, response, exception);
        } catch (IllegalStateException exception) {
            response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE,
                    "The uploaded request exceeds the allowed size.");
        }
    }

    private void createComplaint(HttpServletRequest request, HttpServletResponse response, Student student)
            throws ServletException, IOException {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("category", trim(request, "category"));
        form.put("title", trim(request, "title"));
        form.put("description", trim(request, "description"));
        String imagePath = null;
        boolean saved = false;
        try {
            Category category = parseCategory(form.get("category"));
            Part image = request.getPart("image");
            imagePath = fileStorage.save(image);
            saved = imagePath != null;
            boolean confirmDuplicate = "true".equalsIgnoreCase(trim(request, "confirmDuplicate"));
            var complaint = complaintService().raiseComplaint(student, category, form.get("title"),
                    form.get("description"), imagePath, confirmDuplicate);
            saved = false;
            setFlash(request, FlashMessage.Type.SUCCESS, "Your complaint has been submitted.");
            redirect(request, response, "/student/complaints/view?id=" + complaint.getId());
        } catch (DuplicateComplaintException exception) {
            deleteStoredImage(imagePath);
            request.setAttribute("errors", Map.of());
            request.setAttribute("form", form);
            request.setAttribute("categories", Category.values());
            request.setAttribute("duplicateWarning", exception.getExistingComplaint());
            forward(request, response, "student/raise-complaint.jsp");
        } catch (ValidationException exception) {
            deleteStoredImage(imagePath);
            request.setAttribute("errors", exception.getErrors());
            request.setAttribute("form", form);
            request.setAttribute("categories", Category.values());
            forward(request, response, "student/raise-complaint.jsp");
        } catch (HostelException exception) {
            deleteStoredImage(imagePath);
            handle(request, response, exception);
        } finally {
            if (saved && response.isCommitted()) {
                deleteStoredImage(imagePath);
            }
        }
    }

    private void showDetail(HttpServletRequest request, HttpServletResponse response, Student student)
            throws ServletException, IOException {
        int id = parseIntParam(request, "id");
        var complaint = complaintService().getForStudent(id, student);
        request.setAttribute("complaint", complaint);
        request.setAttribute("timeline", complaint.getTimeline());
        request.setAttribute("canConfirm", complaint.canBeConfirmedByStudent());
        request.setAttribute("canReopen", complaint.canBeReopenedByStudent());
        request.setAttribute("canRate", complaint.canBeRated());
        request.setAttribute("overdue", complaint.isOverdue(LocalDateTime.now()));
        consumeFlash(request);
        forward(request, response, "student/complaint-detail.jsp");
    }

    private static Category parseCategory(String value) {
        try {
            return Category.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new ValidationException(Map.of("category", "Choose a valid complaint category."));
        }
    }

    private static ComplaintStatus parseStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return ComplaintStatus.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new ValidationException(Map.of("status", "Choose a valid complaint status."));
        }
    }

    private void deleteStoredImage(String imagePath) {
        if (imagePath != null) {
            fileStorage.delete(imagePath);
        }
    }
}
