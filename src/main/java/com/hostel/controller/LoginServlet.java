package com.hostel.controller;

import com.hostel.exception.AuthenticationException;
import com.hostel.exception.HostelException;
import com.hostel.model.User;
import com.hostel.util.FlashMessage;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/login")
public final class LoginServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = getCurrentUser(request);
        if (user != null) {
            redirect(request, response, user.getDashboardPath());
            return;
        }
        consumeFlash(request);
        forward(request, response, "auth/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = trim(request, "email");
        String password = request.getParameter("password");
        try {
            User user = authService().login(email, password);
            if (request.getSession(false) != null) {
                request.getSession(false).invalidate();
            }
            request.getSession(true).setAttribute("currentUser", user);
            redirect(request, response, user.getDashboardPath());
        } catch (AuthenticationException exception) {
            request.setAttribute("error", exception.getMessage());
            request.setAttribute("email", email);
            forward(request, response, "auth/login.jsp");
        } catch (HostelException exception) {
            handle(request, response, exception);
        }
    }
}
