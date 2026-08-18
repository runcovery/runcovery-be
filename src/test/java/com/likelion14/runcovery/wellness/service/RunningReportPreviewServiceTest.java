package com.likelion14.runcovery.wellness.service;

import com.likelion14.runcovery.activity.ActivityRecord;
import com.likelion14.runcovery.activity.ActivityRecordRepository;
import com.likelion14.runcovery.common.exception.CustomException;
import com.likelion14.runcovery.common.weather.WeatherResponseDto;
import com.likelion14.runcovery.user.User;
import com.likelion14.runcovery.wellness.dto.RunningReportPreviewResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RunningReportPreviewServiceTest {

    @Mock
    private ActivityRecordRepository activityRecordRepository;

    @Mock
    private WellnessPastWeatherClient wellnessPastWeatherClient;

    @InjectMocks
    private RunningReportPreviewService runningReportPreviewService;

    private ActivityRecord activity;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);
        user.setNickname("강냉이");

        activity = new ActivityRecord();
        activity.setId(14L);
        activity.setUser(user);
        activity.setRecordDate(LocalDate.of(2026, 8, 18));
        activity.setStartTime(LocalDateTime.of(2026, 8, 18, 7, 29));
        activity.setEndTime(LocalDateTime.of(2026, 8, 18, 8, 22));
        activity.setLat(37.5665);
        activity.setLon(126.9780);
        activity.setDistanceM(5_000);
        activity.setRunningDuration(3_180);
        activity.setAvgPace(367);
        activity.setCadence(160);
    }

    @Test
    void returnsPastWeatherAndActivitySummary() {
        when(activityRecordRepository.findById(14L)).thenReturn(Optional.of(activity));
        when(wellnessPastWeatherClient.getPastWeather(
                activity.getStartTime(),
                activity.getLat(),
                activity.getLon()
        )).thenReturn(WeatherResponseDto.builder()
                .uvi(3.2)
                .temp(26.0)
                .humidity(60)
                .build());

        RunningReportPreviewResponseDto response =
                runningReportPreviewService.getPreview(1L, 14L);

        assertEquals(14L, response.getActivityRecordId());
        assertEquals("강냉이", response.getNickname());
        assertEquals(3.2, response.getWeather().getUvIndex());
        assertEquals(26.0, response.getWeather().getTemperatureCelsius());
        assertEquals(60, response.getWeather().getHumidityPercent());
        assertEquals(5_000, response.getActivity().getDistanceM());
        assertEquals(3_180, response.getActivity().getRunningDuration());
        assertEquals(367, response.getActivity().getAvgPace());
        assertEquals(160, response.getActivity().getCadence());

        verify(wellnessPastWeatherClient).getPastWeather(
                activity.getStartTime(),
                activity.getLat(),
                activity.getLon()
        );
    }

    @Test
    void rejectsAnotherUsersActivity() {
        when(activityRecordRepository.findById(14L)).thenReturn(Optional.of(activity));

        CustomException exception = assertThrows(
                CustomException.class,
                () -> runningReportPreviewService.getPreview(2L, 14L)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        verifyNoInteractions(wellnessPastWeatherClient);
    }
}
