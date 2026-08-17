package com.likelion14.runcovery.wellness.enums;

public enum RunningIntensityLevel {
    LOW,
    MODERATE,
    HIGH;

    public static RunningIntensityLevel fromScore(int score) {
        if (score <= 3) {
            return LOW;
        }
        if (score <= 7) {
            return MODERATE;
        }
        return HIGH;
    }
}