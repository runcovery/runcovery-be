package com.likelion14.runcovery.wellness.service;

import com.likelion14.runcovery.activity.ActivityRecord;
import com.likelion14.runcovery.activity.ActivityRecordRepository;
import com.likelion14.runcovery.body.BodyPart;
import com.likelion14.runcovery.common.OpenAiService;
import com.likelion14.runcovery.common.exception.CustomException;
import com.likelion14.runcovery.common.weather.WeatherResponseDto;
import com.likelion14.runcovery.condition.ConditionRepository;
import com.likelion14.runcovery.condition.Condition;
import com.likelion14.runcovery.user.User;
import com.likelion14.runcovery.user.UserRepository;
import com.likelion14.runcovery.wellness.dto.ReportRequestDto;
import com.likelion14.runcovery.wellness.dto.ReportResponseDto;
import com.likelion14.runcovery.wellness.entity.SkinRecord;
import com.likelion14.runcovery.wellness.enums.SkinRecordType;
import com.likelion14.runcovery.wellness.enums.SweatStatus;
import com.likelion14.runcovery.wellness.repository.SkinRecordQueryRepository;
import com.likelion14.runcovery.wellness.repository.WellnessActivityRecordRepository;
import com.likelion14.runcovery.wellness.repository.WellnessBodyPartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RunningReportService {

    private static final SkinRecordType REPORT_SKIN_TYPE = SkinRecordType.AFTER_RUN;
    private static final int MAX_PAIN_PART_CODES = 20;

    private final WellnessPastWeatherClient wellnessPastWeatherClient;
    private final OpenAiService openAiService;
    private final YouTubeVideoSearchService youTubeVideoSearchService;
    private final RecoveryVideoResponseMapper recoveryVideoResponseMapper;
    private final RunningReportPromptFactory runningReportPromptFactory;
    private final RunningIntensityCalculator runningIntensityCalculator;
    private final RunningReportPersistenceService runningReportPersistenceService;
    private final UserRepository userRepository;
    private final ActivityRecordRepository activityRecordRepository;
    private final WellnessActivityRecordRepository wellnessActivityRecordRepository;
    private final SkinRecordQueryRepository skinRecordQueryRepository;
    private final ConditionRepository conditionRepository;
    private final WellnessBodyPartRepository wellnessBodyPartRepository;

    /** 외부 API 호출을 마친 뒤 DB 저장 구간만 하나의 트랜잭션으로 처리합니다. */
    public ReportResponseDto generateAndSaveReport(Long userId, ReportRequestDto request) {
        ReportContext context = loadReportContext(userId, request);
        ReportResponseDto response = requestAiReport(request, context);

        runningReportPersistenceService.save(
                context.activity(),
                context.skinRecord(),
                response
        );
        return response;
    }

    private ReportContext loadReportContext(Long userId, ReportRequestDto request) {
        validateRequest(request);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        LocalDate requestedDate = request.getRecordDate() == null
                ? LocalDate.now()
                : request.getRecordDate();

        ActivityRecord activity = findActivityRecord(request, user, requestedDate);
        LocalDate reportDate = activity.getRecordDate();
        SkinRecord skinRecord = skinRecordQueryRepository
                .findFirstByUser_IdAndTypeAndMeasuredDateOrderByIdDesc(
                        userId,
                        REPORT_SKIN_TYPE,
                        reportDate
                )
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND,
                        reportDate + " 날짜의 AFTER_RUN 피부 스캔 기록이 없습니다."
                ));
        Condition condition = conditionRepository
                .findFirstByUserAndConditionDateOrderByIdDesc(user, reportDate)
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
            weather = wellnessPastWeatherClient.getPastWeather(
                    activity.getStartTime(),
                    activity.getLat(),
                    activity.getLon()
            );
        } catch (CustomException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            log.warn("Past weather lookup failed for activity {}", activity.getId(), exception);
            throw new CustomException(HttpStatus.BAD_GATEWAY, "러닝 당시 날씨를 가져오지 못했습니다.");
        }
        HydrationEstimate hydrationEstimate = estimateHydrationLoss(
                activity,
                weather,
                request.getSurvey().getSweat()
        );
        RunningIntensityCalculator.Assessment intensityAssessment = runningIntensityCalculator.calculate(
                user,
                activity,
                weather,
                condition
        );

        return new ReportContext(
                user,
                activity,
                skinRecord,
                condition,
                painfulParts,
                weather,
                hydrationEstimate,
                intensityAssessment
        );
    }

    private ActivityRecord findActivityRecord(ReportRequestDto request, User user, LocalDate requestedDate) {
        if (request.getActivityRecordId() != null) {
            ActivityRecord activity = activityRecordRepository.findById(request.getActivityRecordId())
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "러닝 기록을 찾을 수 없습니다."));
            if (!Objects.equals(activity.getUser().getId(), user.getId())) {
                throw new CustomException(HttpStatus.FORBIDDEN, "해당 러닝 기록에 접근할 수 없습니다.");
            }
            if (request.getRecordDate() != null && !activity.getRecordDate().equals(requestedDate)) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "activityRecordId와 recordDate가 일치하지 않습니다.");
            }
            return activity;
        }

        LocalDateTime reportGeneratedAt = LocalDateTime.now();
        return wellnessActivityRecordRepository
                .findAllByUser_IdAndRecordDate(user.getId(), requestedDate)
                .stream()
                .min(Comparator
                        .comparingLong((ActivityRecord activity) ->
                                distanceFromReportTime(activity, reportGeneratedAt))
                        .thenComparing(ActivityRecord::getId, Comparator.reverseOrder()))
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND,
                        requestedDate + " 날짜의 러닝 기록이 없습니다."
                ));
    }


    private long distanceFromReportTime(ActivityRecord activity, LocalDateTime reportGeneratedAt) {
        LocalDateTime activityTime = activity.getEndTime() != null
                ? activity.getEndTime()
                : activity.getStartTime();
        if (activityTime == null) {
            return Long.MAX_VALUE;
        }
        try {
            return Math.abs(Duration.between(activityTime, reportGeneratedAt).toMillis());
        } catch (ArithmeticException exception) {
            return Long.MAX_VALUE;
        }
    }
    private List<BodyPart> findPainfulParts(List<String> painPartCodes) {
        if (painPartCodes == null || painPartCodes.isEmpty()) {
            return List.of();
        }

        List<String> normalizedCodes = painPartCodes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(code -> !code.isBlank())
                .distinct()
                .toList();
        if (normalizedCodes.isEmpty()) {
            return List.of();
        }
        if (normalizedCodes.size() > MAX_PAIN_PART_CODES) {
            throw new CustomException(
                    HttpStatus.BAD_REQUEST,
                    "아픈 부위는 최대 " + MAX_PAIN_PART_CODES + "개까지 선택할 수 있습니다."
            );
        }

        Map<String, BodyPart> bodyPartsByCode = wellnessBodyPartRepository
                .findAllByBodyPartCodeIn(normalizedCodes)
                .stream()
                .collect(Collectors.toMap(BodyPart::getBodyPartCode, Function.identity()));

        return normalizedCodes.stream()
                .map(code -> {
                    BodyPart bodyPart = bodyPartsByCode.get(code);
                    if (bodyPart == null) {
                        throw new CustomException(
                                HttpStatus.BAD_REQUEST,
                                "존재하지 않는 신체 부위 코드입니다: " + code
                        );
                    }
                    return bodyPart;
                })
                .toList();
    }
    private ReportResponseDto requestAiReport(ReportRequestDto request, ReportContext context) {
        try {
            List<YouTubeVideoSearchService.VideoResult> verifiedVideos =
                    youTubeVideoSearchService.findRecoveryVideos(context.painfulParts());
            ReportResponseDto response = requestStructuredAiReport(
                    request,
                    context,
                    verifiedVideos
            );
            validateAndNormalizeAiResponse(response, context.painfulParts(), verifiedVideos);
            applyCalculatedIntensity(response, context.intensityAssessment());
            return response;
        } catch (CustomException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            log.warn("Wellness report generation failed", exception);
            throw new CustomException(HttpStatus.BAD_GATEWAY, "맞춤형 웰니스 리포트 생성에 실패했습니다.");
        }
    }

    private ReportResponseDto requestStructuredAiReport(
            ReportRequestDto request,
            ReportContext context,
            List<YouTubeVideoSearchService.VideoResult> verifiedVideos
    ) {
        try {
            RunningReportPromptFactory.PromptData promptData = new RunningReportPromptFactory.PromptData(
                    context.user(),
                    context.activity(),
                    context.skinRecord(),
                    context.condition(),
                    context.painfulParts(),
                    context.weather(),
                    context.hydrationEstimate().estimatedFluidLossMl(),
                    context.hydrationEstimate().recommendedIntakeMl(),
                    context.hydrationEstimate().sweatRateLitersPerHour()
            );
            return openAiService.getStructuredCompletion(
                    runningReportPromptFactory.buildSystemPrompt(),
                    runningReportPromptFactory.buildUserPrompt(request, promptData)
                            + runningReportPromptFactory.buildVerifiedVideosPrompt(verifiedVideos),
                    ReportResponseDto.class
            );
        } catch (CustomException exception) {
            if (exception.getStatus().is5xxServerError()) {
                log.warn("OpenAI report request failed with HTTP {}", exception.getStatus().value());
                throw new CustomException(HttpStatus.BAD_GATEWAY, "AI 리포트 생성 서버가 오류를 반환했습니다.");
            }
            throw exception;
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


        validatePrescription(response.getHydration(), "수분/영양");
        validatePrescription(response.getSkin(), "피부");
        validatePrescription(response.getStretching(), "스트레칭");

        // 회복 영상은 AI가 요약하거나 개수를 결정하지 않습니다.
        // 서버가 body_part.body_name 기반으로 그룹화하고 검증한 영상만 응답에 넣습니다.
        List<ReportResponseDto.RecoveryVideo> normalizedVideos = verifiedVideos.stream()
                .map(recoveryVideoResponseMapper::toResponse)
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
        response.setUncoveredPainPartCodes(uncoveredCodes);
    }

    private void applyCalculatedIntensity(
            ReportResponseDto response,
            RunningIntensityCalculator.Assessment assessment
    ) {
        response.setIntensity(ReportResponseDto.RunningIntensity.builder()
                .score(assessment.score())
                .level(assessment.level().name())
                .comment(assessment.comment())
                .build());
    }
    private void validatePrescription(ReportResponseDto.Prescription prescription, String label) {
        if (prescription == null || isBlank(prescription.getTitle()) || isBlank(prescription.getSolution())) {
            throw new CustomException(HttpStatus.BAD_GATEWAY, "AI 리포트의 " + label + " 처방이 누락되었습니다.");
        }
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



    private record ReportContext(
            User user,
            ActivityRecord activity,
            SkinRecord skinRecord,
            Condition condition,
            List<BodyPart> painfulParts,
            WeatherResponseDto weather,
            HydrationEstimate hydrationEstimate,
            RunningIntensityCalculator.Assessment intensityAssessment
    ) {
    }

    private record HydrationEstimate(
            int estimatedFluidLossMl,
            int recommendedIntakeMl,
            double sweatRateLitersPerHour
    ) {
    }
}