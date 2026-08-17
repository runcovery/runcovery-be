package com.likelion14.runcovery.wellness.dto;

import com.likelion14.runcovery.wellness.entity.SkinRecord;
import com.likelion14.runcovery.wellness.enums.SkinRecordType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "저장된 피부 스캔 점수. 내부 사용자 ID와 이미지 파일명은 노출하지 않습니다.")
public record SkinRecordResponseDto(
        @Schema(description = "피부 기록 ID", example = "1") Long skinId,
        @Schema(description = "스캔 타입", example = "AFTER_RUN", allowableValues = {"AFTER_RUN", "AFTER_CARE"}) SkinRecordType type,
        @Schema(description = "측정일", example = "2026-08-17") LocalDate measuredDate,
        @Schema(description = "7개 항목 평균 총점", example = "79") Integer totalScore,
        @Schema(description = "홍조 점수", example = "61") Integer redness,
        @Schema(description = "유분 점수", example = "76") Integer oiliness,
        @Schema(description = "피부결 점수", example = "89") Integer texture,
        @Schema(description = "모공 점수", example = "100") Integer pores,
        @Schema(description = "잡티 점수", example = "79") Integer blemishes,
        @Schema(description = "보습 점수", example = "64") Integer hydration,
        @Schema(description = "색소침착 점수", example = "85") Integer pigment
) {
    public static SkinRecordResponseDto from(SkinRecord record) {
        return new SkinRecordResponseDto(
                record.getId(), record.getType(), record.getMeasuredDate(), record.getTotalScore(),
                record.getRedness(), record.getOiliness(), record.getTexture(), record.getPores(),
                record.getBlemishes(), record.getHydration(), record.getPigment()
        );
    }
}