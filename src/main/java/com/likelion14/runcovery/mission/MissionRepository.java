package com.likelion14.runcovery.mission;

import com.likelion14.runcovery.condition.Condition;
import com.likelion14.runcovery.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MissionRepository extends JpaRepository<Mission, Long> {
    Optional<Mission> findByMissionDate(LocalDate missionDate);

    // 운동 완료 횟수
    List<Mission> findByMissionDateBetweenAndIsCompletedTrueAndIsRestFalse(LocalDate start, LocalDate end);

    // 휴식 횟수
    List<Mission> findByMissionDateBetweenAndIsRestTrue(LocalDate start, LocalDate end);

    Optional<Mission> findByConditionAndMissionDate(Condition condition, LocalDate missionDate);

    // 미래 목표 생성일 이후 유저의 완료된 미션 수 조회
    @Query("SELECT COUNT(m) FROM Mission m WHERE m.condition.user = :user AND m.isCompleted = true AND m.missionDate >= :startDate")
    long countByUserAndIsCompletedTrueAndMissionDateAfter(@Param("user") User user, @Param("startDate") LocalDate startDate);

    // 이번주 완료된 미션 목록 조회
    List<Mission> findByConditionUserAndMissionDateBetweenAndIsCompletedTrue(User user, LocalDate start, LocalDate end);
    // 유저와 미션 날짜로 오늘의 미션 조회
    Optional<Mission> findByConditionUserAndMissionDate(User user, LocalDate missionDate);

}
