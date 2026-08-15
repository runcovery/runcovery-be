package com.likelion14.runcovery.wellness.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.likelion14.runcovery.wellness.enums.PrescriptionCategory;
import com.likelion14.runcovery.wellness.enums.SkinRecordType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

public final class PrescriptionQueryResponseDto {

    private PrescriptionQueryResponseDto() {
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Summary {
        private Long prescriptionId;
        private Long reportId;
        private LocalDate prescriptionDate;
        private PrescriptionCategory category;
        private String categoryName;
        private String title;
        private String summary;
        private Boolean isCompleted;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Detail {
        private Long prescriptionId;
        private Long reportId;
        private LocalDate prescriptionDate;
        private PrescriptionCategory category;
        private String categoryName;
        private String title;
        private String summary;
        private Boolean isCompleted;

        private NutritionDetail nutritionDetail;
        private SkinDetail skinDetail;
        private StretchingDetail stretchingDetail;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class NutritionDetail {
        private String description;
        private Integer runningDurationSeconds;
        private Integer caloriesBurned;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SkinDetail {
        private String description;
        private Long skinRecordId;
        private LocalDate measuredDate;
        private SkinRecordType skinRecordType;
        private Integer totalScore;
        private Integer redness;
        private Integer oiliness;
        private Integer texture;
        private Integer pores;
        private Integer blemishes;
        private Integer hydration;
        private Integer pigment;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class StretchingDetail {
        private String description;
        private List<Step> steps;
        private String recommendedLink;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Step {
        private String label;
        private String description;
    }
}