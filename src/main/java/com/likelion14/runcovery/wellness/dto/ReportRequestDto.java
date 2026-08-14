package com.likelion14.runcovery.wellness.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.likelion14.runcovery.wellness.enums.EnergyStatus;
import com.likelion14.runcovery.wellness.enums.FeelingStatus;
import com.likelion14.runcovery.wellness.enums.SweatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 이미 저장된 당일 러닝·피부·컨디션 데이터를 이용해 웰니스 리포트를 생성하는 요청 DTO입니다.
 * recordDate를 생략하면 서버의 오늘 날짜를 사용합니다.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportRequestDto {

    private LocalDate recordDate;
    private Long activityRecordId;
    private SurveyData survey;

    @Builder.Default
    private List<String> painPartCodes = new ArrayList<>();

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SurveyData {
        private FeelingStatus feeling;
        private EnergyStatus energy;
        private SweatStatus sweat;
    }
}