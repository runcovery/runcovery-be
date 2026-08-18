package com.likelion14.runcovery.wellness.repository;
import com.likelion14.runcovery.user.User;
import com.likelion14.runcovery.wellness.entity.SkinRecord;

import com.likelion14.runcovery.wellness.enums.SkinRecordType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SkinRecordRepository extends JpaRepository<SkinRecord, Long> {
    List<SkinRecord> findByUserAndTypeAndMeasuredDateBetweenOrderByMeasuredDateAsc(
            User user, SkinRecordType type, LocalDate start, LocalDate end);
}

