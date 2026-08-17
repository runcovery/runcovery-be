package com.likelion14.runcovery.wellness.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion14.runcovery.common.exception.CustomException;
import com.likelion14.runcovery.wellness.dto.PrescriptionQueryResponseDto;
import com.likelion14.runcovery.wellness.dto.ReportResponseDto;
import com.likelion14.runcovery.wellness.enums.PrescriptionCategory;
import com.likelion14.runcovery.activity.ActivityRecord;
import com.likelion14.runcovery.wellness.entity.Prescription;
import com.likelion14.runcovery.wellness.entity.SkinRecord;
import com.likelion14.runcovery.wellness.entity.WellnessReport;
import com.likelion14.runcovery.wellness.repository.PrescriptionRepository;
import com.likelion14.runcovery.wellness.repository.WellnessReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrescriptionQueryService {

    private static final Pattern STEP_PATTERN =
            Pattern.compile("^(STEP\\s*\\d+)\\s*:?\\s*(.*)$", Pattern.CASE_INSENSITIVE);

    private final ObjectMapper objectMapper;
    private final PrescriptionRepository prescriptionRepository;
    private final WellnessReportRepository wellnessReportRepository;

    public List<PrescriptionQueryResponseDto.Summary> getPrescriptions(Long userId, Long reportId) {
        validateUserId(userId);
        Long targetReportId = resolveReportId(userId, reportId);

        return prescriptionRepository
                .findAllByWellnessReport_IdAndWellnessReport_ActivityRecord_User_IdOrderByCategoryAscIdAsc(
                        targetReportId,
                        userId
                )
                .stream()
                .map(this::toSummary)
                .toList();
    }

    public PrescriptionQueryResponseDto.Detail getPrescription(Long userId, Long prescriptionId) {
        validateUserId(userId);
        if (prescriptionId == null || prescriptionId <= 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "prescriptionId는 1 이상의 값이어야 합니다.");
        }

        Prescription prescription = prescriptionRepository
                .findByIdAndWellnessReport_ActivityRecord_User_Id(prescriptionId, userId)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND,
                        "처방전을 찾을 수 없습니다."
                ));
        return toDetail(prescription);
    }

    @Transactional
    public PrescriptionQueryResponseDto.Completion updateCompletion(
            Long userId,
            Long reportId,
            PrescriptionCategory category,
            Boolean isCompleted
    ) {
        validateUserId(userId);
        if (category == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "처방전 카테고리는 필수입니다.");
        }
        if (isCompleted == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "isCompleted 값은 필수입니다.");
        }
        if (!isCompletionSupported(category)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "수분/영양 처방은 완료 처리 대상이 아닙니다.");
        }

        Long targetReportId = resolveReportId(userId, reportId);
        Prescription prescription = prescriptionRepository
                .findByWellnessReport_IdAndWellnessReport_ActivityRecord_User_IdAndCategory(
                        targetReportId,
                        userId,
                        category
                )
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND,
                        toCategoryName(category) + " 처방전을 찾을 수 없습니다."
                ));

        prescription.setIsCompleted(isCompleted);
        Prescription savedPrescription = prescriptionRepository.save(prescription);

        return PrescriptionQueryResponseDto.Completion.builder()
                .prescriptionId(savedPrescription.getId())
                .reportId(savedPrescription.getWellnessReport().getId())
                .category(savedPrescription.getCategory())
                .categoryName(toCategoryName(savedPrescription.getCategory()))
                .isCompleted(Boolean.TRUE.equals(savedPrescription.getIsCompleted()))
                .build();
    }
    private Long resolveReportId(Long userId, Long reportId) {
        if (reportId == null) {
            return wellnessReportRepository
                    .findFirstByActivityRecord_User_IdOrderByReportDateDescIdDesc(userId)
                    .map(WellnessReport::getId)
                    .orElseThrow(() -> new CustomException(
                            HttpStatus.NOT_FOUND,
                            "조회할 웰니스 리포트가 없습니다."
                    ));
        }
        if (reportId <= 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "reportId는 1 이상의 값이어야 합니다.");
        }

        return wellnessReportRepository
                .findByIdAndActivityRecord_User_Id(reportId, userId)
                .map(WellnessReport::getId)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND,
                        "웰니스 리포트를 찾을 수 없습니다."
                ));
    }

    private PrescriptionQueryResponseDto.Summary toSummary(Prescription prescription) {
        return PrescriptionQueryResponseDto.Summary.builder()
                .prescriptionId(prescription.getId())
                .reportId(prescription.getWellnessReport().getId())
                .prescriptionDate(prescription.getPrescriptionDate())
                .category(prescription.getCategory())
                .categoryName(toCategoryName(prescription.getCategory()))
                .title(prescription.getTitle())
                .summary(prescription.getSummary())
                .isCompleted(Boolean.TRUE.equals(prescription.getIsCompleted()))
                .completionSupported(isCompletionSupported(prescription.getCategory()))
                .build();
    }

    private PrescriptionQueryResponseDto.Detail toDetail(Prescription prescription) {
        PrescriptionQueryResponseDto.Detail.DetailBuilder response =
                PrescriptionQueryResponseDto.Detail.builder()
                        .prescriptionId(prescription.getId())
                        .reportId(prescription.getWellnessReport().getId())
                        .prescriptionDate(prescription.getPrescriptionDate())
                        .category(prescription.getCategory())
                        .categoryName(toCategoryName(prescription.getCategory()))
                        .title(prescription.getTitle())
                        .summary(prescription.getSummary())
                        .isCompleted(Boolean.TRUE.equals(prescription.getIsCompleted()))
                        .completionSupported(isCompletionSupported(prescription.getCategory()));

        switch (prescription.getCategory()) {
            case NUTRITION -> response.nutritionDetail(toNutritionDetail(prescription));
            case SKIN -> response.skinDetail(toSkinDetail(prescription));
            case STRETCH -> response.stretchingDetail(toStretchingDetail(prescription));
        }
        return response.build();
    }

    private PrescriptionQueryResponseDto.NutritionDetail toNutritionDetail(Prescription prescription) {
        ActivityRecord activity = prescription.getWellnessReport().getActivityRecord();
        return PrescriptionQueryResponseDto.NutritionDetail.builder()
                .description(detailOrSummary(prescription))
                .runningDurationSeconds(activity.getRunningDuration())
                .caloriesBurned(activity.getCalories())
                .build();
    }

    private PrescriptionQueryResponseDto.SkinDetail toSkinDetail(Prescription prescription) {
        SkinRecord skin = prescription.getSkinRecord();
        return PrescriptionQueryResponseDto.SkinDetail.builder()
                .description(detailOrSummary(prescription))
                .skinRecordId(skin.getId())
                .measuredDate(skin.getMeasuredDate())
                .skinRecordType(skin.getType())
                .totalScore(skin.getTotalScore())
                .redness(skin.getRedness())
                .oiliness(skin.getOiliness())
                .texture(skin.getTexture())
                .pores(skin.getPores())
                .blemishes(skin.getBlemishes())
                .hydration(skin.getHydration())
                .pigment(skin.getPigment())
                .build();
    }

    private PrescriptionQueryResponseDto.StretchingDetail toStretchingDetail(Prescription prescription) {
        List<ReportResponseDto.RecoveryVideo> recoveryVideos = parseRecoveryVideos(prescription.getDetail());

        return PrescriptionQueryResponseDto.StretchingDetail.builder()
                .description(recoveryVideos.isEmpty() ? detailOrSummary(prescription) : prescription.getSummary())
                .recommendedLink(prescription.getRecommendedLink())
                .recoveryVideos(recoveryVideos)
                .build();
    }

    private List<ReportResponseDto.RecoveryVideo> parseRecoveryVideos(String detail) {
        if (isBlank(detail) || !detail.trim().startsWith("[")) {
            return List.of();
        }
        try {
            return objectMapper.readValue(
                    detail,
                    new TypeReference<List<ReportResponseDto.RecoveryVideo>>() {
                    }
            );
        } catch (JsonProcessingException exception) {
            return List.of();
        }
    }

    private List<PrescriptionQueryResponseDto.Step> parseSteps(String detail, String fallback) {
        String source = isBlank(detail) ? fallback : detail;
        if (isBlank(source)) {
            return List.of();
        }

        List<PrescriptionQueryResponseDto.Step> steps = new ArrayList<>();
        String currentLabel = null;
        StringBuilder currentDescription = new StringBuilder();

        for (String rawLine : source.split("\\R")) {
            String line = rawLine.trim();
            if (line.isBlank()) {
                continue;
            }

            Matcher matcher = STEP_PATTERN.matcher(line);
            if (matcher.matches()) {
                addStep(steps, currentLabel, currentDescription);
                currentLabel = matcher.group(1).toUpperCase(Locale.ROOT).replaceAll("\\s+", " ");
                currentDescription = new StringBuilder(matcher.group(2).trim());
            } else if (currentLabel != null) {
                if (!currentDescription.isEmpty()) {
                    currentDescription.append(' ');
                }
                currentDescription.append(line);
            }
        }
        addStep(steps, currentLabel, currentDescription);

        if (steps.isEmpty()) {
            steps.add(PrescriptionQueryResponseDto.Step.builder()
                    .label("GUIDE")
                    .description(source)
                    .build());
        }
        return steps;
    }

    private void addStep(
            List<PrescriptionQueryResponseDto.Step> steps,
            String label,
            StringBuilder description
    ) {
        if (label == null) {
            return;
        }
        steps.add(PrescriptionQueryResponseDto.Step.builder()
                .label(label)
                .description(description.toString())
                .build());
    }

    private String detailOrSummary(Prescription prescription) {
        return isBlank(prescription.getDetail())
                ? prescription.getSummary()
                : prescription.getDetail();
    }

    private boolean isCompletionSupported(PrescriptionCategory category) {
        return category == PrescriptionCategory.SKIN || category == PrescriptionCategory.STRETCH;
    }
    private String toCategoryName(PrescriptionCategory category) {
        return switch (category) {
            case NUTRITION -> "수분/영양";
            case SKIN -> "피부";
            case STRETCH -> "스트레칭";
        };
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "userId는 1 이상의 값이어야 합니다.");
        }
    }
}