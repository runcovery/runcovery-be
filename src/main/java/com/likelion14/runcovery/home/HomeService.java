package com.likelion14.runcovery.home;

import com.likelion14.runcovery.common.OpenAiService;
import com.likelion14.runcovery.common.exception.CustomException;
import com.likelion14.runcovery.common.weather.WeatherResponseDto;
import com.likelion14.runcovery.common.weather.WeatherService;
import com.likelion14.runcovery.goal.FutureGoal;
import com.likelion14.runcovery.goal.FutureGoalRepository;
import com.likelion14.runcovery.mission.MissionRepository;
import com.likelion14.runcovery.mission.Mission;
import com.likelion14.runcovery.user.User;
import com.likelion14.runcovery.user.UserRepository;
import com.likelion14.runcovery.wellness.entity.Prescription;
import com.likelion14.runcovery.wellness.repository.PrescriptionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    private final WeatherService weatherService;
    private final OpenAiService openAiService;

    @Transactional
    public HomeResponseDto getHome(double lat, double lon) {

        LocalDate today = LocalDate.now();

        // 유저 조회
        User user = userRepository.findById(1L)
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

        // 웰니스 팁 생성
        String wellnessTip = buildWellnessTip(today, weather);

        return new HomeResponseDto(user.getNickname(), scene, achievementRate, temp, daysRemaining, wellnessTip);
    }

    // 달성률 계산
    private int calcAndSaveAchievementRate(FutureGoal futureGoal) {
        int totalTarget = futureGoal.getWeeklyFrequency() * (futureGoal.getTargetPeriod() * 4);
        long completedCount = missionRepository.countByIsCompletedTrue();
        int achievementRate = totalTarget == 0 ? 0 :
                (int) Math.min((double) completedCount / totalTarget * 100, 100);

        log.info("총 가운트 : {}, 완료 카운트 : {}, 달성률 : {}", totalTarget, completedCount, achievementRate);

        futureGoal.setAchievementRate(BigDecimal.valueOf(achievementRate));
        futureGoalRepository.save(futureGoal);
        return achievementRate;
    }

    // 상태에 따른 웰니스 팁 생성
    private String buildWellnessTip(LocalDate today, WeatherResponseDto weather) {
        List<Prescription> prescriptions = prescriptionRepository.findByPrescriptionDate(today);
        Mission mission = missionRepository.findByMissionDate(today).orElse(null);

        if (!prescriptions.isEmpty()) {
            // 처방전 리포트 생성 후
            return openAiService.getTextCompletion(buildSystemPrompt(), buildCompletedMissionPrompt(prescriptions));
        } else if (mission != null && mission.getIsCompleted()) {
            // 미션 완료, 처방전 생성 전
            return "오늘의 운동을 완료했어요! 사후관리 리포트를 받아보세요.";
        } else if (mission != null) {
            // 미션 생성 시
            return openAiService.getTextCompletion(buildSystemPrompt(), buildMissionPrompt(mission));
        } else {
            // 미션 생성 전
            return openAiService.getTextCompletion(buildSystemPrompt(), buildWeatherPrompt(weather));
        }
    }

    private String buildSystemPrompt() {
        return """
        사용자의 오늘 상태에 맞는 웰니스 팁을 한 문장으로 생성해주세요.
        반드시 한 문장으로만 응답하세요. 다른 텍스트나 마크다운은 포함하지 마세요.
        친근한 느낌으로 "추천드려요"로 문장을 끝내주세요.
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
        위 미션을 바탕으로 오늘의 웰니스 팁을 한 문장으로 생성해주세요.
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