package com.hostel.filter;

import com.hostel.model.User;
import com.hostel.model.enums.Role;
import com.hostel.model.enums.UserStatus;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public final class AuthFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        httpResponse.setHeader("Pragma", "no-cache");
        httpResponse.setDateHeader("Expires", 0);

        String path = httpRequest.getServletPath();
        Role requiredRole = path.startsWith("/student/") ? Role.STUDENT
                : path.startsWith("/warden/") ? Role.WARDEN : null;
        User currentUser = null;
        if (httpRequest.getSession(false) != null) {
            Object value = httpRequest.getSession(false).getAttribute("currentUser");
            if (value instanceof User user) {
                currentUser = user;
            }
        }
        if (currentUser == null || currentUser.getStatus() != UserStatus.ACTIVE) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
            return;
        }
        if (requiredRole == null || currentUser.getRole() != requiredRole) {
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpRequest.setAttribute("statusCode", HttpServletResponse.SC_FORBIDDEN);
            httpRequest.setAttribute("errorMessage", "You do not have permission to view this page.");
            httpRequest.getRequestDispatcher("/WEB-INF/views/access-denied.jsp").forward(httpRequest, httpResponse);
            return;
        }
        chain.doFilter(request, response);
    }
}
