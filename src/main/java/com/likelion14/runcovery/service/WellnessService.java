package com.likelion14.runcovery.service;

import com.likelion14.runcovery.dto.WeatherResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class WellnessService {
    private final ActivityService activityService;
    private final WeatherService weatherService;

    // 과거 날씨 조회 메서드
    private WeatherResponseDto getPastWeatherByRecord(long recordId, double lat, double lon) {

        LocalDateTime startTime = activityService.getActivityStartTime(recordId);
        log.info("시간 : {}", startTime);
        return weatherService.getPastWeather(startTime, lat, lon);

    }
}
