package com.likelion14.runcovery.goal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlanRecommendResponseDto {
    private Integer targetDistance;
    private Integer targetPeriod;
    private Integer weeklyFrequency;
    private Integer availableTime;
    private Integer baselineVolume;
    private String reason;
}
