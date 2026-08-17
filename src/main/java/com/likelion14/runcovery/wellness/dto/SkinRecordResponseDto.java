package com.likelion14.runcovery.wellness.dto;
import com.likelion14.runcovery.wellness.entity.SkinRecord;
import com.likelion14.runcovery.wellness.enums.SkinRecordType;

import java.time.LocalDate;

public record SkinRecordResponseDto(
        Long skinId,
        SkinRecordType type,
        LocalDate measuredDate,
        Integer totalScore,
        Integer redness,
        Integer oiliness,
        Integer texture,
        Integer pores,
        Integer blemishes,
        Integer hydration,
        Integer pigment
) {
    public static SkinRecordResponseDto from(SkinRecord record) {
        return new SkinRecordResponseDto(
                record.getId(),
                record.getType(),
                record.getMeasuredDate(),
                record.getTotalScore(),
                record.getRedness(),
                record.getOiliness(),
                record.getTexture(),
                record.getPores(),
                record.getBlemishes(),
                record.getHydration(),
                record.getPigment()
        );
    }
}

