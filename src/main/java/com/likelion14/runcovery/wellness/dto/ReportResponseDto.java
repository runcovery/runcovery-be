package com.likelion14.runcovery.wellness.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    /** AI의 이전 단일 영상 응답을 읽기 위한 내부 호환 필드입니다. HTTP 응답에는 노출하지 않습니다. */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private RecoveryVideo recoveryVideo;

    /** 통증 부위를 신체 그룹으로 묶어 반환하는 최대 2개의 회복 영상입니다. */
    @Builder.Default
    private List<RecoveryVideo> recoveryVideos = new ArrayList<>();

    /** 검증된 영상에서 다루지 못한 통증 부위 코드입니다. */
    @Builder.Default
    private List<String> uncoveredPainPartCodes = new ArrayList<>();

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

        /** 서버가 body_part의 body_name을 기준으로 분류한 신체 그룹입니다. */
        private String bodyGroup;

        /** 영상 제목·선택 부위를 근거로 서버가 만든 간단한 추천 이유입니다. */
        private String recommendationReason;

        /** 이전 응답 역직렬화 호환용 필드이며 HTTP 응답에는 노출하지 않습니다. */
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        private String summaryBasis;

        /** 해당 영상 검색에 반영한 통증 부위 표시명입니다. */
        @Builder.Default
        private List<String> targetParts = new ArrayList<>();

        /** 영상 제목·설명 메타데이터에서 확인된 통증 부위 코드입니다. */
        @Builder.Default
        private List<String> coveredPainPartCodes = new ArrayList<>();

        /** 이 영상에서 확인하지 못한 같은 그룹의 통증 부위 코드입니다. */
        @Builder.Default
        private List<String> uncoveredPainPartCodes = new ArrayList<>();

        /** 이전 응답 역직렬화 호환용 필드이며 HTTP 응답에는 노출하지 않습니다. */
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        private String summaryMessage;

        /**
         * 실제 영상 자막 요약으로 오해되지 않도록 HTTP 응답에서는 숨깁니다.
         * 이전 저장 데이터 역직렬화 호환을 위해 필드 자체는 유지합니다.
         */
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
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