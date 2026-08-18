package com.likelion14.runcovery.mission;



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

//    public record Status(
//            String status,
//            String message,
//            MissionResponseDto mission
//    ) {
//        public static Status noCondition() {
//            return new Status("NO_CONDITION", "오늘의 컨디션 분석을 먼저 해주세요.", null);
//        }
//
//        public static Status noMission() {
//            return new Status("NO_MISSION", "아직 미션이 생성되지 않았어요.", null);
//        }
//
//        public static Status hasMission(MissionResponseDto mission) {
//            return new Status("HAS_MISSION", null, mission);
//        }
//
//        public static Status weekCompleted() {
//            return new Status("WEEK_COMPLETED", "이번주 스케줄을 모두 완료했어요!", null);
//        }
//    }
}
