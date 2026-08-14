package com.likelion14.runcovery.wellness.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FeelingStatus {
    GREAT("너무 좋았어요!"),
    NORMAL("보통이에요"),
    EXHAUSTED("너무 힘들었어요.");

    private final String description;
}