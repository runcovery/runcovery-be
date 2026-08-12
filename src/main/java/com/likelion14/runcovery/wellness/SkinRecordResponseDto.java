package com.likelion14.runcovery.wellness;

import java.time.LocalDate;

public record SkinRecordResponseDto(
        Long skinId,
        Long memberId,
        SkinRecordType type,
        LocalDate measuredDate,
        Integer totalScore,
        Integer redness,
        Integer oiliness,
        Integer texture,
        Integer pores,
        Integer blemishes,
        Integer hydration,
        Integer pigment,
        String skinImage
) {
    public static SkinRecordResponseDto from(SkinRecord record) {
        return new SkinRecordResponseDto(
                record.getId(),
                record.getUser().getId(),
                record.getType(),
                record.getMeasuredDate(),
                record.getTotalScore(),
                record.getRedness(),
                record.getOiliness(),
                record.getTexture(),
                record.getPores(),
                record.getBlemishes(),
                record.getHydration(),
                record.getPigment(),
                record.getSkinImage()
        );
    }
}
