package com.likelion14.runcovery.wellness.service;

import com.likelion14.runcovery.activity.ActivityRecord;
import com.likelion14.runcovery.condition.BodyCondition;
import com.likelion14.runcovery.condition.Condition;
import com.likelion14.runcovery.condition.SleepQuality;
import com.likelion14.runcovery.user.User;
import com.likelion14.runcovery.wellness.enums.RunningIntensityLevel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RunningIntensityCalculatorTest {

    private final RunningIntensityCalculator calculator = new RunningIntensityCalculator();

    @Test
    void classifiesAverage190BpmFor22YearOldAsHighIntensity() {
        User user = new User();
        user.setAge(22);

        ActivityRecord activity = new ActivityRecord();
        activity.setAvgHeartRate(190);
        activity.setRunningDuration(1200);

        Condition condition = new Condition();
        condition.setBodyCondition(BodyCondition.EXHAUSTED);
        condition.setSleepQuality(SleepQuality.GOOD);

        RunningIntensityCalculator.Assessment result = calculator.calculate(user, activity, null, condition);

        assertEquals(9, result.score());
        assertEquals(RunningIntensityLevel.HIGH, result.level());
        assertTrue(result.comment().contains("96%"));
        assertTrue(result.comment().contains("EXHAUSTED"));
    }

    @Test
    void doesNotLowerObjectiveHeartRateIntensityBecauseOfSurveyOrCondition() {
        User user = new User();
        user.setAge(22);

        ActivityRecord activity = new ActivityRecord();
        activity.setAvgHeartRate(190);
        activity.setRunningDuration(1200);

        Condition condition = new Condition();
        condition.setBodyCondition(BodyCondition.EXHAUSTED);
        condition.setSleepQuality(SleepQuality.POOR);

        RunningIntensityCalculator.Assessment result = calculator.calculate(user, activity, null, condition);

        assertEquals(9, result.score());
        assertEquals(RunningIntensityLevel.HIGH, result.level());
    }
}