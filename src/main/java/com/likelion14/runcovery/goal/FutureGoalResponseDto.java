package com.likelion14.runcovery.goal;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class FutureGoalResponseDto {

    private Long futureId;
    private String scene;
    private Integer targetDistance;
    private Integer targetPeriod;
    private Integer weeklyFrequency;
    private Integer availableTime;
    private BigDecimal achievementRate;
    private LocalDateTime createdAt;

    public FutureGoalResponseDto(FutureGoal futureGoal) {
        this.futureId = futureGoal.getId();
        this.scene = futureGoal.getScene();
        this.targetDistance = futureGoal.getTargetDistance();
        this.targetPeriod = futureGoal.getTargetPeriod();
        this.weeklyFrequency = futureGoal.getWeeklyFrequency();
        this.availableTime = futureGoal.getAvailableTime();
        this.achievementRate = futureGoal.getAchievementRate();
        this.createdAt = futureGoal.getCreatedAt();
    }
}
