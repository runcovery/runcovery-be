package com.likelion14.runcovery.user;


import java.util.List;

public record MyStatsResponseDto (

        String nickname,
        int totalCalories,
        int burnedCalories,
        WeeklyMissionStats weeklyMission,
        PostCareStats postCare,
        List<SkinScore> monthlySkinScore
) {
    public record WeeklyMissionStats(
            int successCount,
            List<String> successDays
    ) {}

    public record PostCareStats(
            int conditionRate,
            int skinRate,
            int stretchRate,
            String weeklyFeedback
    ) {}

    public record SkinScore(
            int day,
            int score
    ) {}
}