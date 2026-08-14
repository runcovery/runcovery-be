package com.likelion14.runcovery.wellness.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnergyStatus {
    DEPLETED("완전 방전이에요"),
    TIRED("적당히 지쳤어요"),
    ENERGETIC("에너지가 넘쳐요!");

    private final String description;
}