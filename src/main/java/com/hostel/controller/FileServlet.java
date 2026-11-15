package com.hostel.controller;

import com.hostel.exception.AccessDeniedException;
import com.hostel.exception.HostelException;
import com.hostel.model.Complaint;
import com.hostel.model.Student;
import com.hostel.model.User;
import com.hostel.model.enums.Role;
import com.hostel.model.enums.UserStatus;
import com.hostel.util.FileStorage;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@WebServlet("/files/complaint")
public final class FileServlet extends BaseServlet {
    private FileStorage fileStorage;

    @Override
    public void init() throws ServletException {
        super.init();
        fileStorage = new FileStorage();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = getCurrentUser(request);
        if (user == null || user.getStatus() != UserStatus.ACTIVE) {
            redirect(request, response, "/login");
            return;
        }
        try {
            int id = parseIntParam(request, "id");
            Complaint complaint;
            if (user.getRole() == Role.WARDEN) {
                complaint = complaintService().getForWarden(id);
            } else if (user instanceof Student student) {
                complaint = complaintService().getForStudent(id, student);
            } else {
                throw new AccessDeniedException("An active student or warden account is required.");
            }
            Path image = fileStorage.resolve(complaint.getImagePath());
            String filename = image.getFileName().toString();
            String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
            response.setContentType(extension.equals("png") ? "image/png" : "image/jpeg");
            response.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");
            response.setHeader("Cache-Control", "no-store");
            Files.copy(image, response.getOutputStream());
        } catch (HostelException exception) {
            handle(request, response, exception);
        }
    }
}
