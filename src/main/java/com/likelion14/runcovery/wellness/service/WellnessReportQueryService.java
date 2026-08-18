package com.likelion14.runcovery.wellness.service;

import com.likelion14.runcovery.common.exception.CustomException;
import com.likelion14.runcovery.wellness.dto.WellnessReportQueryResponseDto;
import com.likelion14.runcovery.wellness.entity.WellnessReport;
import com.likelion14.runcovery.wellness.enums.RunningIntensityLevel;
import com.likelion14.runcovery.wellness.repository.WellnessReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WellnessReportQueryService {

    private final WellnessReportRepository wellnessReportRepository;

    public WellnessReportQueryResponseDto getLatestOrByDate(Long userId, LocalDate reportDate) {
        validateUserId(userId);

        WellnessReport report = reportDate == null
                ? wellnessReportRepository
                        .findFirstByActivityRecord_User_IdOrderByReportDateDescIdDesc(userId)
                        .orElseThrow(() -> reportNotFound(userId))
                : wellnessReportRepository
                        .findFirstByActivityRecord_User_IdAndReportDateOrderByIdDesc(userId, reportDate)
                        .orElseThrow(() -> new CustomException(
                                HttpStatus.NOT_FOUND,
                                reportDate + " 날짜의 웰니스 리포트를 찾을 수 없습니다."
                        ));

        return toResponse(report);
    }

    public WellnessReportQueryResponseDto getById(Long userId, Long reportId) {
        validateUserId(userId);
        if (reportId == null || reportId <= 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "reportId는 1 이상의 값이어야 합니다.");
        }

        WellnessReport report = wellnessReportRepository
                .findByIdAndActivityRecord_User_Id(reportId, userId)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND,
                        "웰니스 리포트를 찾을 수 없습니다."
                ));
        return toResponse(report);
    }

    private WellnessReportQueryResponseDto toResponse(WellnessReport report) {
        return WellnessReportQueryResponseDto.builder()
                .reportId(report.getId())
                .activityRecordId(report.getActivityRecord().getId())
                .reportDate(report.getReportDate())
                .runningIntensity(report.getRunningIntensity())
                .intensityLevel(toIntensityLevel(report.getRunningIntensity()))
                .comment(report.getWarningTitle())
                .build();
    }

    private String toIntensityLevel(Integer score) {
        return score == null ? null : RunningIntensityLevel.fromScore(score).name();
    }
    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "userId는 1 이상의 값이어야 합니다.");
        }
    }

    private CustomException reportNotFound(Long userId) {
        return new CustomException(
                HttpStatus.NOT_FOUND,
                "userId=" + userId + "의 웰니스 리포트를 찾을 수 없습니다."
        );
    }
}