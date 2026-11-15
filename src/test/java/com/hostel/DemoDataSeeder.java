package com.hostel;

import com.hostel.dao.ComplaintDAO;
import com.hostel.dao.UserDAO;
import com.hostel.dao.impl.ComplaintDAOImpl;
import com.hostel.dao.impl.UserDAOImpl;
import com.hostel.exception.NotFoundException;
import com.hostel.model.Complaint;
import com.hostel.model.Student;
import com.hostel.model.User;
import com.hostel.model.Warden;
import com.hostel.model.enums.Category;
import com.hostel.model.enums.ComplaintStatus;
import com.hostel.model.enums.Priority;
import com.hostel.model.enums.Role;
import com.hostel.model.enums.UserStatus;
import com.hostel.service.CategoryPriorityPolicy;
import com.hostel.service.PriorityPolicy;
import com.hostel.util.DBConnection;
import com.hostel.util.PasswordUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class DemoDataSeeder {
    private DemoDataSeeder() {
    }

    public static void main(String[] args) {
        UserDAO userDAO = new UserDAOImpl();
        ComplaintDAO complaintDAO = new ComplaintDAOImpl();
        Warden warden = userDAO.findByEmail(DBConnection.getAppProperty("warden.email"))
                .filter(Warden.class::isInstance)
                .map(Warden.class::cast)
                .orElseThrow(() -> new NotFoundException(
                        "Warden account is missing; start the application once before seeding."));

        List<Student> students = seedStudents(userDAO);
        PriorityPolicy priorityPolicy = new CategoryPriorityPolicy();
        int created = 0;
        for (int index = 0; index < 40; index++) {
            Student student = students.get(index % students.size());
            Category category = Category.values()[index % Category.values().length];
            LocalDateTime createdAt = LocalDateTime.now().minusDays(index % 18).minusHours(index % 12);
            Priority priority = priorityPolicy.priorityFor(category);
            Complaint complaint = new Complaint(0, student.getId(), category,
                    titles()[index % titles().length] + " " + (index + 1),
                    "Demo complaint description for " + category.getDisplayName() + ".",
                    priority, createdAt);
            complaintDAO.insert(complaint);
            applyLifecycle(complaintDAO, complaint, student, warden, index);
            created++;
        }
        System.out.println("Seeded " + students.size() + " students and " + created
                + " complaints. Rerunning creates another complaint set.");
    }

    private static List<Student> seedStudents(UserDAO userDAO) {
        List<Student> students = new ArrayList<>();
        for (int index = 1; index <= 10; index++) {
            String email = "demo.student" + index + "@example.test";
            User existing = userDAO.findByEmail(email).orElse(null);
            if (existing instanceof Student student) {
                if (student.getStatus() != UserStatus.ACTIVE) {
                    userDAO.updateStatus(student.getId(), UserStatus.ACTIVE);
                    student.setStatus(UserStatus.ACTIVE);
                }
                students.add(student);
                continue;
            }
            Student student = new Student(0, "Demo Student " + index, email,
                    String.format("%010d", 9876500000L + index),
                    PasswordUtil.hash("DemoStudent" + index + "Pass9"),
                    UserStatus.ACTIVE, LocalDateTime.now().minusMonths(index),
                    String.format("1RV23CS%03d", index), index <= 5 ? "A" : "B",
                    String.valueOf(200 + index));
            students.add((Student) userDAO.insert(student));
        }
        return List.copyOf(students);
    }

    private static void applyLifecycle(ComplaintDAO dao, Complaint complaint, Student student,
                                       Warden warden, int index) {
        switch (index % 6) {
            case 0 -> {
                dao.updateStatus(complaint.getId(), ComplaintStatus.IN_PROGRESS, "Work started.", warden.getId());
                dao.updateStatus(complaint.getId(), ComplaintStatus.RESOLVED, "Issue repaired.", warden.getId());
                dao.updateStatus(complaint.getId(), ComplaintStatus.CLOSED, null, student.getId());
                dao.saveFeedback(complaint.getId(), index % 5 + 1, "Demo feedback.");
            }
            case 1 -> dao.updateStatus(complaint.getId(), ComplaintStatus.REJECTED,
                    "Demo rejection reason.", warden.getId());
            case 2 -> dao.updateStatus(complaint.getId(), ComplaintStatus.IN_PROGRESS,
                    "Assigned for inspection.", warden.getId());
            case 3 -> {
                dao.updateStatus(complaint.getId(), ComplaintStatus.IN_PROGRESS, "Work started.", warden.getId());
                dao.updateStatus(complaint.getId(), ComplaintStatus.RESOLVED, "Issue repaired.", warden.getId());
            }
            case 4 -> {
                dao.updateStatus(complaint.getId(), ComplaintStatus.IN_PROGRESS, "Work started.", warden.getId());
                dao.updateStatus(complaint.getId(), ComplaintStatus.RESOLVED, "Issue repaired.", warden.getId());
                dao.updateStatus(complaint.getId(), ComplaintStatus.REOPENED, "The issue returned.", student.getId());
            }
            default -> {
            }
        }
    }

    private static String[] titles() {
        return new String[]{"Repair requested", "Inspection needed", "Follow-up required",
                "Maintenance issue", "Service request", "Safety check"};
    }
}
