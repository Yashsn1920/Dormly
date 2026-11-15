package com.hostel.controller;

import com.hostel.exception.HostelException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/student/dashboard")
public final class StudentDashboardServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            var student = getStudent(request);
            request.setAttribute("stats", complaintService().studentStats(student.getId()));
            request.setAttribute("recent", complaintService().recentForStudent(student.getId(), 5));
            request.setAttribute("latestNotices", noticeService().latest(3));
            request.setAttribute("currentUser", student);
            consumeFlash(request);
            forward(request, response, "student/dashboard.jsp");
        } catch (HostelException exception) {
            handle(request, response, exception);
        }
    }
}
