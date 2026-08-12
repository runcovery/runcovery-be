package com.likelion14.runcovery.goal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedPlanDto {
    private Integer targetDistance;
    private Integer targetPeriod;
    private Integer weeklyFrequency;
    private Integer availableTime;
    private String reason;
}
