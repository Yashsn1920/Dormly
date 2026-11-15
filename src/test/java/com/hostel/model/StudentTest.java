package com.hostel.model;

import com.hostel.model.enums.UserStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StudentTest {
    @Test
    void combinesBlockAndRoomIntoRoomLabel() {
        Student student = new Student(1, "A Student", "student@example.com", "9876543210",
                "hash", UserStatus.ACTIVE, LocalDateTime.now(), "1RV23CS001", "A", "204");

        assertEquals("A-204", student.getRoomLabel());
    }
}
