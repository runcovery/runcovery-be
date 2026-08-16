package com.likelion14.runcovery.wellness.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion14.runcovery.activity.ActivityRecord;
import com.likelion14.runcovery.activity.ActivityRecordRepository;
import com.likelion14.runcovery.body.BodyIssue;
import com.likelion14.runcovery.body.BodyPart;
import com.likelion14.runcovery.body.BodyPartRepository;
import com.likelion14.runcovery.body.BodyIssueRepository;
import com.likelion14.runcovery.common.OpenAiService;
import com.likelion14.runcovery.common.exception.CustomException;
import com.likelion14.runcovery.common.weather.WeatherResponseDto;
import com.likelion14.runcovery.common.weather.WeatherService;
import com.likelion14.runcovery.condition.ConditionRepository;
import com.likelion14.runcovery.condition.TodayCondition;
import com.likelion14.runcovery.user.User;
import com.likelion14.runcovery.user.UserRepository;
import com.likelion14.runcovery.wellness.dto.ReportRequestDto;
import com.likelion14.runcovery.wellness.dto.ReportResponseDto;
import com.likelion14.runcovery.wellness.entity.Prescription;
import com.likelion14.runcovery.wellness.entity.SkinRecord;
import com.likelion14.runcovery.wellness.entity.WellnessReport;
import com.likelion14.runcovery.wellness.enums.PrescriptionCategory;
import com.likelion14.runcovery.wellness.enums.SkinRecordType;
import com.likelion14.runcovery.wellness.enums.SweatStatus;
import com.likelion14.runcovery.wellness.repository.PrescriptionRepository;
import com.likelion14.runcovery.wellness.repository.SkinRecordQueryRepository;
import com.likelion14.runcovery.wellness.repository.WellnessReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RunningReportService {

    private static final Long USER_ID = 1L;
    private static final SkinRecordType REPORT_SKIN_TYPE = SkinRecordType.AFTER_RUN;
    private static final int MAX_SAVED_COMMENT_LENGTH = 250;

    private final ObjectMapper objectMapper;
    private final WeatherService weatherService;
    private final OpenAiService openAiService;
    private final YouTubeVideoSearchService youTubeVideoSearchService;
    private final UserRepository userRepository;
    private final ActivityRecordRepository activityRecordRepository;
    private final SkinRecordQueryRepository skinRecordQueryRepository;
    private final ConditionRepository conditionRepository;
    private final BodyPartRepository bodyPartRepository;
    private final BodyIssueRepository bodyIssueRepository;
    private final WellnessReportRepository wellnessReportRepository;
    private final PrescriptionRepository prescriptionRepository;

    /** 저장된 당일 데이터를 조회해 리포트만 생성합니다. */
    public ReportResponseDto generateReport(ReportRequestDto request) {
        ReportContext context = loadReportContext(request);
        return requestAiReport(request, context);
    }

    /** 통증 설문을 반영하고 AI 리포트 생성 결과를 wellness_report에 저장합니다. */
    @Transactional
    public ReportResponseDto generateAndSaveReport(ReportRequestDto request) {
        ReportContext context = loadReportContext(request);
        synchronizeBodyIssues(context.user(), context.painfulParts());

        ReportResponseDto response = requestAiReport(request, context);
        WellnessReport savedReport = saveWellnessReport(context.activity(), response);
        savePrescriptions(savedReport, context.skinRecord(), response);
        return response;
    }

    private ReportContext loadReportContext(ReportRequestDto request) {
        validateRequest(request);

        User user = userRepository.findById(USER_ID)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        LocalDate requestedDate = request.getRecordDate() == null
                ? LocalDate.now()
                : request.getRecordDate();

        ActivityRecord activity = findActivityRecord(request, user, requestedDate);
        LocalDate reportDate = activity.getRecordDate();
        SkinRecord skinRecord = skinRecordQueryRepository
                .findFirstByUser_IdAndTypeAndMeasuredDateOrderByIdDesc(
                        USER_ID,
                        REPORT_SKIN_TYPE,
                        reportDate
                )
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND,
                        reportDate + " 날짜의 AFTER_RUN 피부 스캔 기록이 없습니다."
                ));
        TodayCondition condition = conditionRepository.findByUserAndConditionDate(user, reportDate)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND,
                        reportDate + " 날짜의 수면 컨디션 기록이 없습니다."
                ));
        List<BodyPart> painfulParts = findPainfulParts(request.getPainPartCodes());

        if (activity.getLat() == null || activity.getLon() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "러닝 기록에 위도와 경도가 없습니다.");
        }
        WeatherResponseDto weather;
        try {
            weather = weatherService.getPastWeather(
                    activity.getStartTime(),
                    activity.getLat(),
                    activity.getLon()
            );
        } catch (RuntimeException exception) {
            log.warn("Past weather lookup failed for activity {}", activity.getId(), exception);
            throw new CustomException(HttpStatus.BAD_GATEWAY, "러닝 당시 날씨를 가져오지 못했습니다.");
        }
        HydrationEstimate hydrationEstimate = estimateHydrationLoss(
                activity,
                weather,
                request.getSurvey().getSweat()
        );

        return new ReportContext(
                user,
                activity,
                skinRecord,
                condition,
                painfulParts,
                weather,
                hydrationEstimate
        );
    }

    private ActivityRecord findActivityRecord(ReportRequestDto request, User user, LocalDate requestedDate) {
        if (request.getActivityRecordId() != null) {
            ActivityRecord activity = activityRecordRepository.findById(request.getActivityRecordId())
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "러닝 기록을 찾을 수 없습니다."));
            if (!Objects.equals(activity.getUser().getId(), USER_ID)) {
                throw new CustomException(HttpStatus.FORBIDDEN, "해당 러닝 기록에 접근할 수 없습니다.");
            }
            if (request.getRecordDate() != null && !activity.getRecordDate().equals(requestedDate)) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "activityRecordId와 recordDate가 일치하지 않습니다.");
            }
            return activity;
        }

        return activityRecordRepository.findByUserAndRecordDate(user, requestedDate)
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND,
                        requestedDate + " 날짜의 러닝 기록이 없습니다."
                ));
    }

    private List<BodyPart> findPainfulParts(List<String> painPartCodes) {
        if (painPartCodes == null || painPartCodes.isEmpty()) {
            return List.of();
        }

        return painPartCodes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(code -> !code.isBlank())
                .distinct()
                .map(code -> bodyPartRepository.findById(code)
                        .orElseThrow(() -> new CustomException(
                                HttpStatus.BAD_REQUEST,
                                "존재하지 않는 신체 부위 코드입니다: " + code
                        )))
                .toList();
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

    private ReportResponseDto requestAiReport(ReportRequestDto request, ReportContext context) {
        try {
            List<YouTubeVideoSearchService.VideoResult> verifiedVideos =
                    youTubeVideoSearchService.findRecoveryVideos(context.painfulParts());
            ReportResponseDto response = openAiService.getStructuredCompletion(
                    buildSystemPrompt(),
                    buildUserPrompt(request, context) + buildVerifiedVideosPrompt(verifiedVideos),
                    ReportResponseDto.class
            );
            validateAndNormalizeAiResponse(response, context.painfulParts(), verifiedVideos);
            return response;
        } catch (CustomException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            log.warn("Wellness report generation failed", exception);
            throw new CustomException(HttpStatus.BAD_GATEWAY, "맞춤형 웰니스 리포트 생성에 실패했습니다.");
        }
    }

    private void validateAndNormalizeAiResponse(
            ReportResponseDto response,
            List<BodyPart> painfulParts,
            List<YouTubeVideoSearchService.VideoResult> verifiedVideos
    ) {
        if (response == null) {
            throw new CustomException(HttpStatus.BAD_GATEWAY, "AI 리포트 응답이 비어 있습니다.");
        }

        ReportResponseDto.RunningIntensity intensity = response.getIntensity();
        if (intensity == null || intensity.getScore() == null
                || intensity.getScore() < 1 || intensity.getScore() > 10
                || isBlank(intensity.getComment())) {
            throw new CustomException(HttpStatus.BAD_GATEWAY, "AI 리포트의 러닝 강도 정보가 올바르지 않습니다.");
        }
        intensity.setLevel(toIntensityLevel(intensity.getScore()));

        validatePrescription(response.getHydration(), "수분/영양");
        validatePrescription(response.getSkin(), "피부");
        validatePrescription(response.getStretching(), "스트레칭");

        // 회복 영상은 AI가 요약하거나 개수를 결정하지 않습니다.
        // 서버가 body_part.body_name 기반으로 그룹화하고 검증한 영상만 응답에 넣습니다.
        List<ReportResponseDto.RecoveryVideo> normalizedVideos = verifiedVideos.stream()
                .map(this::toRecoveryVideo)
                .toList();

        List<String> coveredCodes = normalizedVideos.stream()
                .flatMap(video -> video.getCoveredPainPartCodes().stream())
                .distinct()
                .toList();
        List<String> uncoveredCodes = painfulParts.stream()
                .map(BodyPart::getBodyPartCode)
                .filter(code -> !coveredCodes.contains(code))
                .distinct()
                .toList();

        response.setRecoveryVideos(normalizedVideos);
        response.setRecoveryVideo(normalizedVideos.isEmpty() ? null : normalizedVideos.get(0));
        response.setUncoveredPainPartCodes(uncoveredCodes);
    }

    private ReportResponseDto.RecoveryVideo toRecoveryVideo(
            YouTubeVideoSearchService.VideoResult verifiedVideo
    ) {
        String bodyGroup = resolveBodyGroup(verifiedVideo.targetParts());
        return ReportResponseDto.RecoveryVideo.builder()
                .title(buildRecoveryVideoTitle(verifiedVideo.targetParts()))
                .videoUrl(verifiedVideo.videoUrl())
                .sourceTitle(verifiedVideo.title())
                .durationSeconds(verifiedVideo.durationSeconds())
                .bodyGroup(bodyGroup)
                .recommendationReason(buildRecommendationReason(bodyGroup, verifiedVideo.targetParts()))
                .targetParts(verifiedVideo.targetParts())
                .coveredPainPartCodes(verifiedVideo.coveredPainPartCodes())
                .uncoveredPainPartCodes(verifiedVideo.uncoveredPainPartCodes())
                .build();
    }

    private String resolveBodyGroup(List<String> targetParts) {
        boolean lowerBody = targetParts.stream().anyMatch(this::isLowerBodyName);
        return lowerBody ? "LOWER_BODY" : "UPPER_BODY";
    }

    private boolean isLowerBodyName(String bodyName) {
        if (bodyName == null) {
            return false;
        }
        return List.of("무릎", "오금", "허벅지", "종아리", "정강이", "발", "골반", "서혜부", "엉덩이", "둔근", "발목")
                .stream()
                .anyMatch(bodyName::contains);
    }

    private String buildRecommendationReason(String bodyGroup, List<String> targetParts) {
        String groupLabel = "LOWER_BODY".equals(bodyGroup) ? "하체" : "상체";
        String parts = displayBodyNames(targetParts);
        String target = parts.isBlank() ? "선택 부위" : parts;
        return target + " 부위의 긴장 완화와 운동 후 회복을 돕기 위해 "
                + target + " 중심의 " + groupLabel + " 스트레칭 영상을 추천합니다.";
    }

    private String displayBodyNames(List<String> targetParts) {
        if (targetParts == null || targetParts.isEmpty()) {
            return "";
        }
        return targetParts.stream()
                .filter(Objects::nonNull)
                .map(this::displayBodyName)
                .filter(name -> !name.isBlank())
                .distinct()
                .collect(Collectors.joining(", "));
    }

    private String displayBodyName(String targetPart) {
        String bodyName = targetPart
                .replaceAll("^(?:(?:LEFT|RIGHT|FRONT|BACK)\\s*)+", "")
                .trim();
        int openingParenthesis = bodyName.indexOf('(');
        int closingParenthesis = bodyName.indexOf(')', openingParenthesis + 1);
        if (openingParenthesis >= 0 && closingParenthesis > openingParenthesis + 1) {
            return bodyName.substring(openingParenthesis + 1, closingParenthesis).trim();
        }
        return bodyName;
    }
    private void validatePrescription(ReportResponseDto.Prescription prescription, String label) {
        if (prescription == null || isBlank(prescription.getTitle()) || isBlank(prescription.getSolution())) {
            throw new CustomException(HttpStatus.BAD_GATEWAY, "AI 리포트의 " + label + " 처방이 누락되었습니다.");
        }
    }

    private String toIntensityLevel(int score) {
        if (score <= 3) {
            return "LOW";
        }
        if (score <= 7) {
            return "MODERATE";
        }
        return "HIGH";
    }

    private WellnessReport saveWellnessReport(ActivityRecord activityRecord, ReportResponseDto response) {
        ReportResponseDto.RunningIntensity intensity = response.getIntensity();
        WellnessReport report = new WellnessReport(
                activityRecord,
                activityRecord.getRecordDate(),
                abbreviate(intensity.getComment(), MAX_SAVED_COMMENT_LENGTH),
                intensity.getScore()
        );
        return wellnessReportRepository.save(report);
    }

    private void savePrescriptions(
            WellnessReport wellnessReport,
            SkinRecord skinRecord,
            ReportResponseDto response
    ) {
        Prescription nutrition = createPrescription(
                wellnessReport,
                skinRecord,
                PrescriptionCategory.NUTRITION,
                response.getHydration(),
                response.getHydration().getSolution(),
                null,
                null
        );
        Prescription skin = createPrescription(
                wellnessReport,
                skinRecord,
                PrescriptionCategory.SKIN,
                response.getSkin(),
                response.getSkin().getSolution(),
                null,
                buildSkinResult(skinRecord)
        );
        List<ReportResponseDto.RecoveryVideo> recoveryVideos = response.getRecoveryVideos();
        ReportResponseDto.RecoveryVideo primaryVideo = recoveryVideos.isEmpty()
                ? response.getRecoveryVideo()
                : recoveryVideos.get(0);
        Prescription stretch = createPrescription(
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

    private Prescription createPrescription(
            WellnessReport wellnessReport,
            SkinRecord skinRecord,
            PrescriptionCategory category,
            ReportResponseDto.Prescription prescriptionResponse,
            String detail,
            String recommendedLink,
            String skinResult
    ) {
        Prescription prescription = new Prescription(
                wellnessReport,
                skinRecord,
                wellnessReport.getReportDate(),
                category,
                prescriptionResponse.getTitle(),
                prescriptionResponse.getSolution()
        );
        prescription.setDetail(detail);
        prescription.setRecommendedLink(recommendedLink);
        prescription.setSkinResult(skinResult);
        prescription.setIsCompleted(false);
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
    private HydrationEstimate estimateHydrationLoss(
            ActivityRecord activity,
            WeatherResponseDto weather,
            SweatStatus sweatStatus
    ) {
        double sweatRate = baseSweatRate(sweatStatus);
        if (weather != null && weather.getTemp() != null) {
            if (weather.getTemp() >= 30) {
                sweatRate += 0.20;
            } else if (weather.getTemp() >= 25) {
                sweatRate += 0.10;
            }
        }
        if (weather != null && weather.getHumidity() != null) {
            if (weather.getHumidity() >= 80) {
                sweatRate += 0.15;
            } else if (weather.getHumidity() >= 65) {
                sweatRate += 0.08;
            }
        }
        sweatRate = Math.max(0.30, Math.min(sweatRate, 1.50));

        double runningHours = activity.getRunningDuration() / 3600.0;
        int estimatedLossMl = roundToNearest50(runningHours * sweatRate * 1000);
        int recommendedIntakeMl = roundToNearest50(estimatedLossMl * 1.25);
        return new HydrationEstimate(estimatedLossMl, recommendedIntakeMl, sweatRate);
    }

    private double baseSweatRate(SweatStatus sweatStatus) {
        return switch (sweatStatus) {
            case LOW -> 0.40;
            case MODERATE -> 0.70;
            case HIGH -> 1.00;
        };
    }

    private int roundToNearest50(double value) {
        return (int) (Math.round(value / 50.0) * 50);
    }

    private String buildUserPrompt(ReportRequestDto request, ReportContext context) {
        ActivityRecord run = context.activity();
        SkinRecord skin = context.skinRecord();
        WeatherResponseDto weather = context.weather();
        ReportRequestDto.SurveyData survey = request.getSurvey();
        HydrationEstimate hydration = context.hydrationEstimate();
        String painParts = context.painfulParts().isEmpty()
                ? "없음"
                : context.painfulParts().stream()
                        .map(this::describeBodyPart)
                        .collect(Collectors.joining(", "));

        return """
                다음은 같은 날짜에 저장된 실제 데이터입니다. 제공되지 않은 수치를 임의로 만들지 마세요.

                [사용자]
                - 사용자 ID: %s
                - 성별: %s
                - 나이: %s세
                - 체중: %skg

                [러닝 데이터 - %s]
                - 활동 ID: %s
                - 거리: %sm
                - 운동 시간: %s초
                - 평균 페이스: %s초/km
                - 케이던스: %sspm
                - 평균 심박수: %sbpm
                - 최대 심박수: %sbpm
                - 소모 칼로리: %skcal
                - 시작 시각: %s
                - 종료 시각: %s
                - 위도/경도: %s, %s

                [러닝 당시 과거 날씨]
                - 기온: %s℃
                - 체감온도: %s℃
                - 습도: %s%%
                - 자외선 지수: %s
                - 날씨 설명: %s

                [AFTER_RUN 피부 점수 - 0~100]
                - 총점: %s
                - 홍조(redness): %s
                - 유분(oiliness): %s
                - 피부결(texture): %s
                - 모공(pores): %s
                - 잡티(blemishes): %s
                - 수분(hydration): %s
                - 색소침착(pigment): %s

                [당일 컨디션]
                - 수면 상태: %s
                - 수면 상태 설명: %s

                [운동 후 설문]
                - feeling: %s
                - energy: %s
                - sweat: %s
                - 아픈 부위: %s

                [서버 계산 참고값]
                - 추정 시간당 발한량: %.2fL/h
                - 추정 수분 손실량: 약 %sml
                - 권장 수분 보충량: 약 %sml
                - 이 값은 운동 시간, 땀 설문, 기온, 습도를 이용한 웰니스 추정치이며 의학적 측정값이 아님
                """.formatted(
                context.user().getId(), context.user().getGender(), context.user().getAge(), context.user().getWeight(),
                run.getRecordDate(), run.getId(), run.getDistanceM(), run.getRunningDuration(), run.getAvgPace(),
                run.getCadence(), run.getAvgHeartRate(), run.getMaxHeartRate(), run.getCalories(),
                run.getStartTime(), run.getEndTime(), run.getLat(), run.getLon(),
                value(weather, WeatherResponseDto::getTemp),
                value(weather, WeatherResponseDto::getFeelsLike),
                value(weather, WeatherResponseDto::getHumidity),
                value(weather, WeatherResponseDto::getUvi),
                value(weather, WeatherResponseDto::getWeatherDesc),
                skin.getTotalScore(), skin.getRedness(), skin.getOiliness(), skin.getTexture(), skin.getPores(),
                skin.getBlemishes(), skin.getHydration(), skin.getPigment(),
                context.condition().getSleepQuality().name(),
                context.condition().getSleepQuality().getDescription(),
                survey.getFeeling().name() + " (" + survey.getFeeling().getDescription() + ")",
                survey.getEnergy().name() + " (" + survey.getEnergy().getDescription() + ")",
                survey.getSweat().name() + " (" + survey.getSweat().getDescription() + ")",
                painParts,
                hydration.sweatRateLitersPerHour(), hydration.estimatedFluidLossMl(), hydration.recommendedIntakeMl()
        );
    }

    private String buildVerifiedVideosPrompt(List<YouTubeVideoSearchService.VideoResult> videos) {
        StringBuilder videoDetails = new StringBuilder();
        for (int index = 0; index < videos.size(); index++) {
            YouTubeVideoSearchService.VideoResult video = videos.get(index);
            videoDetails.append("\n[").append(index + 1).append("번 검증 영상]")
                    .append("\n- 대상 부위: ").append(String.join(", ", video.targetParts()))
                    .append("\n- 포함된 통증 코드: ").append(String.join(", ", video.coveredPainPartCodes()))
                    .append("\n- 실제 영상 제목: ").append(video.title())
                    .append("\n- 실제 영상 URL: ").append(video.videoUrl())
                    .append("\n- 영상 길이: ").append(video.durationSeconds()).append("초")
                    .append("\n- 영상 설명: ").append(video.description()).append("\n");
        }

        return """

                [서버가 body_part.body_name 기준으로 분류·검증한 회복 영상 - 최대 2개]
                %s
                - 회복 영상 목록과 추천 이유는 서버가 생성합니다.
                - AI는 영상 요약, 동작 단계, URL, 영상 제목을 생성하거나 수정하지 마세요.
                """.formatted(videoDetails);
    }
    private String buildSystemPrompt() {
        return """
                당신은 RunCovery의 데이터 기반 웰니스 러닝 코치입니다.
                입력에 포함된 실제 수치만 근거로 사용하고, 입력마다 결과가 달라지도록 분석하세요.
                고정된 예문을 반복하거나 제공되지 않은 수치·질환·증상을 만들어내지 마세요.

                1. 오늘의 러닝 강도
                - 기온, 체감온도, 습도, 운동시간, 거리, 페이스, 케이던스, 평균·최대 심박수를 핵심 근거로 score를 판단하세요.
                - 수면 상태, feeling, energy, sweat, AFTER_RUN 피부 점수는 회복 부담과 경고 코멘트를 보정하는 근거로 사용하세요.
                - score 1~3은 LOW, 4~7은 MODERATE, 8~10은 HIGH입니다.
                - comment는 실제 입력값 중 의미 있는 근거를 최소 2개 포함한 180자 이내의 동적인 한글 문장으로 작성하세요.
                - HIGH이거나 수면 부족과 높은 심박수 등 복합 위험 신호가 있을 때만 ⚠️ 경고 표현을 사용하세요.

                2. 맞춤형 웰니스 처방전
                - hydration: 서버가 계산한 추정 수분 손실량, 권장 보충량, 실제 소모 칼로리와 날씨를 활용해 수분/영양 한 줄 솔루션을 작성하세요.
                - skin: 7가지 피부 점수, 기온, 습도, 자외선, sweat 응답을 근거로 한 줄 피부 솔루션을 작성하세요.
                - stretching: 사용자가 선택한 아픈 부위를 우선 고려하되 의료적 치료를 단정하지 않는 한 줄 스트레칭 솔루션을 작성하세요.
                - 각 solution은 핵심 수치를 포함하고 120자 이내 한 문장으로 작성하세요.

                3. 회복 영상 추천
                - 영상은 서버가 body_part.body_name 기준으로 상체·하체를 분류한 뒤 최대 2개까지 결정합니다.
                - AI는 영상의 실제 내용을 분석하거나 요약하지 않습니다.
                - recoveryVideos, 영상 URL, 영상 제목, 동작 단계는 JSON에 포함하지 마세요. 서버가 검증값과 짧은 추천 이유를 추가합니다.

                반드시 아래 구조의 JSON 객체 하나만 반환하세요. Markdown과 추가 설명은 금지합니다.
                {
                  "intensity": {
                    "score": 1,
                    "level": "LOW",
                    "comment": "데이터 근거가 포함된 오늘의 러닝 강도 코멘트"
                  },
                  "hydration": {
                    "title": "수분/영양 제목",
                    "solution": "한 줄 솔루션"
                  },
                  "skin": {
                    "title": "피부 제목",
                    "solution": "한 줄 솔루션"
                  },
                  "stretching": {
                    "title": "스트레칭 제목",
                    "solution": "한 줄 솔루션"
                  }
                }
                """;
    }
    private String describeBodyPart(BodyPart bodyPart) {
        List<String> details = new ArrayList<>();
        if (!isBlank(bodyPart.getSide())) {
            details.add(bodyPart.getSide());
        }
        if (!isBlank(bodyPart.getDirection())) {
            details.add(bodyPart.getDirection());
        }
        String suffix = details.isEmpty() ? "" : " / " + String.join(" / ", details);
        return bodyPart.getBodyName() + suffix + " [" + bodyPart.getBodyPartCode() + "]";
    }

    private String buildRecoveryVideoTitle(List<String> targetParts) {
        String target = displayBodyNames(targetParts);
        return (target.isBlank() ? "전신" : target) + " 회복 스트레칭 영상";
    }
    private <T> Object value(WeatherResponseDto weather, Function<WeatherResponseDto, T> getter) {
        return weather == null ? null : getter.apply(weather);
    }

    private void validateRequest(ReportRequestDto request) {
        if (request == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "리포트 요청은 필수입니다.");
        }
        if (request.getRecordDate() != null && request.getRecordDate().isAfter(LocalDate.now())) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "미래 날짜의 리포트는 생성할 수 없습니다.");
        }
        ReportRequestDto.SurveyData survey = request.getSurvey();
        if (survey == null || survey.getFeeling() == null
                || survey.getEnergy() == null || survey.getSweat() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "feeling, energy, sweat 설문 응답은 필수입니다.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String abbreviate(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private record ReportContext(
            User user,
            ActivityRecord activity,
            SkinRecord skinRecord,
            TodayCondition condition,
            List<BodyPart> painfulParts,
            WeatherResponseDto weather,
            HydrationEstimate hydrationEstimate
    ) {
    }

    private record HydrationEstimate(
            int estimatedFluidLossMl,
            int recommendedIntakeMl,
            double sweatRateLitersPerHour
    ) {
    }
}