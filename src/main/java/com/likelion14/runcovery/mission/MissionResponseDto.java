package com.likelion14.runcovery.mission;

import lombok.Getter;

@Getter

public class MissionResponseDto {
    private final Long missionId;
    private final String recommendedIntensity;
    private final String recommendedTime;
    private final String recommendedZone;
    private final String recommendedZoneDesc;
    private final String detailComment;
    private final Boolean isRest;

    private MissionResponseDto(Long missionId, String recommendedIntensity, String recommendedTime,
                               String recommendedZone, String recommendedZoneDesc,
                               String detailComment, Boolean isRest) {
        this.missionId = missionId;
        this.recommendedIntensity = recommendedIntensity;
        this.recommendedTime = recommendedTime;
        this.recommendedZone = recommendedZone;
        this.recommendedZoneDesc = recommendedZoneDesc;
        this.detailComment = detailComment;
        this.isRest = isRest;
    }

    public static MissionResponseDto from(Mission mission) {
        return new MissionResponseDto(
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
