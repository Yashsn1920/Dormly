package com.hostel.service;

import com.hostel.dao.NoticeDAO;
import com.hostel.exception.AccessDeniedException;
import com.hostel.model.Notice;
import com.hostel.model.Warden;
import com.hostel.model.enums.UserStatus;
import com.hostel.util.Validator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public final class NoticeService {
    private final NoticeDAO noticeDAO;

    public NoticeService(NoticeDAO noticeDAO) {
        this.noticeDAO = Objects.requireNonNull(noticeDAO, "noticeDAO cannot be null");
    }

    public Notice post(String title, String message, Warden warden) {
        requireActiveWarden(warden);
        new Validator().required("title", title, "Title").maxLength("title", title, 150, "Title")
                .required("message", message, "Message").maxLength("message", message, 10_000, "Message")
                .throwIfInvalid();
        return noticeDAO.insert(new Notice(0, title, message, warden.getId(), LocalDateTime.now()));
    }

    public void delete(int noticeId, Warden warden) {
        requireActiveWarden(warden);
        noticeDAO.delete(noticeId);
    }

    public List<Notice> listAll() {
        return noticeDAO.findAll();
    }

    public List<Notice> latest(int count) {
        return noticeDAO.findLatest(count);
    }

    private static void requireActiveWarden(Warden warden) {
        if (warden == null || warden.getStatus() != UserStatus.ACTIVE) {
            throw new AccessDeniedException("An active warden account is required.");
        }
    }
}
