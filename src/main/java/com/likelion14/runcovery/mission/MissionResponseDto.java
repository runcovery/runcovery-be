package com.likelion14.runcovery.mission;

import lombok.Getter;

public record MissionResponseDto(
        Long userId,
        Long missionId,
        String recommendedIntensity,
        String recommendedTime,
        String recommendedZone,
        String recommendedZoneDesc,
        String detailComment,
        Boolean isRest
) {
    public static MissionResponseDto from(Mission mission) {
        return new MissionResponseDto(
                mission.getCondition().getUser().getId(),
                mission.getId(),
                mission.getRecommendedIntensity(),
                mission.getRecommendedTime(),
                mission.getRecommendedZone(),
                mission.getRecommendedZoneDesc(),
                mission.getDetailComment(),
                mission.getIsRest()
        );
    }
}
