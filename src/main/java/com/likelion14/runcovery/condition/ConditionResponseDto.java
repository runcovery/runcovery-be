package com.likelion14.runcovery.condition;


import java.time.LocalDate;
import java.util.List;

public record ConditionResponseDto(
        Long userId,
        LocalDate conditionDate,
        String conditionTitle,
        List<String> conditionFeedback
) {}
