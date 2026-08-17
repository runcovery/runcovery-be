package com.likelion14.runcovery.wellness.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RunningIntensityLevelTest {

    @Test
    void mapsScoreBoundariesToExpectedLevels() {
        assertEquals(RunningIntensityLevel.LOW, RunningIntensityLevel.fromScore(1));
        assertEquals(RunningIntensityLevel.LOW, RunningIntensityLevel.fromScore(3));
        assertEquals(RunningIntensityLevel.MODERATE, RunningIntensityLevel.fromScore(4));
        assertEquals(RunningIntensityLevel.MODERATE, RunningIntensityLevel.fromScore(7));
        assertEquals(RunningIntensityLevel.HIGH, RunningIntensityLevel.fromScore(8));
        assertEquals(RunningIntensityLevel.HIGH, RunningIntensityLevel.fromScore(10));
    }
}