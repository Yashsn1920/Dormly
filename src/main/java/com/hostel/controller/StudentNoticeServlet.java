package com.hostel.controller;

import com.hostel.exception.HostelException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/student/notices")
public final class StudentNoticeServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("notices", noticeService().listAll());
            consumeFlash(request);
            forward(request, response, "student/notices.jsp");
        } catch (HostelException exception) {
            handle(request, response, exception);
        }
    }
}
