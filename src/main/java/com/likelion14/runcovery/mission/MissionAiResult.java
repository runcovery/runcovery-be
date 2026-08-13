package com.likelion14.runcovery.mission;

public record MissionAiResult(
        String recommendedIntensity,
        String recommendedTime,
        String recommendedZone,
        String recommendedZoneDesc,
        String detailComment,
        Boolean isRest
) {}
