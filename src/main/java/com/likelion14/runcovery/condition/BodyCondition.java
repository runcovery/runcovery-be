package com.likelion14.runcovery.condition;

import lombok.Getter;

@Getter
public enum BodyCondition {
    EXHAUSTED("완전 방전이에요."),
    FAIR("적당히 지쳤어요."),
    GOOD("좋아요. 에너지가 넘쳐요!");

    private final String description;

    BodyCondition(String description) {
        this.description = description;
    }
}