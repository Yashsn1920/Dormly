package com.hostel.controller;

import com.hostel.exception.HostelException;
import com.hostel.exception.ValidationException;
import com.hostel.model.Warden;
import com.hostel.model.enums.UserStatus;
import com.hostel.util.FlashMessage;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = {"/warden/students", "/warden/students/approve",
        "/warden/students/deactivate", "/warden/students/reactivate"})
public final class WardenStudentServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            UserStatus status = parseStatus(trim(request, "status"));
            request.setAttribute("students", userService().listStudents(status));
            request.setAttribute("selectedStatus", status);
            consumeFlash(request);
            forward(request, response, "warden/students.jsp");
        } catch (HostelException exception) {
            handle(request, response, exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int id = parseIntParam(request, "id");
            Warden warden = (Warden) getCurrentUser(request);
            switch (request.getServletPath()) {
                case "/warden/students/approve" -> {
                    userService().approve(id, warden);
                    setFlash(request, FlashMessage.Type.SUCCESS, "Student account approved.");
                    redirect(request, response, "/warden/students?status=PENDING");
                }
                case "/warden/students/deactivate" -> {
                    userService().deactivate(id, warden);
                    setFlash(request, FlashMessage.Type.SUCCESS, "Student account deactivated.");
                    redirect(request, response, "/warden/students?status=ACTIVE");
                }
                case "/warden/students/reactivate" -> {
                    userService().reactivate(id, warden);
                    setFlash(request, FlashMessage.Type.SUCCESS, "Student account reactivated.");
                    redirect(request, response, "/warden/students?status=INACTIVE");
                }
                default -> response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        } catch (HostelException exception) {
            handle(request, response, exception);
        }
    }

    private static UserStatus parseStatus(String value) {
        if (value == null || value.isBlank()) {
            return UserStatus.PENDING;
        }
        try {
            return UserStatus.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new ValidationException(java.util.Map.of("status", "Choose a valid account status."));
        }
    }
}
