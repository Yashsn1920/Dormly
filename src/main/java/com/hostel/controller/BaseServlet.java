package com.hostel.controller;

import com.hostel.dao.impl.ComplaintDAOImpl;
import com.hostel.dao.impl.NoticeDAOImpl;
import com.hostel.dao.impl.UserDAOImpl;
import com.hostel.exception.AccessDeniedException;
import com.hostel.exception.AuthenticationException;
import com.hostel.exception.BusinessRuleException;
import com.hostel.exception.DataAccessException;
import com.hostel.exception.HostelException;
import com.hostel.exception.NotFoundException;
import com.hostel.exception.ValidationException;
import com.hostel.model.Student;
import com.hostel.model.User;
import com.hostel.service.AuthService;
import com.hostel.service.CategoryPriorityPolicy;
import com.hostel.service.ComplaintService;
import com.hostel.service.NoticeService;
import com.hostel.service.UserService;
import com.hostel.util.FlashMessage;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;

public abstract class BaseServlet extends HttpServlet {
    private transient AuthService authService;
    private transient UserService userService;
    private transient ComplaintService complaintService;
    private transient NoticeService noticeService;

    @Override
    public void init() throws ServletException {
        UserDAOImpl userDAO = new UserDAOImpl();
        ComplaintDAOImpl complaintDAO = new ComplaintDAOImpl();
        NoticeDAOImpl noticeDAO = new NoticeDAOImpl();
        authService = new AuthService(userDAO);
        userService = new UserService(userDAO);
        complaintService = new ComplaintService(complaintDAO, new CategoryPriorityPolicy());
        noticeService = new NoticeService(noticeDAO);
    }

    protected final User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (User) session.getAttribute("currentUser");
    }

    protected final Student getStudent(HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user instanceof Student student) {
            return student;
        }
        throw new AccessDeniedException("An authenticated student account is required.");
    }

    protected final AuthService authService() {
        return authService;
    }

    protected final UserService userService() {
        return userService;
    }

    protected final ComplaintService complaintService() {
        return complaintService;
    }

    protected final NoticeService noticeService() {
        return noticeService;
    }

    protected final void forward(HttpServletRequest request, HttpServletResponse response, String view)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/" + view).forward(request, response);
    }

    protected final void redirect(HttpServletRequest request, HttpServletResponse response, String path)
            throws IOException {
        if (path == null || !path.startsWith("/") || path.startsWith("//")) {
            throw new IllegalArgumentException("Redirect path must be an application-relative path.");
        }
        response.sendRedirect(request.getContextPath() + path);
    }

    protected final void setFlash(HttpServletRequest request, FlashMessage.Type type, String message) {
        request.getSession(true).setAttribute("flash", new FlashMessage(type, message));
    }

    protected final void consumeFlash(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            request.setAttribute("flash", session.getAttribute("flash"));
            session.removeAttribute("flash");
        }
    }

    protected final int parseIntParam(HttpServletRequest request, String name) {
        String value = trim(request, name);
        try {
            int parsed = Integer.parseInt(value);
            if (parsed < 1) {
                throw new NumberFormatException("not positive");
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new ValidationException(Map.of(name, "Enter a valid positive number."));
        }
    }

    protected final String trim(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? null : value.trim();
    }

    protected final void handle(HttpServletRequest request, HttpServletResponse response,
                                HostelException exception) throws ServletException, IOException {
        consumeFlash(request);
        if (exception instanceof AccessDeniedException) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute("statusCode", HttpServletResponse.SC_FORBIDDEN);
            request.setAttribute("errorMessage", "You do not have permission to view this page.");
            forward(request, response, "access-denied.jsp");
            return;
        }
        if (exception instanceof ValidationException validationException) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("errors", validationException.getErrors());
            request.setAttribute("errorMessage", "The request contains invalid values. Review the messages below.");
            request.setAttribute("statusCode", HttpServletResponse.SC_BAD_REQUEST);
            forward(request, response, "error.jsp");
            return;
        }
        if (exception instanceof AuthenticationException) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            request.setAttribute("statusCode", HttpServletResponse.SC_UNAUTHORIZED);
            request.setAttribute("errorMessage", exception.getMessage());
            forward(request, response, "error.jsp");
            return;
        }
        if (exception instanceof NotFoundException) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            request.setAttribute("statusCode", HttpServletResponse.SC_NOT_FOUND);
            request.setAttribute("errorMessage", "The requested item was not found.");
            forward(request, response, "error.jsp");
            return;
        }
        if (exception instanceof BusinessRuleException businessException) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("statusCode", HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("errorMessage", businessException.getMessage());
            forward(request, response, "error.jsp");
            return;
        }
        if (exception instanceof DataAccessException) {
            getServletContext().log("A data access operation failed.", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            request.setAttribute("statusCode", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            request.setAttribute("errorMessage", "The request could not be completed.");
            forward(request, response, "error.jsp");
            return;
        }
        throw exception;
    }
}
