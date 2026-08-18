package com.likelion14.runcovery.goal;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class RecommendedWeeklyGoalDto {

    private String weeklyGoal;
    private Integer weeklyGoalDistance;
    private Integer expectedCalories;
    private List<ScheduleItemDto> schedules;

    @Getter
    @NoArgsConstructor
    public static class ScheduleItemDto {
        private String trainingContent;
    }
}
