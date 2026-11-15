package com.hostel.dao;

import com.hostel.model.Notice;

import java.util.List;
import java.util.Optional;

public interface NoticeDAO {
    Notice insert(Notice notice);

    List<Notice> findAll();

    List<Notice> findLatest(int count);

    Optional<Notice> findById(int id);

    void delete(int id);
}
