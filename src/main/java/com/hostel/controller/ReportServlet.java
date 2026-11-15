package com.hostel.controller;

import com.hostel.exception.HostelException;
import com.hostel.exception.ValidationException;
import com.hostel.service.ReportService;
import com.hostel.util.FlashMessage;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.YearMonth;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

@WebServlet("/warden/reports")
public final class ReportServlet extends BaseServlet {
    private ReportService reportService;

    @Override
    public void init() throws ServletException {
        super.init();
        reportService = new ReportService(new com.hostel.dao.impl.ComplaintDAOImpl());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            YearMonth month = parseMonth(trim(request, "month"));
            var report = reportService.reportFor(month);
            request.setAttribute("selectedMonth", month.toString());
            request.setAttribute("generatedDate", LocalDate.now());
            request.setAttribute("report", report);
            request.setAttribute("monthlyCounts", reportService.lastSixMonths(YearMonth.now()));
            request.setAttribute("categoryStats", report.getCategoryStats());
            request.setAttribute("overallAvgRating", report.getAverageRating());
            request.setAttribute("overallAvgResolutionHours", report.getAverageResolutionHours());
            consumeFlash(request);
            forward(request, response, "warden/reports.jsp");
        } catch (HostelException exception) {
            handle(request, response, exception);
        }
    }

    private static YearMonth parseMonth(String value) {
        if (value == null || value.isBlank()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(value);
        } catch (DateTimeParseException exception) {
            throw new ValidationException(Map.of("month", "Choose a valid report month."));
        }
    }
}
