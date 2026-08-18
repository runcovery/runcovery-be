package com.likelion14.runcovery.condition;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ConditionRequestDto {

    @NotNull(message = "몸 상태는 필수입니다")
    private BodyCondition bodyCondition;

    @NotNull(message = "수면 상태는 필수입니다")
    private SleepQuality sleepQuality;

    private List<String> painAreas;
}
