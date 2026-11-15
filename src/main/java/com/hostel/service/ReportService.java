package com.hostel.service;

import com.hostel.dao.ComplaintDAO;
import com.hostel.model.enums.ComplaintStatus;
import com.hostel.report.CategoryStat;
import com.hostel.report.MonthlyReport;
import com.hostel.report.MonthlyStat;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ReportService {
    private final ComplaintDAO complaintDAO;

    public ReportService(ComplaintDAO complaintDAO) {
        this.complaintDAO = Objects.requireNonNull(complaintDAO, "complaintDAO cannot be null");
    }

    public MonthlyReport reportFor(YearMonth month) {
        YearMonth selectedMonth = Objects.requireNonNull(month, "month cannot be null");
        LocalDateTime from = selectedMonth.atDay(1).atStartOfDay();
        LocalDateTime to = selectedMonth.plusMonths(1).atDay(1).atStartOfDay();

        Map<ComplaintStatus, Long> counts = new EnumMap<>(ComplaintStatus.class);
        for (ComplaintStatus status : ComplaintStatus.values()) {
            counts.put(status, 0L);
        }
        counts.putAll(complaintDAO.countByStatus(from, to));

        return new MonthlyReport(selectedMonth,
                complaintDAO.countTotal(from, to),
                counts,
                complaintDAO.categoryStatistics(from, to),
                complaintDAO.averageRating(from, to),
                complaintDAO.averageResolutionHours(from, to));
    }

    public List<MonthlyStat> lastSixMonths(YearMonth currentMonth) {
        YearMonth endMonth = Objects.requireNonNull(currentMonth, "currentMonth cannot be null");
        YearMonth startMonth = endMonth.minusMonths(5);
        List<MonthlyStat> stored = complaintDAO.countByMonth(
                startMonth.atDay(1).atStartOfDay(),
                endMonth.plusMonths(1).atDay(1).atStartOfDay());
        Map<YearMonth, Long> counts = new java.util.HashMap<>();
        for (MonthlyStat stat : stored) {
            counts.put(stat.getYearMonth(), stat.getCount());
        }
        List<MonthlyStat> complete = new ArrayList<>(6);
        for (int offset = 0; offset < 6; offset++) {
            YearMonth month = startMonth.plusMonths(offset);
            complete.add(new MonthlyStat(month, counts.getOrDefault(month, 0L)));
        }
        return List.copyOf(complete);
    }

    public List<CategoryStat> categoryStatistics(YearMonth month) {
        YearMonth selectedMonth = Objects.requireNonNull(month, "month cannot be null");
        return complaintDAO.categoryStatistics(selectedMonth.atDay(1).atStartOfDay(),
                selectedMonth.plusMonths(1).atDay(1).atStartOfDay());
    }
}
