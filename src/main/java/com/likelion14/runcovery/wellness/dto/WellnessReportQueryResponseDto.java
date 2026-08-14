package com.likelion14.runcovery.wellness.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WellnessReportQueryResponseDto {

    private Long reportId;
    private Long userId;
    private Long activityRecordId;
    private LocalDate reportDate;
    private Integer runningIntensity;
    private String intensityLevel;
    private String comment;
}