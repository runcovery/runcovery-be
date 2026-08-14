package com.likelion14.runcovery.wellness.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** 서버에서 프론트엔드로 반환하는 맞춤형 웰니스 러닝 리포트 DTO입니다. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportResponseDto {

    private RunningIntensity intensity;
    private Prescription hydration;
    private Prescription skin;
    private Prescription stretching;
    private RecoveryVideo recoveryVideo;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RunningIntensity {
        private Integer score;
        private String level;
        private String comment;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Prescription {
        private String title;
        private String solution;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RecoveryVideo {
        private String title;
        private String videoUrl;
        private String sourceTitle;
        private Integer durationSeconds;
        private String summaryBasis;

        @Builder.Default
        private List<Step> steps = new ArrayList<>();
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Step {
        private String label;
        private String description;
    }
}