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

@WebServlet("/register")
public final class RegisterServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (getCurrentUser(request) != null) {
            redirect(request, response, getCurrentUser(request).getDashboardPath());
            return;
        }
        consumeFlash(request);
        forward(request, response, "auth/register.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (getCurrentUser(request) != null) {
            redirect(request, response, getCurrentUser(request).getDashboardPath());
            return;
        }
        Map<String, String> form = new LinkedHashMap<>();
        for (String field : new String[]{"name", "usn", "email", "phone", "block", "roomNo"}) {
            form.put(field, trim(request, field));
        }
        try {
            authService().register(form.get("name"), form.get("usn"), form.get("email"),
                    form.get("phone"), form.get("block"), form.get("roomNo"),
                    request.getParameter("password"), request.getParameter("confirmPassword"));
            setFlash(request, FlashMessage.Type.SUCCESS,
                    "Registration submitted. Your account is awaiting warden approval.");
            redirect(request, response, "/login");
        } catch (ValidationException exception) {
            request.setAttribute("errors", exception.getErrors());
            request.setAttribute("form", form);
            forward(request, response, "auth/register.jsp");
        } catch (HostelException exception) {
            handle(request, response, exception);
        }
    }
}
