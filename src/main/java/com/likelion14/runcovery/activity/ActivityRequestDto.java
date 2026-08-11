package com.likelion14.runcovery.activity;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ActivityRequestDto {

    @NotNull(message = "러닝시간(sec)는 필수입니다")
    private Integer runningDuration;

    @NotNull(message = "기록일은 필수입니다")
    private LocalDate recordDate;

    @NotNull(message = "러닝거리(m)는 필수입니다")
    private Integer distanceM;

    @NotNull(message = "평균페이스(sec)는 필수입니다")
    private Integer avgPace;

    @NotNull(message = "평균 심박수는 필수입니다")
    private Integer avgHeartRate;

    @NotNull(message = "최대 심박수는 필수입니다")
    private Integer maxHeartRate;

    @NotNull(message = "소모 칼로리는 필수입니다")
    private Integer calories;

    @NotNull(message = "케이던스는 필수입니다")
    private Integer cadence;

    @NotNull(message = "러닝 시작시간은 필수입니다")
    private LocalDateTime startTime;

    @NotNull(message = "러닝 종료시간은 필수입니다")
    private LocalDateTime endTime;

    @NotNull(message = "위도는 필수입니다")
    private Double lat;

    @NotNull(message = "경도는 필수입니다")
    private Double lon;

}
