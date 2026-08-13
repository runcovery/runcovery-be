package com.likelion14.runcovery.wellness;

import org.springframework.data.repository.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SkinRecordQueryRepository extends Repository<SkinRecord, Long> {

    List<SkinRecord> findAllByUser_IdAndMeasuredDateOrderByIdAsc(
            Long memberId,
            LocalDate measuredDate
    );

    Optional<SkinRecord> findFirstByUser_IdAndTypeAndMeasuredDateOrderByIdDesc(
            Long memberId,
            SkinRecordType type,
            LocalDate measuredDate
    );
}
