package com.likelion14.runcovery.goal;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FuturePlanRequestDto {

    @NotNull(message = "목표 거리는 필수입니다")
    private Integer targetDistance;

    @NotNull(message = "목표 기간은 필수입니다")
    private Integer targetPeriod;

    @NotNull(message = "주간 운동 횟수는 필수입니다")
    private Integer weeklyFrequency;

    @NotNull(message = "1회 가능 시간은 필수입니다")
    private Integer availableTime;
}
