package com.likelion14.runcovery.wellness.repository;

import com.likelion14.runcovery.wellness.entity.WellnessReport;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

/** wellness_report 저장 및 사용자별 리포트 조회를 담당합니다. */
public interface WellnessReportRepository extends JpaRepository<WellnessReport, Long> {

    Optional<WellnessReport> findFirstByActivityRecord_IdOrderByIdDesc(Long activityRecordId);

    @EntityGraph(attributePaths = {"activityRecord", "activityRecord.user"})
    Optional<WellnessReport> findFirstByActivityRecord_User_IdOrderByReportDateDescIdDesc(Long userId);

    @EntityGraph(attributePaths = {"activityRecord", "activityRecord.user"})
    Optional<WellnessReport> findFirstByActivityRecord_User_IdAndReportDateOrderByIdDesc(
            Long userId,
            LocalDate reportDate
    );

    @EntityGraph(attributePaths = {"activityRecord", "activityRecord.user"})
    Optional<WellnessReport> findByIdAndActivityRecord_User_Id(Long reportId, Long userId);
}