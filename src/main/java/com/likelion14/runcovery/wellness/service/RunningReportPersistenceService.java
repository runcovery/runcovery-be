package com.likelion14.runcovery.wellness.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion14.runcovery.activity.ActivityRecord;
import com.likelion14.runcovery.body.BodyIssue;
import com.likelion14.runcovery.body.BodyIssueRepository;
import com.likelion14.runcovery.body.BodyPart;
import com.likelion14.runcovery.common.exception.CustomException;
import com.likelion14.runcovery.user.User;
import com.likelion14.runcovery.wellness.dto.ReportResponseDto;
import com.likelion14.runcovery.wellness.entity.Prescription;
import com.likelion14.runcovery.wellness.entity.SkinRecord;
import com.likelion14.runcovery.wellness.entity.WellnessReport;
import com.likelion14.runcovery.wellness.enums.PrescriptionCategory;
import com.likelion14.runcovery.wellness.repository.PrescriptionRepository;
import com.likelion14.runcovery.wellness.repository.WellnessActivityRecordRepository;
import com.likelion14.runcovery.wellness.repository.WellnessReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RunningReportPersistenceService {

    private static final int MAX_SAVED_COMMENT_LENGTH = 250;

    private final ObjectMapper objectMapper;
    private final WellnessActivityRecordRepository wellnessActivityRecordRepository;
    private final BodyIssueRepository bodyIssueRepository;
    private final WellnessReportRepository wellnessReportRepository;
    private final PrescriptionRepository prescriptionRepository;

    @Transactional
    public void save(
            User user,
            ActivityRecord activity,
            SkinRecord skinRecord,
            List<BodyPart> painfulParts,
            ReportResponseDto response
    ) {
        ActivityRecord lockedActivity = wellnessActivityRecordRepository
                .findByIdForUpdate(activity.getId())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "러닝 기록을 찾을 수 없습니다."));

        synchronizeBodyIssues(user, painfulParts);
        WellnessReport savedReport = saveWellnessReport(lockedActivity, response);
        savePrescriptions(savedReport, skinRecord, response);
    }

    private void synchronizeBodyIssues(User user, List<BodyPart> painfulParts) {
        Map<String, BodyPart> selectedParts = painfulParts.stream()
                .collect(Collectors.toMap(
                        BodyPart::getBodyPartCode,
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
        List<BodyIssue> existingIssues = bodyIssueRepository.findAllByUser_IdAndIsPainfulTrue(user.getId());
        Map<String, BodyIssue> existingByCode = existingIssues.stream()
                .collect(Collectors.toMap(
                        issue -> issue.getBodyPart().getBodyPartCode(),
                        Function.identity()
                ));

        List<BodyIssue> issuesToSave = new ArrayList<>();
        for (BodyIssue existingIssue : existingIssues) {
            boolean isPainful = selectedParts.containsKey(existingIssue.getBodyPart().getBodyPartCode());
            existingIssue.update(isPainful);
            issuesToSave.add(existingIssue);
        }
        for (BodyPart selectedPart : painfulParts) {
            if (!existingByCode.containsKey(selectedPart.getBodyPartCode())) {
                issuesToSave.add(new BodyIssue(user, selectedPart, true));
            }
        }

        if (!issuesToSave.isEmpty()) {
            bodyIssueRepository.saveAll(issuesToSave);
        }
    }

    private WellnessReport saveWellnessReport(ActivityRecord activityRecord, ReportResponseDto response) {
        ReportResponseDto.RunningIntensity intensity = response.getIntensity();
        String savedComment = abbreviate(
                databaseSafe(intensity.getComment()),
                MAX_SAVED_COMMENT_LENGTH
        );

        WellnessReport report = wellnessReportRepository
                .findFirstByActivityRecord_IdOrderByIdDesc(activityRecord.getId())
                .orElseGet(() -> new WellnessReport(
                        activityRecord,
                        activityRecord.getRecordDate(),
                        savedComment,
                        intensity.getScore()
                ));

        report.setActivityRecord(activityRecord);
        report.update(activityRecord.getRecordDate(), savedComment, intensity.getScore());
        return wellnessReportRepository.save(report);
    }

    private void savePrescriptions(
            WellnessReport wellnessReport,
            SkinRecord skinRecord,
            ReportResponseDto response
    ) {
        Prescription nutrition = createOrUpdatePrescription(
                wellnessReport,
                skinRecord,
                PrescriptionCategory.NUTRITION,
                response.getHydration(),
                response.getHydration().getSolution(),
                null,
                null
        );
        Prescription skin = createOrUpdatePrescription(
                wellnessReport,
                skinRecord,
                PrescriptionCategory.SKIN,
                response.getSkin(),
                response.getSkin().getSolution(),
                null,
                buildSkinResult(skinRecord)
        );
        List<ReportResponseDto.RecoveryVideo> recoveryVideos = response.getRecoveryVideos();
        ReportResponseDto.RecoveryVideo primaryVideo = recoveryVideos.isEmpty() ? null : recoveryVideos.get(0);
        Prescription stretch = createOrUpdatePrescription(
                wellnessReport,
                skinRecord,
                PrescriptionCategory.STRETCH,
                response.getStretching(),
                buildStretchingDetail(recoveryVideos),
                primaryVideo == null ? null : primaryVideo.getVideoUrl(),
                null
        );

        prescriptionRepository.saveAll(List.of(nutrition, skin, stretch));
    }

    private Prescription createOrUpdatePrescription(
            WellnessReport wellnessReport,
            SkinRecord skinRecord,
            PrescriptionCategory category,
            ReportResponseDto.Prescription prescriptionResponse,
            String detail,
            String recommendedLink,
            String skinResult
    ) {
        String title = databaseSafe(prescriptionResponse.getTitle());
        String summary = databaseSafe(prescriptionResponse.getSolution());

        Prescription prescription = prescriptionRepository
                .findFirstByWellnessReport_IdAndCategoryOrderByIdDesc(
                        wellnessReport.getId(),
                        category
                )
                .orElseGet(() -> new Prescription(
                        wellnessReport,
                        skinRecord,
                        wellnessReport.getReportDate(),
                        category,
                        title,
                        summary
                ));

        boolean newPrescription = prescription.getId() == null;
        prescription.setWellnessReport(wellnessReport);
        prescription.setSkinRecord(skinRecord);
        prescription.update(wellnessReport.getReportDate(), category, title, summary);
        prescription.setDetail(databaseSafe(detail));
        prescription.setRecommendedLink(databaseSafe(recommendedLink));
        prescription.setSkinResult(databaseSafe(skinResult));
        if (newPrescription || category == PrescriptionCategory.NUTRITION) {
            prescription.setIsCompleted(false);
        }
        return prescription;
    }

    private String buildStretchingDetail(List<ReportResponseDto.RecoveryVideo> recoveryVideos) {
        try {
            return objectMapper.writeValueAsString(recoveryVideos);
        } catch (JsonProcessingException exception) {
            log.warn("Failed to serialize recovery videos for prescription detail", exception);
            return buildLegacyStretchingDetail(recoveryVideos);
        }
    }

    private String buildLegacyStretchingDetail(List<ReportResponseDto.RecoveryVideo> recoveryVideos) {
        return recoveryVideos.stream()
                .map(video -> "[" + video.getTitle() + "]" + System.lineSeparator()
                        + video.getRecommendationReason()
                        + System.lineSeparator()
                        + video.getVideoUrl())
                .collect(Collectors.joining(System.lineSeparator() + System.lineSeparator()));
    }

    private String buildSkinResult(SkinRecord skinRecord) {
        return "totalScore=" + skinRecord.getTotalScore()
                + ", redness=" + skinRecord.getRedness()
                + ", oiliness=" + skinRecord.getOiliness()
                + ", texture=" + skinRecord.getTexture()
                + ", pores=" + skinRecord.getPores()
                + ", blemishes=" + skinRecord.getBlemishes()
                + ", hydration=" + skinRecord.getHydration()
                + ", pigment=" + skinRecord.getPigment();
    }

    private String databaseSafe(String value) {
        if (value == null) {
            return null;
        }
        StringBuilder safe = new StringBuilder(value.length());
        value.codePoints()
                .filter(Character::isBmpCodePoint)
                .forEach(safe::appendCodePoint);
        return safe.toString();
    }

    private String abbreviate(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}