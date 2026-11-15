package com.hostel.controller;

import com.hostel.exception.HostelException;
import com.hostel.model.enums.UserStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/warden/dashboard")
public final class WardenDashboardServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("stats", complaintService().wardenStats());
            request.setAttribute("categoryCounts", complaintService().categoryCounts());
            request.setAttribute("overdueComplaints", complaintService().overdueComplaints(10));
            request.setAttribute("pendingStudentCount",
                    userService().listStudents(UserStatus.PENDING).size());
            consumeFlash(request);
            forward(request, response, "warden/dashboard.jsp");
        } catch (HostelException exception) {
            handle(request, response, exception);
        }
    }
}
