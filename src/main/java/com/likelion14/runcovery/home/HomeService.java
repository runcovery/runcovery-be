package com.likelion14.runcovery.home;

import com.likelion14.runcovery.common.OpenAiService;
import com.likelion14.runcovery.common.exception.CustomException;
import com.likelion14.runcovery.common.weather.WeatherResponseDto;
import com.likelion14.runcovery.common.weather.WeatherService;
import com.likelion14.runcovery.goal.*;
import com.likelion14.runcovery.mission.MissionRepository;
import com.likelion14.runcovery.mission.Mission;
import com.likelion14.runcovery.user.User;
import com.likelion14.runcovery.user.UserRepository;
import com.likelion14.runcovery.wellness.entity.Prescription;
import com.likelion14.runcovery.wellness.repository.PrescriptionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeService {

    private final UserRepository userRepository;
    private final FutureGoalRepository futureGoalRepository;
    private final MissionRepository missionRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final WeeklyGoalRepository weeklyGoalRepository;
    private final WeeklyScheduleRepository weeklyScheduleRepository;
    private final WeatherService weatherService;
    private final OpenAiService openAiService;

    @Transactional
    public HomeResponseDto getHome(long userId, double lat, double lon) {

        LocalDate today = LocalDate.now();

        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당하는 유저가 없습니다."));

        // 미래 목표 조회
        FutureGoal futureGoal = futureGoalRepository.findFirstByUserOrderByIdDesc(user).orElse(null);


        // 미래 목표 관련 필드
        String scene = null;
        int achievementRate = 0;
        int daysRemaining = 0;

        if (futureGoal != null) {
            scene = futureGoal.getScene();
            achievementRate = calcAndSaveAchievementRate(futureGoal);
            daysRemaining = (int) ChronoUnit.DAYS.between(
                    today,
                    futureGoal.getCreatedAt().toLocalDate().plusMonths(futureGoal.getTargetPeriod())
            );
        }

        // 날씨 조회
        WeatherResponseDto weather = weatherService.getCurrentWeather(lat, lon);
        int temp = (int) Math.round(weather.getTemp());

        log.info("Opne AI 요청");

         //웰니스 팁 생성
        String wellnessTip = buildWellnessTip(user, today, weather);

        log.info("Opne AI 완료");
        return new HomeResponseDto(userId, user.getNickname(), scene, achievementRate, temp, daysRemaining, wellnessTip);
    }

    // 달성률 계산
    private int calcAndSaveAchievementRate(FutureGoal futureGoal) {
        int totalTarget = futureGoal.getWeeklyFrequency() * (futureGoal.getTargetPeriod() * 4);
        long completedCount = missionRepository.countByUserAndIsCompletedTrueAndMissionDateAfter(
                futureGoal.getUser(), futureGoal.getCreatedAt().toLocalDate());
        int achievementRate = totalTarget == 0 ? 0 :
                (int) Math.min((double) completedCount / totalTarget * 100, 100);

        log.info("총 가운트 : {}, 완료 카운트 : {}, 달성률 : {}", totalTarget, completedCount, achievementRate);

        futureGoal.setAchievementRate(BigDecimal.valueOf(achievementRate));
        futureGoalRepository.save(futureGoal);
        return achievementRate;
    }

    // 상태에 따른 웰니스 팁 생성
    private String buildWellnessTip(User user, LocalDate today, WeatherResponseDto weather) {

        List<Prescription> prescriptions = prescriptionRepository.findBySkinRecordUserAndPrescriptionDate(user, today);
        Mission mission = missionRepository.findByConditionUserAndMissionDate(user, today).orElse(null);

        // 이번주 스케줄 완료 여부 확인
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);
        WeeklyGoal weeklyGoal = weeklyGoalRepository.findByUserAndCreatedAtBetween(
                user, startOfWeek.atStartOfDay(), endOfWeek.atTime(23, 59, 59)).orElse(null);

        if (weeklyGoal != null) {
            long completedThisWeek = missionRepository
                    .findByConditionUserAndMissionDateBetweenAndIsCompletedTrue(user, startOfWeek, endOfWeek)
                    .stream()
                    .filter(m -> !m.getIsRest())
                    .count();
            int totalSchedules = weeklyScheduleRepository.findByWeeklyGoal(weeklyGoal).size();

            if (completedThisWeek >= totalSchedules && prescriptions.isEmpty() && mission == null) {
                return "이번주 스케줄을 모두 완료했어요! 다음주도 화이팅이에요.";
            }
        }

        if (!prescriptions.isEmpty()) {
            // 처방전 리포트 생성 후
            boolean allCompleted = prescriptions.stream().allMatch(Prescription::getIsCompleted);
            if (allCompleted) {
                return "운동과 회복까지 챙긴 오늘, 내일의 몸이 달라질 거예요.";
            }
            return openAiService.getTextCompletion(buildSystemPrompt(), buildCompletedMissionPrompt(prescriptions));
        } else if (mission == null) {
            // 미션 생성 전
            return openAiService.getTextCompletion(buildSystemPrompt(), buildWeatherPrompt(weather));
        } else if (mission.getIsRest()) {
            // 휴식일
            return "오늘은 휴식일이에요! 가벼운 스트레칭으로 몸을 풀어주는 걸 추천드려요.";
        } else if (mission.getIsCompleted()) {
            // 미션 완료, 처방전 생성 전
            return "오늘의 운동을 완료했어요! 사후관리 리포트를 받아보세요.";
        } else {
            // 미션 생성 시, 미완료
            return openAiService.getTextCompletion(buildSystemPrompt(), buildMissionPrompt(mission));
        }
    }

    private String buildSystemPrompt() {
        return """
        사용자의 오늘 상태에 맞는 웰니스 팁을 한 문장으로 생성해주세요.
        반드시 한 문장으로만 응답하세요. 다른 텍스트나 마크다운은 포함하지 마세요.
        친근한 해요체로 자연스럽게 끝나도록 작성해주세요.
        문장 중간이나 끝에 쉼표(,) 뒤에 추가 문구를 붙이지 마세요.
        "지금 즉시", "즉각", "바로" 같은 긴박한 표현은 사용하지 마세요.
        """;
    }

    private String buildCompletedMissionPrompt(List<Prescription> prescriptions) {
        List<String> summaries = prescriptions.stream()
                .map(Prescription::getSummary)
                .toList();
        return String.format("""
                        오늘의 처방전: %s
                        위 처방전 중 가장 중요한 한 가지만 골라, "운동 후 ~을 추천드려요" 형식으로 한 문장으로 생성해주세요.
                        권장 수치(ml, 횟수 등)는 포함해도 좋지만, 사용자의 상태(땀 배출량, 피로도 등)를 추측하는 표현은 사용하지 마세요.
                        """,
                String.join(", ", summaries)
        );
    }

    private String buildMissionPrompt(Mission mission) {
        return String.format("""
        오늘 미션: %s
        권장 시간: %s
        위 미션을 바탕으로 오늘 운동을 응원하는 한 문장을 생성해주세요.
        운동을 시작하기 전 동기부여가 될 수 있도록 긍정적이고 활기찬 어투로 자연스럽게 작성해주세요.
        문장 끝에 쉼표(,) 뒤에 추가 문구를 붙이지 마세요.
        """,
                mission.getRecommendedIntensity(),
                mission.getRecommendedTime()
        );
    }

    private String buildWeatherPrompt(WeatherResponseDto weather) {
        return String.format("""
                        현재 날씨: 기온 %d°C, %s
                        오늘 날씨를 고려한 러닝 전 수분 섭취 권장량을 ml 단위로 포함해서 한 문장으로 생성해주세요.
                        예: "오늘은 기온이 높으니 러닝 전 500ml 이상 수분섭취를 추천드려요."
                        """,
                (int) Math.round(weather.getTemp()),
                weather.getWeatherDesc()
        );
    }

}