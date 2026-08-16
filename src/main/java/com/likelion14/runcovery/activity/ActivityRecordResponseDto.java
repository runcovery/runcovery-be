package com.likelion14.runcovery.activity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ActivityRecordResponseDto(
        Long userId,
        Long recordId,
        Integer runningDuration,
        LocalDate recordDate,
        Integer distanceM,
        Integer avgPace,
        Integer avgHeartRate,
        Integer maxHeartRate,
        Integer calories,
        Integer cadence,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
    public static ActivityRecordResponseDto from(ActivityRecord record) {
        return new ActivityRecordResponseDto(
                record.getUser().getId(),
                record.getId(),
                record.getRunningDuration(),
                record.getRecordDate(),
                record.getDistanceM(),
                record.getAvgPace(),
                record.getAvgHeartRate(),
                record.getMaxHeartRate(),
                record.getCalories(),
                record.getCadence(),
                record.getStartTime(),
                record.getEndTime()
        );
    }
}
