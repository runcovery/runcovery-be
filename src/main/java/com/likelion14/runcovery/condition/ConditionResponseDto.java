package com.likelion14.runcovery.condition;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ConditionResponseDto {
    //private Long conditionId;
    private LocalDate conditionDate;
    private String conditionTitle;    // "최고의 컨디션이에요!"
    private List<String> conditionItems;
}
