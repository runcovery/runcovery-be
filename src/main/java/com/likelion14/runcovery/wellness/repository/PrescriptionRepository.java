package com.likelion14.runcovery.wellness.repository;

import com.likelion14.runcovery.wellness.entity.Prescription;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    @EntityGraph(attributePaths = {
            "wellnessReport",
            "wellnessReport.activityRecord",
            "wellnessReport.activityRecord.user",
            "skinRecord"
    })
    List<Prescription> findAllByWellnessReport_IdAndWellnessReport_ActivityRecord_User_IdOrderByCategoryAscIdAsc(
            Long reportId,
            Long userId
    );

    @EntityGraph(attributePaths = {
            "wellnessReport",
            "wellnessReport.activityRecord",
            "wellnessReport.activityRecord.user",
            "skinRecord"
    })
    Optional<Prescription> findByIdAndWellnessReport_ActivityRecord_User_Id(
            Long prescriptionId,
            Long userId
    );
}
