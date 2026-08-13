package com.likelion14.runcovery.goal;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

import java.util.List;

@Getter
@JsonPropertyOrder({ "weekId", "weekNo", "weeklyGoal", "weeklyGoalDistance", "expectedCalories", "schedules" })
public class WeeklyGoalResponseDto {

    private final Long weekId;
    private final Integer weekNo;
    private final String weeklyGoal;
    private final Integer weeklyGoalDistance;
    private final Integer expectedCalories;
    private final List<WeeklyScheduleResponseDto> schedules;

    public WeeklyGoalResponseDto(WeeklyGoal weeklyGoal, List<WeeklySchedule> schedules) {
        this.weekId = weeklyGoal.getId();
        this.weekNo = weeklyGoal.getWeekNo();
        this.weeklyGoal = weeklyGoal.getWeeklyGoal();
        this.weeklyGoalDistance = weeklyGoal.getWeeklyGoalDistance();
        this.expectedCalories = weeklyGoal.getExpectedCalories();
        this.schedules = schedules.stream().map(WeeklyScheduleResponseDto::new).toList();
    }
}
