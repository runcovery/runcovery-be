package com.likelion14.runcovery.wellness.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "저장된 웰니스 리포트 러닝 강도 조회 응답")
public class WellnessReportQueryResponseDto {

    @Schema(description = "리포트 ID. 처방전 조회 시 reportId로 사용", example = "20")
    private Long reportId;

    @Schema(description = "분석에 사용된 러닝 기록 ID", example = "8")
    private Long activityRecordId;

    @Schema(description = "리포트 날짜", example = "2026-08-17")
    private LocalDate reportDate;

    @Schema(description = "러닝 강도 점수(1~10)", example = "7", minimum = "1", maximum = "10")
    private Integer runningIntensity;

    @Schema(description = "러닝 강도 단계", example = "MODERATE", allowableValues = {"LOW", "MODERATE", "HIGH"})
    private String intensityLevel;

    @Schema(description = "데이터 기반 러닝 강도 코멘트", example = "평균 심박수와 습도를 고려하면 오늘 운동은 중강도였어요.")
    private String comment;
}