package com.likelion14.runcovery.mission;

import com.likelion14.runcovery.common.OpenAiService;
import com.likelion14.runcovery.common.exception.CustomException;
import com.likelion14.runcovery.common.weather.WeatherResponseDto;
import com.likelion14.runcovery.common.weather.WeatherService;
import com.likelion14.runcovery.condition.ConditionRepository;
import com.likelion14.runcovery.condition.ConditionRequestDto;
import com.likelion14.runcovery.condition.ConditionResponseDto;
import com.likelion14.runcovery.condition.TodayCondition;
import com.likelion14.runcovery.goal.WeeklyGoal;
import com.likelion14.runcovery.goal.WeeklyGoalRepository;
import com.likelion14.runcovery.goal.WeeklySchedule;
import com.likelion14.runcovery.goal.WeeklyScheduleRepository;
import com.likelion14.runcovery.user.User;
import com.likelion14.runcovery.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MissionService {

    private final UserRepository userRepository;
    private final ConditionRepository conditionRepository;
    private final WeeklyGoalRepository weeklyGoalRepository;
    private final WeeklyScheduleRepository weeklyScheduleRepository;
    private final MissionRepository missionRepository;
    private final WeatherService weatherService;
    private final OpenAiService openAiService;

    public MissionResponseDto generateMission(double lat, double lon) {

        // 1. 유저 조회
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당하는 유저가 없습니다."));

        // 2. 요청일 기준 컨디션 조회
        LocalDate today = LocalDate.now();
        TodayCondition condition = conditionRepository.findByUserAndConditionDate(user, today)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "오늘 컨디션 기록이 없습니다."));

        log.info("컨디션 조회 완료: {}", condition.getId());

        // 3. 주간 목표, 스케줄 조회
        WeeklyGoal weeklyGoal = weeklyGoalRepository.findTopByUserOrderByWeekNoDesc(user)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "주간 목표가 없습니다."));

        List<String> schedules = weeklyScheduleRepository.findByWeeklyGoal(weeklyGoal)
                .stream()
                .map(WeeklySchedule::getTrainingContent)
                .toList();

        log.info("주간목표 조회 완료: {}", weeklyGoal.getWeeklyGoal());
        log.info("주간목표 조회 완료: {}", String.join(", ", schedules));

        // 4. 현재 날씨 조회
        WeatherResponseDto currentWeather = weatherService.getCurrentWeather(lat, lon);

        log.info("날씨 조회 완료");

        log.info("OpenAI 요청 시작");
        // 5. OpenAI에 미션 생성 요청 (주간목표, 주간스케줄, 컨디션, 날씨 전달)
        MissionAiResult aiResult = openAiService.getStructuredCompletion(
                buildSystemPrompt(), buildUserPrompt(user, condition, weeklyGoal, schedules, currentWeather), MissionAiResult.class);

        log.info("OpenAI 응답 완료");

        // 6. 응답 미션 저장
        TodayMission mission = missionRepository.findByTodayConditionAndMissionDate(condition, today)
                .map(existing -> {
                    existing.update(today, aiResult.recommendedIntensity(), aiResult.recommendedTime(),
                            aiResult.recommendedZone(), aiResult.recommendedZoneDesc(), aiResult.detailComment());
                    existing.setIsRest(aiResult.isRest());
                    return existing;
                })
                .orElseGet(() -> {
                    TodayMission newMission = new TodayMission(condition, weeklyGoal, today,
                            aiResult.recommendedIntensity(), aiResult.recommendedTime(),
                            aiResult.recommendedZone(), aiResult.recommendedZoneDesc(), aiResult.detailComment());
                    newMission.setIsRest(aiResult.isRest());
                    return newMission;
                });
        TodayMission savedMission = missionRepository.save(mission);

        log.info("미션 저장 완료");

        // 7. 응답 반환
        MissionResponseDto response = new MissionResponseDto(
                savedMission.getId(),
                savedMission.getRecommendedIntensity(),
                savedMission.getRecommendedTime(),
                savedMission.getRecommendedZone(),
                savedMission.getRecommendedZoneDesc(),
                savedMission.getDetailComment(),
                savedMission.getIsRest()
        );

        return response;
    }

    private String buildSystemPrompt() {
        return """
            사용자의 컨디션, 날씨, 주간 목표를 고려하여 오늘의 일일 러닝 미션을 생성해주세요.
            반드시 아래 JSON 형식으로만 응답하세요. 다른 텍스트는 포함하지 마세요.
            "완전히", "절대", "항상" 등 극단적이고 단정적인 표현은 사용하지 마세요.
            컨디션이 매우 나쁘거나 과도한 피로가 예상되면 isRest를 true로 설정하고, 운동 관련 필드는 "오늘은 휴식을 취하세요."로 채워주세요.
            {
              "recommendedIntensity": "권장 운동 강도 (예: 중·고강도 러닝)",
              "recommendedTime": "권장 운동 시간 (예: 20분 내외로 도전해보세요)",
              "recommendedZone": "권장 러닝 존 (예: Zone 3~4)",
              "recommendedZoneDesc": "러닝 존 설명 (예: 숨이 약간 찰 정도, 짧은 대답만 가능)",
              "detailComment": "상세 운동 방법 (예: 워밍업(5분)-메인(10분)-쿨다운(5분))",
              "isRest": false
            }
    """;
    }

    private String buildUserPrompt(User user, TodayCondition condition,
                                   WeeklyGoal weeklyGoal, List<String> schedules, WeatherResponseDto currentWeather) {
        return String.format("""
                       사용자 정보: %s, %d세, %.1fkg
                       몸 상태: %s
                       수면: %s
                       주간 목표: %s
                       주간 스케줄: %s
                       현재 날씨: 기온 %.1f°C, 습도 %d%%, 날씨 %s
                        """,
                user.getGender(),
                user.getAge(),
                user.getWeight(),
                condition.getBodyCondition().getDescription(),
                condition.getSleepQuality().getDescription(),
                weeklyGoal.getWeeklyGoal(),
                schedules,
                currentWeather.getTemp(),
                currentWeather.getHumidity(),
                currentWeather.getWeatherDesc()
        );
    }
}
