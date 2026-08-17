package com.likelion14.runcovery.wellness.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.likelion14.runcovery.wellness.enums.EnergyStatus;
import com.likelion14.runcovery.wellness.enums.FeelingStatus;
import com.likelion14.runcovery.wellness.enums.SweatStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** 이미 저장된 당일 데이터를 이용해 웰니스 리포트를 생성하는 요청 DTO입니다. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "맞춤형 웰니스 러닝 리포트 생성 요청")
public class ReportRequestDto {

    @Schema(
            description = "리포트 기준일. 컨디션, 운동 기록, AFTER_RUN 피부 기록의 날짜가 모두 같아야 합니다. 생략하면 오늘",
            example = "2026-08-17"
    )
    private LocalDate recordDate;

    @Schema(
            description = "사용할 러닝 기록 ID. 생략하면 기준일 기록 중 현재 시각과 가장 가까운 기록을 선택합니다.",
            example = "8"
    )
    private Long activityRecordId;

    @Schema(description = "운동 후 설문 세 항목", requiredMode = Schema.RequiredMode.REQUIRED)
    private SurveyData survey;

    @Builder.Default
    @Schema(
            description = "아픈 신체 부위 코드 목록. body_part의 코드이며 최대 20개입니다.",
            example = "[\"F_KNEE_L\", \"B_THIGH_R\"]"
    )
    private List<String> painPartCodes = new ArrayList<>();

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(description = "운동 후 주관적 설문")
    public static class SurveyData {

        @Schema(
                description = "오늘 러닝 느낌",
                example = "NORMAL",
                allowableValues = {"GREAT", "NORMAL", "EXHAUSTED"},
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private FeelingStatus feeling;

        @Schema(
                description = "운동 후 에너지 상태",
                example = "TIRED",
                allowableValues = {"DEPLETED", "TIRED", "ENERGETIC"},
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private EnergyStatus energy;

        @Schema(
                description = "운동 중 땀 배출 체감",
                example = "MODERATE",
                allowableValues = {"LOW", "MODERATE", "HIGH"},
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private SweatStatus sweat;
    }
}