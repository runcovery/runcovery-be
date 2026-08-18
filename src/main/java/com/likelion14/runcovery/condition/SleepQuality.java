package com.likelion14.runcovery.condition;

import lombok.Getter;

@Getter
public enum SleepQuality {
    GOOD("폭 자서 개운해요. (7시간 이상)"),
    FAIR("그럭저럭 잤어요. (7시간 이하)"),
    POOR("자꾸 뒤척이거나 부족했어요. (수면 부족)");

    private final String description;

    SleepQuality(String description) {
        this.description = description;
    }
}