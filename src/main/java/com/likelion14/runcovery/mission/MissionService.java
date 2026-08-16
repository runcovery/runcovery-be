package com.likelion14.runcovery.mission;

import com.likelion14.runcovery.common.OpenAiService;
import com.likelion14.runcovery.common.exception.CustomException;
import com.likelion14.runcovery.common.weather.WeatherResponseDto;
import com.likelion14.runcovery.common.weather.WeatherService;
import com.likelion14.runcovery.condition.ConditionRepository;
import com.likelion14.runcovery.condition.Condition;
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
        Condition condition = conditionRepository.findByUserAndConditionDate(user, today)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "오늘 컨디션 기록이 없습니다."));

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

        // 5. OpenAI에 미션 생성 요청 (주간목표, 주간스케줄, 컨디션, 날씨 전달)
        log.info("OpenAI 요청 시작");

        MissionAiResult aiResult = openAiService.getStructuredCompletion(
                buildSystemPrompt(), buildUserPrompt(user, condition, weeklyGoal, schedules, currentWeather), MissionAiResult.class);

        log.info("OpenAI 응답 완료");

        if (aiResult == null || aiResult.recommendedIntensity() == null) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "AI 미션 생성에 실패했습니다.");
        }

        // 6. 응답 미션 저장
        Mission mission = missionRepository.findByConditionAndMissionDate(condition, today)
                .map(existing -> {
                    existing.update(today, aiResult.recommendedIntensity(), aiResult.recommendedTime(),
                            aiResult.recommendedZone(), aiResult.recommendedZoneDesc(), aiResult.detailComment());
                    existing.setIsRest(aiResult.isRest());
                    return existing;
                })
                .orElseGet(() -> {
                    Mission newMission = new Mission(condition, weeklyGoal, today,
                            aiResult.recommendedIntensity(), aiResult.recommendedTime(),
                            aiResult.recommendedZone(), aiResult.recommendedZoneDesc(), aiResult.detailComment());
                    newMission.setIsRest(aiResult.isRest());
                    return newMission;
                });
        Mission savedMission = missionRepository.save(mission);

        log.info("미션 저장 완료");

        // 7. 응답 반환
        return MissionResponseDto.from(savedMission);
    }

    public MissionResponseDto getTodayMission() {

        User user = userRepository.findById(1L)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당하는 유저가 없습니다."));

        LocalDate today = LocalDate.now();

        Condition condition = conditionRepository.findByUserAndConditionDate(user, today)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "오늘 컨디션 기록이 없습니다."));

        Mission mission = missionRepository.findByConditionAndMissionDate(condition, today)
                .orElse(null);

        if (mission == null) return null;

        return MissionResponseDto.from(mission);
    }

    private String buildSystemPrompt() {
        return """
                사용자의 컨디션, 날씨, 주간 목표를 고려하여 오늘의 일일 러닝 미션을 생성해주세요.
                반드시 아래 JSON 형식으로만 응답하세요. 다른 텍스트나 마크다운은 포함하지 마세요.
                
                [규칙]
                - "완전히", "절대", "항상" 등 극단적이고 단정적인 표현은 사용하지 마세요.
                - 권장 운동 시간은 사용자의 1회 운동 가능 시간을 초과하지 않아야 합니다.
                - 권장 운동 강도와 러닝 존은 주간 목표와 주간 스케줄에 맞게 설정해주세요.
                - 오늘의 컨디션 최근 운동 상태, 통증 부위, 피로도 분석을 참고하여 미션에 반영해주세요.
                - detailComment는 워밍업/메인/쿨다운 시간 구성만 작성하고, 운동 강도 표현은 포함하지 마세요.
                - 컨디션이 매우 나쁘거나 과도한 피로가 예상되면 isRest를 true로 설정하고, 나머지 필드는 "오늘은 휴식을 취하세요."로 채워주세요.
                
                [응답 형식]
                {
                  "recommendedIntensity": "중·고강도 러닝",
                  "recommendedTime": "20분 내외로 도전해보세요",
                  "recommendedZone": "Zone 3~4",
                  "recommendedZoneDesc": "숨이 약간 찰 정도, 짧은 대답만 가능",
                  "detailComment": "워밍업(5분)-메인(10분)-쿨다운(5분)",
                  "isRest": false
                }
               """;
    }

    private String buildUserPrompt(User user, Condition condition,
                                   WeeklyGoal weeklyGoal, List<String> schedules, WeatherResponseDto currentWeather) {
        return String.format("""
                        사용자 정보: %s, %d세, %.1fkg 최대 러닝 가능 시간: %d분
                        몸 상태: %s
                        수면: %s
                        오늘의 컨디션 분석: %s
                        주간 목표: %s
                        주간 스케줄: %s
                        주간 운동 가능 횟수: %d회
                        1회 운동 가능 시간: %d분
                        현재 날씨: 기온 %.1f°C, 습도 %d%%, 날씨 %s
                         """,
                user.getGender(),
                user.getAge(),
                user.getWeight(),
                user.getMaxRunDuration(),
                condition.getBodyCondition().getDescription(),
                condition.getSleepQuality().getDescription(),
                condition.getConditionFeedback(),
                weeklyGoal.getWeeklyGoal(),
                schedules,
                weeklyGoal.getFutureGoal().getWeeklyFrequency(),
                weeklyGoal.getFutureGoal().getAvailableTime(),
                currentWeather.getTemp(),
                currentWeather.getHumidity(),
                currentWeather.getWeatherDesc()
        );
    }
}
