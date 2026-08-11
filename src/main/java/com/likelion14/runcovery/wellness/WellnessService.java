package com.likelion14.runcovery.wellness;

import com.likelion14.runcovery.activity.ActivityService;
import com.likelion14.runcovery.common.weather.WeatherService;
import com.likelion14.runcovery.common.weather.WeatherResponseDto;
import com.likelion14.runcovery.activity.ActivityRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WellnessService {
    private final ActivityService activityService;
    private final WeatherService weatherService;

    // 과거 날씨 조회 메서드
    public WeatherResponseDto getPastWeatherByRecord() {
        long recordId = 1; // 나중에 매개변수로 받아와야 함
        ActivityRecord record = activityService.getActivityRecord(recordId);
        log.info("운동 시작 시간 : {}", record.getStartTime());
        return weatherService.getPastWeather(record.getStartTime(), record.getLat(), record.getLon());
    }
}
