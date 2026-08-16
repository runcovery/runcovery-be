package com.likelion14.runcovery.activity;

import org.springframework.data.jpa.repository.JpaRepository;
import com.likelion14.runcovery.user.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface ActivityRecordRepository extends JpaRepository<ActivityRecord, Long> {
    Optional<ActivityRecord> findByUserAndRecordDate(User user, LocalDate recordDate);

    // 마지막 운동일
    Optional<ActivityRecord> findTopByUserOrderByRecordDateDesc(User user);

    // 최근 10회 러닝 기록 (최대 거리 계산용)
    List<ActivityRecord> findTop10ByUserOrderByRecordDateDesc(User user);

    // 주간 소모 칼로리 조회
    @Query("SELECT COALESCE(SUM(a.calories), 0) FROM ActivityRecord a WHERE a.id IN " +
            "(SELECT m.activityId FROM Mission m WHERE m.missionDate BETWEEN :start AND :end AND m.isCompleted = true)")
    int sumCaloriesByCompletedMissionsThisWeek(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
