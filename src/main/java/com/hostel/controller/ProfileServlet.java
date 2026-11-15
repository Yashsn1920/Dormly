package com.hostel.controller;

import com.hostel.exception.HostelException;
import com.hostel.exception.ValidationException;
import com.hostel.model.User;
import com.hostel.util.FlashMessage;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet(urlPatterns = {"/student/profile", "/warden/profile",
        "/student/profile/password", "/warden/profile/password"})
public final class ProfileServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = getCurrentUser(request);
        request.setAttribute("user", user);
        consumeFlash(request);
        forward(request, response, profileView(request));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = getCurrentUser(request);
        boolean passwordChange = request.getServletPath().endsWith("/password");
        Map<String, String> form = new LinkedHashMap<>();
        if (passwordChange) {
            try {
                authService().changePassword(user.getId(), request.getParameter("currentPassword"),
                        request.getParameter("newPassword"), request.getParameter("confirmPassword"));
                setFlash(request, FlashMessage.Type.SUCCESS, "Your password has been changed.");
                redirect(request, response, user.getDashboardPath().replace("/dashboard", "/profile"));
            } catch (ValidationException exception) {
                request.setAttribute("errors", exception.getErrors());
                request.setAttribute("user", user);
                forward(request, response, profileView(request));
            } catch (HostelException exception) {
                handle(request, response, exception);
            }
            return;
        }

        form.put("name", trim(request, "name"));
        form.put("phone", trim(request, "phone"));
        form.put("block", trim(request, "block"));
        form.put("roomNo", trim(request, "roomNo"));
        try {
            User updated = authService().updateProfile(user.getId(), form.get("name"), form.get("phone"),
                    form.get("block"), form.get("roomNo"));
            request.getSession().setAttribute("currentUser", updated);
            setFlash(request, FlashMessage.Type.SUCCESS, "Your profile has been updated.");
            redirect(request, response, updated.getDashboardPath().replace("/dashboard", "/profile"));
        } catch (ValidationException exception) {
            request.setAttribute("errors", exception.getErrors());
            request.setAttribute("form", form);
            request.setAttribute("user", user);
            forward(request, response, profileView(request));
        } catch (HostelException exception) {
            handle(request, response, exception);
        }
    }

    private static String profileView(HttpServletRequest request) {
        return request.getServletPath().startsWith("/student/")
                ? "student/profile.jsp" : "warden/profile.jsp";
    }
}
