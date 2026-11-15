package com.hostel.controller;

import com.hostel.exception.HostelException;
import com.hostel.exception.ValidationException;
import com.hostel.model.Warden;
import com.hostel.util.FlashMessage;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet(urlPatterns = {"/warden/notices", "/warden/notices/delete"})
public final class WardenNoticeServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("notices", noticeService().listAll());
            consumeFlash(request);
            forward(request, response, "warden/notices.jsp");
        } catch (HostelException exception) {
            handle(request, response, exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Warden warden = (Warden) getCurrentUser(request);
        if ("/warden/notices/delete".equals(request.getServletPath())) {
            try {
                noticeService().delete(parseIntParam(request, "id"), warden);
                setFlash(request, FlashMessage.Type.SUCCESS, "Notice deleted.");
                redirect(request, response, "/warden/notices");
            } catch (HostelException exception) {
                handle(request, response, exception);
            }
            return;
        }
        try {
            noticeService().post(trim(request, "title"), trim(request, "message"), warden);
            setFlash(request, FlashMessage.Type.SUCCESS, "Notice posted.");
            redirect(request, response, "/warden/notices");
        } catch (ValidationException exception) {
            request.setAttribute("errors", exception.getErrors());
            request.setAttribute("form", Map.of(
                    "title", safe(trim(request, "title")),
                    "message", safe(trim(request, "message"))));
            request.setAttribute("notices", noticeService().listAll());
            forward(request, response, "warden/notices.jsp");
        } catch (HostelException exception) {
            handle(request, response, exception);
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
