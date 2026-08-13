package com.likelion14.runcovery.mission;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MissionRepository extends JpaRepository<TodayMission, Long> {
    Optional<TodayMission> findByMissionDate(LocalDate missionDate);

    // 운동 완료 횟수
    List<TodayMission> findByMissionDateBetweenAndIsCompletedTrueAndIsRestFalse(LocalDate start, LocalDate end);

    // 휴식 횟수
    List<TodayMission> findByMissionDateBetweenAndIsRestTrue(LocalDate start, LocalDate end);
}
