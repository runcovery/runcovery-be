package com.likelion14.runcovery.wellness.service;

import com.likelion14.runcovery.activity.ActivityRecord;
import com.likelion14.runcovery.common.weather.WeatherResponseDto;
import com.likelion14.runcovery.condition.BodyCondition;
import com.likelion14.runcovery.condition.Condition;
import com.likelion14.runcovery.condition.SleepQuality;
import com.likelion14.runcovery.user.User;
import com.likelion14.runcovery.wellness.enums.RunningIntensityLevel;
import org.springframework.stereotype.Component;

/**
 * 심박수 기반으로 러닝 강도의 하한을 계산합니다.
 * AI 설문은 회복 조언에 사용하되 객관적 심박수 강도를 낮추지는 않습니다.
 */
@Component
public class RunningIntensityCalculator {

    private static final int MIN_VALID_AGE = 10;
    private static final int MAX_VALID_AGE = 100;

    public Assessment calculate(
            User user,
            ActivityRecord activity,
            WeatherResponseDto weather,
            Condition condition
    ) {
        Integer age = user == null ? null : user.getAge();
        Integer averageHeartRate = activity == null ? null : activity.getAvgHeartRate();
        if (!isValidAge(age) || averageHeartRate == null || averageHeartRate <= 0) {
            return new Assessment(
                    5,
                    RunningIntensityLevel.MODERATE,
                    "연령 또는 평균 심박수 정보가 충분하지 않아 운동 시간과 거리 기준으로 중강도(5/10)로 분류했습니다.",
                    null,
                    null
            );
        }

        int estimatedMaxHeartRate = 220 - age;
        double averageHeartRateRatio = averageHeartRate / (double) estimatedMaxHeartRate;
        int score = scoreFromHeartRateRatio(averageHeartRateRatio);
        RunningIntensityLevel level = RunningIntensityLevel.fromScore(score);

        int durationMinutes = activity.getRunningDuration() == null
                ? 0
                : Math.max(1, (int) Math.round(activity.getRunningDuration() / 60.0));
        int percentage = (int) Math.round(averageHeartRateRatio * 100);

        boolean recoveryWarning = condition != null
                && (condition.getBodyCondition() == BodyCondition.EXHAUSTED
                || condition.getSleepQuality() == SleepQuality.POOR);
        boolean heatOrHumidityWarning = weather != null
                && ((weather.getTemp() != null && weather.getTemp() >= 28.0)
                || (weather.getHumidity() != null && weather.getHumidity() >= 80));

        StringBuilder comment = new StringBuilder();
        if (level == RunningIntensityLevel.HIGH && recoveryWarning) {
            comment.append("⚠️ ");
        }
        comment.append("평균 심박수 ")
                .append(averageHeartRate)
                .append("bpm은 ")
                .append(age)
                .append("세 기준 추정 최대심박수 약 ")
                .append(estimatedMaxHeartRate)
                .append("bpm의 ")
                .append(percentage)
                .append("%로, ")
                .append(durationMinutes)
                .append("분 러닝을 ")
                .append(level.name())
                .append("(")
                .append(score)
                .append("/10)로 분류했습니다.");

        if (condition != null && condition.getBodyCondition() == BodyCondition.EXHAUSTED) {
            comment.append(" 신체 컨디션이 EXHAUSTED이므로 추가 고강도 운동보다 회복을 우선하세요.");
        } else if (condition != null && condition.getSleepQuality() == SleepQuality.POOR) {
            comment.append(" 수면 상태가 POOR이므로 다음 고강도 세션 전 회복 상태를 확인하세요.");
        }
        if (heatOrHumidityWarning) {
            comment.append(" 더운 또는 습한 환경은 회복 부담을 높일 수 있어 수분 보충에 유의하세요.");
        }

        return new Assessment(score, level, comment.toString(), estimatedMaxHeartRate, averageHeartRateRatio);
    }

    private int scoreFromHeartRateRatio(double ratio) {
        if (ratio >= 1.00) {
            return 10;
        }
        if (ratio >= 0.95) {
            return 9;
        }
        if (ratio >= 0.90) {
            return 8;
        }
        if (ratio >= 0.85) {
            return 7;
        }
        if (ratio >= 0.76) {
            return 6;
        }
        if (ratio >= 0.65) {
            return 5;
        }
        if (ratio >= 0.50) {
            return 4;
        }
        return 2;
    }

    private boolean isValidAge(Integer age) {
        return age != null && age >= MIN_VALID_AGE && age <= MAX_VALID_AGE;
    }

    public record Assessment(
            int score,
            RunningIntensityLevel level,
            String comment,
            Integer estimatedMaxHeartRate,
            Double averageHeartRateRatio
    ) {
    }
}