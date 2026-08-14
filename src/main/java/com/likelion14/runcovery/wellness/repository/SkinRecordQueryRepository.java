package com.likelion14.runcovery.wellness.repository;
import com.likelion14.runcovery.wellness.entity.SkinRecord;
import com.likelion14.runcovery.wellness.enums.SkinRecordType;

import org.springframework.data.repository.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SkinRecordQueryRepository extends Repository<SkinRecord, Long> {

    List<SkinRecord> findAllByUser_IdAndMeasuredDateOrderByIdAsc(
            Long userId,
            LocalDate measuredDate
    );

    Optional<SkinRecord> findFirstByUser_IdAndTypeAndMeasuredDateOrderByIdDesc(
            Long userId,
            SkinRecordType type,
            LocalDate measuredDate
    );
}

