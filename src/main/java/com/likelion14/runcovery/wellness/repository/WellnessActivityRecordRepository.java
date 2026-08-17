package com.likelion14.runcovery.wellness.repository;

import com.likelion14.runcovery.activity.ActivityRecord;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 웰니스 리포트 생성 과정에서만 사용하는 활동 기록 조회 Repository입니다.
 */
public interface WellnessActivityRecordRepository extends Repository<ActivityRecord, Long> {

    List<ActivityRecord> findAllByUser_IdAndRecordDate(Long userId, LocalDate recordDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select activity from ActivityRecord activity where activity.id = :activityId")
    Optional<ActivityRecord> findByIdForUpdate(@Param("activityId") Long activityId);
}