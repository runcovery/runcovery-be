package com.likelion14.runcovery.wellness.repository;

import com.likelion14.runcovery.user.User;
import com.likelion14.runcovery.wellness.entity.Prescription;
import com.likelion14.runcovery.wellness.enums.PrescriptionCategory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import com.likelion14.runcovery.wellness.enums.PrescriptionCategory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
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

    Optional<Prescription> findByWellnessReport_IdAndWellnessReport_ActivityRecord_User_IdAndCategory(
            Long reportId,
            Long userId,
            PrescriptionCategory category
    );
    List<Prescription> findByPrescriptionDate(LocalDate prescriptionDate);


    // 유저별 처방전 조회
    List<Prescription> findBySkinRecordUserAndPrescriptionDate(User user, LocalDate prescriptionDate);

    // 유저의 기간별 카테고리 처방전 수 조회
    int countBySkinRecordUserAndPrescriptionDateBetweenAndCategory(User user, LocalDate start, LocalDate end, PrescriptionCategory category);

    // 유저의 기간별 카테고리 완료된 처방전 수 조회
    int countBySkinRecordUserAndPrescriptionDateBetweenAndCategoryAndIsCompletedTrue(User user, LocalDate start, LocalDate end, PrescriptionCategory category);

    // 유저의 기간별 카테고리 처방전 목록 조회
    List<Prescription> findBySkinRecordUserAndPrescriptionDateBetweenAndCategory(User user, LocalDate start, LocalDate end, PrescriptionCategory category);
}

