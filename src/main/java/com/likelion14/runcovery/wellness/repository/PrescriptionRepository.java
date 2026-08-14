package com.likelion14.runcovery.wellness.repository;

import com.likelion14.runcovery.wellness.entity.Prescription;
import com.likelion14.runcovery.wellness.enums.PrescriptionCategory;
import org.springframework.data.repository.Repository;

import java.time.LocalDate;
import java.util.List;

public interface PrescriptionRepository extends Repository<Prescription, Long> {

    List<Prescription> findByPrescriptionDate(LocalDate prescriptionDate);

    // 이번주 카테고리별 생성된 처방전 수
    int countByPrescriptionDateBetweenAndCategory(LocalDate start, LocalDate end, PrescriptionCategory category);

    // 이번주 카테고리별 완료된 처방전 수
    int countByPrescriptionDateBetweenAndCategoryAndIsCompletedTrue(LocalDate start, LocalDate end, PrescriptionCategory category);

    // 카테고리별 처방전 목록 조회
    List<Prescription> findByPrescriptionDateBetweenAndCategory(LocalDate start, LocalDate end, PrescriptionCategory category);
}
