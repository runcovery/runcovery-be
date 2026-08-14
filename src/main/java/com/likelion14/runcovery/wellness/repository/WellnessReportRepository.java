package com.likelion14.runcovery.wellness.repository;
import com.likelion14.runcovery.wellness.entity.WellnessReport;

import org.springframework.data.jpa.repository.JpaRepository;

/** 기존 wellness_report 테이블을 담당하는 Repository입니다. */
public interface WellnessReportRepository extends JpaRepository<WellnessReport, Long> {
}
