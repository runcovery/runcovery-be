package com.likelion14.runcovery.wellness;

import java.time.LocalDate;

/**
 * 오늘과 전날의 AFTER_CARE 피부 점수 및 점수 차이 응답입니다.
 * difference 값은 오늘 점수 - 전날 점수이며, 양수는 개선된 점수입니다.
 */
public record SkinScoreComparisonResponse(
        SkinRecordType type,
        SkinScoreSnapshot today,
        SkinScoreSnapshot previousDay,
        SkinScoreDifference difference
) {

    public record SkinScoreSnapshot(
            LocalDate measuredDate,
            Integer totalScore,
            Integer redness,
            Integer oiliness,
            Integer texture,
            Integer pores,
            Integer blemishes,
            Integer hydration,
            Integer pigment
    ) {
        public static SkinScoreSnapshot from(SkinRecord record) {
            return new SkinScoreSnapshot(
                    record.getMeasuredDate(),
                    record.getTotalScore(),
                    record.getRedness(),
                    record.getOiliness(),
                    record.getTexture(),
                    record.getPores(),
                    record.getBlemishes(),
                    record.getHydration(),
                    record.getPigment()
            );
        }
    }

    public record SkinScoreDifference(
            Integer totalScore,
            Integer redness,
            Integer oiliness,
            Integer texture,
            Integer pores,
            Integer blemishes,
            Integer hydration,
            Integer pigment
    ) {
        public static SkinScoreDifference between(SkinRecord today, SkinRecord previousDay) {
            return new SkinScoreDifference(
                    subtract(today.getTotalScore(), previousDay.getTotalScore()),
                    subtract(today.getRedness(), previousDay.getRedness()),
                    subtract(today.getOiliness(), previousDay.getOiliness()),
                    subtract(today.getTexture(), previousDay.getTexture()),
                    subtract(today.getPores(), previousDay.getPores()),
                    subtract(today.getBlemishes(), previousDay.getBlemishes()),
                    subtract(today.getHydration(), previousDay.getHydration()),
                    subtract(today.getPigment(), previousDay.getPigment())
            );
        }

        private static Integer subtract(Integer today, Integer previousDay) {
            if (today == null || previousDay == null) {
                return null;
            }
            return today - previousDay;
        }
    }
}