package com.likelion14.runcovery.wellness;

import org.springframework.data.repository.Repository;

import java.time.LocalDate;
import java.util.List;

public interface SkinRecordQueryRepository extends Repository<SkinRecord, Long> {

    List<SkinRecord> findAllByUser_IdAndMeasuredDateOrderByIdAsc(
            Long memberId,
            LocalDate measuredDate
    );
}
