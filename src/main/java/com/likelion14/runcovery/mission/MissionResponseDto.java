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

    public record Status(
            String status,
            MissionResponseDto mission
    ) {
        public static Status noCondition() { // 오늘의 컨디션
            return new Status("NO_CONDITION", null);
        }

        public static Status noMission() {
            return new Status("NO_MISSION", null);
        }

        public static Status hasMission(MissionResponseDto mission) {
            return new Status("HAS_MISSION", mission);
        }
    }
}
