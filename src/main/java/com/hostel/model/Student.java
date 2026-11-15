package com.hostel.model;

import com.hostel.model.enums.Role;
import com.hostel.model.enums.UserStatus;

import java.time.LocalDateTime;

public final class Student extends User {
    private String usn;
    private String block;
    private String roomNo;

    public Student(int id, String name, String email, String phone, String passwordHash,
                   UserStatus status, LocalDateTime createdAt, String usn, String block, String roomNo) {
        super(id, name, email, phone, passwordHash, Role.STUDENT, status, createdAt);
        setUsn(usn);
        setBlock(block);
        setRoomNo(roomNo);
    }

    @Override
    public String getDashboardPath() {
        return "/student/dashboard";
    }

    public String getUsn() {
        return usn;
    }

    public void setUsn(String usn) {
        this.usn = requireText(usn, "usn");
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = requireText(block, "block");
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = requireText(roomNo, "roomNo");
    }

    public String getRoomLabel() {
        return block + "-" + roomNo;
    }
}
