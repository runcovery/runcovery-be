package com.likelion14.runcovery.wellness.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SweatStatus {
    LOW("쾌적해요(거의 안 흘림)"),
    MODERATE("적당히 났어요"),
    HIGH("흠뻑 젖었어요");

    private final String description;
}