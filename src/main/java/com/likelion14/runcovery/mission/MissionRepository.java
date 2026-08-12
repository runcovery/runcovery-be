package com.likelion14.runcovery.mission;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface MissionRepository extends JpaRepository<TodayMission, Long> {
    Optional<TodayMission> findByMissionDate(LocalDate missionDate);
}
