package com.likelion14.runcovery.home;

import com.likelion14.runcovery.common.OpenAiService;
import com.likelion14.runcovery.common.exception.CustomException;
import com.likelion14.runcovery.common.weather.WeatherResponseDto;
import com.likelion14.runcovery.common.weather.WeatherService;
import com.likelion14.runcovery.condition.ConditionResponseDto;
import com.likelion14.runcovery.goal.FutureGoal;
import com.likelion14.runcovery.goal.FutureGoalRepository;
import com.likelion14.runcovery.mission.MissionRepository;
import com.likelion14.runcovery.mission.TodayMission;
import com.likelion14.runcovery.user.User;
import com.likelion14.runcovery.user.UserRepository;
import com.likelion14.runcovery.wellness.Prescription;
import com.likelion14.runcovery.wellness.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    public HomeResponseDto getHome(double lat, double lon) {

        LocalDate today = LocalDate.now();

        // 유저 조회
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당하는 유저가 없습니다."));

        String nickname = user.getNickname();

        // 미래 장면
        String scene = null;
        int achievementRate = 0;
        int daysRemaining = 0;

        FutureGoal futureGoal = futureGoalRepository.findFirstByUserOrderByIdDesc(user)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "유저에 해당하는 미래목표가 없습니다."));
        scene = futureGoal.getScene();

        // 달성률
        int totalTarget = futureGoal.getWeeklyFrequency() * (futureGoal.getTargetPeriod() * 4);
        long completedCount = missionRepository.countByIsCompletedTrue();
        achievementRate = totalTarget == 0 ? 0 :
                (int) Math.min((double) completedCount / totalTarget * 100, 100);

        log.info("총 카운트 : {}, 완료 카운트 : {}, 달성률 : {}", totalTarget, completedCount, achievementRate);

        futureGoal.setAchievementRate(BigDecimal.valueOf(achievementRate));
        futureGoalRepository.save(futureGoal);


        // 현재 온도
        WeatherResponseDto weather = weatherService.getCurrentWeather(lat, lon);
        int temp = (int) Math.round(weather.getTemp());
        log.info("온도 : {}, 변환 : {}", weather.getTemp(), temp);

        // 남은 일수
        daysRemaining = (int) ChronoUnit.DAYS.between(
                LocalDate.now(),
                futureGoal.getCreatedAt().toLocalDate().plusMonths(futureGoal.getTargetPeriod())
        );

        log.info("test 일 : {} ", futureGoal.getTargetPeriod());


        // 프롬프트
        log.info("웰니스 팁 생성 시작");

        String userPrompt;
        String wellnessTip;

        TodayMission mission = missionRepository.findByMissionDate(today).orElse(null);

        List<Prescription> prescription = prescriptionRepository.findByPrescriptionDate(today);

        if (!prescription.isEmpty()) {
            // 처방전 리포트 생성 후
            userPrompt = buildCompletedMissionPrompt(prescription);
            wellnessTip = openAiService.getTextCompletion(buildSystemPrompt(), userPrompt);
        } else if (mission != null && mission.getIsCompleted()) {
            // 미션 완료, 처방전 생성 전
            wellnessTip = "오늘의 운동을 완료했어요! 사후관리 리포트를 받아보세요.";
        } else if (mission != null) {
            // 미션 생성 시
            userPrompt = buildMissionPrompt(mission);
            wellnessTip = openAiService.getTextCompletion(buildSystemPrompt(), userPrompt);
        } else {
            // 미션 생성 전
            userPrompt = buildWeatherPrompt(weather);
            wellnessTip = openAiService.getTextCompletion(buildSystemPrompt(), userPrompt);
        }
        log.info("웰니스팁 생성 완료 : {}", wellnessTip);

        return new HomeResponseDto(nickname, scene, achievementRate, temp, daysRemaining, wellnessTip);
    }

    private String buildSystemPrompt() {
        return """
        사용자의 오늘 상태에 맞는 웰니스 팁을 한 문장으로 생성해주세요.
        반드시 한 문장으로만 응답하세요. 다른 텍스트나 마크다운은 포함하지 마세요.
        친근한 해요체로 작성해주세요.
        """;
    }

    private String buildCompletedMissionPrompt(List<Prescription> prescriptions) {
        List<String> summaries = prescriptions.stream()
                .map(Prescription::getSummary)
                .toList();
        return String.format("""
        오늘의 처방전: %s
        위 처방전을 바탕으로 오늘의 사후관리 팁을 한 문장으로 생성해주세요.
        """,
                String.join(", ", summaries)
        );
    }

    private String buildMissionPrompt(TodayMission mission) {
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