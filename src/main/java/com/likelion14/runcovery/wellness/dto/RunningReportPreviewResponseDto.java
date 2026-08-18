package com.likelion14.runcovery.wellness.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "웰니스 리포트 작성 화면에 표시할 러닝 당시 날씨 및 활동 요약")
public class RunningReportPreviewResponseDto {

    @Schema(description = "러닝 기록 ID", example = "14")
    private final Long activityRecordId;

    @Schema(description = "사용자 닉네임", example = "강냉이")
    private final String nickname;

    @Schema(description = "러닝 기록 날짜", example = "2026-08-18")
    private final LocalDate recordDate;

    @Schema(description = "러닝 시작 시각", example = "2026-08-18T07:29:00.375")
    private final LocalDateTime startTime;

    @Schema(description = "러닝 종료 시각", example = "2026-08-18T08:22:00.375")
    private final LocalDateTime endTime;

    @Schema(description = "러닝 시작 시각과 위치를 기준으로 조회한 과거 날씨")
    private final Weather weather;

    @Schema(description = "화면에 표시할 러닝 활동 요약")
    private final Activity activity;

    @Getter
    @Builder
    @Schema(description = "러닝 당시 날씨")
    public static class Weather {

        @Schema(description = "자외선 지수", example = "3.2")
        private final Double uvIndex;

        @Schema(description = "기온(섭씨)", example = "26.0")
        private final Double temperatureCelsius;

        @Schema(description = "습도(%)", example = "60")
        private final Integer humidityPercent;
    }

    @Getter
    @Builder
    @Schema(description = "러닝 활동 요약")
    public static class Activity {

        @Schema(description = "총 거리(m)", example = "5000")
        private final Integer distanceM;

        @Schema(description = "러닝 시간(초)", example = "3180")
        private final Integer runningDuration;

        @Schema(description = "평균 페이스(초/km)", example = "367")
        private final Integer avgPace;

        @Schema(description = "평균 케이던스(spm)", example = "160")
        private final Integer cadence;
    }
}
