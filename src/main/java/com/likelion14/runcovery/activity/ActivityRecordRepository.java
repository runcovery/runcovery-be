package com.likelion14.runcovery.activity;

import org.springframework.data.jpa.repository.JpaRepository;
import com.likelion14.runcovery.user.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface ActivityRecordRepository extends JpaRepository<ActivityRecord, Long> {
    Optional<ActivityRecord> findByUserAndRecordDate(User user, LocalDate recordDate);

    // 마지막 운동일
    Optional<ActivityRecord> findTopByUserOrderByRecordDateDesc(User user);

    // 최근 10회 러닝 기록 (최대 거리 계산용)
    List<ActivityRecord> findTop10ByUserOrderByRecordDateDesc(User user);
}
