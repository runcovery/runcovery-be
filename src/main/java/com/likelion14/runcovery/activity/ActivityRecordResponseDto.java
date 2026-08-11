package com.likelion14.runcovery.activity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ActivityRecordResponseDto {
    private Long recordId;
    private LocalDate recordDate;
    private Integer runningDuration;
    private Integer distanceM;
    private Integer avgPace;
    private Integer avgHeartRate;
    private Integer maxHeartRate;
    private Integer calories;
    private Integer cadence;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
