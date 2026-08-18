package com.likelion14.runcovery.wellness.service;

import com.likelion14.runcovery.activity.ActivityRecord;
import com.likelion14.runcovery.activity.ActivityRecordRepository;
import com.likelion14.runcovery.common.exception.CustomException;
import com.likelion14.runcovery.common.weather.WeatherResponseDto;
import com.likelion14.runcovery.wellness.dto.RunningReportPreviewResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RunningReportPreviewService {

    private final ActivityRecordRepository activityRecordRepository;
    private final WellnessPastWeatherClient wellnessPastWeatherClient;

    public RunningReportPreviewResponseDto getPreview(Long userId, Long activityRecordId) {
        if (activityRecordId == null || activityRecordId < 1) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "activityRecordId는 1 이상이어야 합니다.");
        }

        ActivityRecord activity = activityRecordRepository.findById(activityRecordId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "러닝 기록을 찾을 수 없습니다."));

        if (!Objects.equals(activity.getUser().getId(), userId)) {
            throw new CustomException(HttpStatus.FORBIDDEN, "해당 러닝 기록에 접근할 수 없습니다.");
        }
        if (activity.getStartTime() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "러닝 기록에 시작 시각이 없습니다.");
        }
        if (activity.getLat() == null || activity.getLon() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "러닝 기록에 위도와 경도가 없습니다.");
        }

        WeatherResponseDto weather = wellnessPastWeatherClient.getPastWeather(
                activity.getStartTime(),
                activity.getLat(),
                activity.getLon()
        );

        return RunningReportPreviewResponseDto.builder()
                .activityRecordId(activity.getId())
                .nickname(activity.getUser().getNickname())
                .recordDate(activity.getRecordDate())
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .weather(RunningReportPreviewResponseDto.Weather.builder()
                        .uvIndex(weather.getUvi())
                        .temperatureCelsius(weather.getTemp())
                        .humidityPercent(weather.getHumidity())
                        .build())
                .activity(RunningReportPreviewResponseDto.Activity.builder()
                        .distanceM(activity.getDistanceM())
                        .runningDuration(activity.getRunningDuration())
                        .avgPace(activity.getAvgPace())
                        .cadence(activity.getCadence())
                        .build())
                .build();
    }
}
