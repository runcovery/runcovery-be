package com.likelion14.runcovery.mission;

import com.likelion14.runcovery.condition.Condition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MissionRepository extends JpaRepository<Mission, Long> {
    Optional<Mission> findByMissionDate(LocalDate missionDate);

    // 운동 완료 횟수
    List<Mission> findByMissionDateBetweenAndIsCompletedTrueAndIsRestFalse(LocalDate start, LocalDate end);

    // 휴식 횟수
    List<Mission> findByMissionDateBetweenAndIsRestTrue(LocalDate start, LocalDate end);

    Optional<Mission> findByConditionAndMissionDate(Condition condition, LocalDate missionDate);

    long countByIsCompletedTrue();

    // 이번주 완료된 미션 목록 조회
    List<Mission> findByMissionDateBetweenAndIsCompletedTrue(LocalDate start, LocalDate end);

}
