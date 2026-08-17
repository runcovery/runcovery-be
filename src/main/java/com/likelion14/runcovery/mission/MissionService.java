package com.likelion14.runcovery.mission;

import com.likelion14.runcovery.activity.ActivityRecord;
import com.likelion14.runcovery.activity.ActivityRecordRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MissionService {

    private final UserRepository userRepository;
    private final ActivityRecordRepository activityRecordRepository;
    private final ConditionRepository conditionRepository;
    private final WeeklyGoalRepository weeklyGoalRepository;
    private final WeeklyScheduleRepository weeklyScheduleRepository;
    private final MissionRepository missionRepository;
    private final WeatherService weatherService;
    private final OpenAiService openAiService;

    @Transactional
    public MissionResponseDto generateMission(long userId, double lat, double lon) {

        // 1. 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당하는 유저가 없습니다."));

        // 2. 요청일 기준 컨디션 조회
        LocalDate today = LocalDate.now();
        Condition condition = conditionRepository.findByUserAndConditionDate(user, today)
                .orElse(null);
        if (condition == null) {
            throw new CustomException(HttpStatus.NOT_FOUND, "오늘의 컨디션 분석을 먼저 해야합니다.");
        }

        // 3. 주간 목표, 스케줄 조회
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);

        WeeklyGoal weeklyGoal = weeklyGoalRepository.findByUserAndCreatedAtBetween(user, startOfWeek.atStartOfDay(), endOfWeek.atTime(23, 59, 59))
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "이번주 주간 목표가 생성되지 않았습니다."));

        List<String> schedules = weeklyScheduleRepository.findByWeeklyGoal(weeklyGoal)
                .stream()
                .map(WeeklySchedule::getTrainingContent)
                .toList();

        log.info("주간목표 조회 완료: {}", weeklyGoal.getWeeklyGoal());
        log.info("주간목표 조회 완료: {}", String.join(", ", schedules));

        // 4. 현재 날씨 조회
        WeatherResponseDto currentWeather = weatherService.getCurrentWeather(lat, lon);

        // 5. 최근 10회 기록 중 최대 러닝 시간 계산 (초 단위 저장값을 분 단위로 변환)
        int currentMaxDuration = activityRecordRepository.findTop10ByUserOrderByRecordDateDesc(user).stream()
                .mapToInt(ActivityRecord::getRunningDuration)
                .max().orElse(0) / 60;

        // 6. 최근 7회 기록 중 평균 페이스, 심박수
        List<ActivityRecord> recentActivities = activityRecordRepository.findTop7ByUserOrderByRecordDateDesc(user);
        int avgHeartRate = (int) recentActivities.stream()
                .mapToInt(ActivityRecord::getAvgHeartRate)
                .average()
                .orElse(0);

        int avgPace = (int) recentActivities.stream()
                .mapToInt(ActivityRecord::getAvgPace)
                .average()
                .orElse(0);

        // 6. OpenAI에 미션 생성 요청 (주간목표, 주간스케줄, 컨디션, 날씨 전달)
        log.info("OpenAI 요청 시작");

        MissionAiResult aiResult = openAiService.getStructuredCompletion(
                buildSystemPrompt(), buildMissionPrompt(user, condition, weeklyGoal, schedules, currentWeather, currentMaxDuration, avgHeartRate, avgPace), MissionAiResult.class);

        log.info("OpenAI 응답 완료");

        if (aiResult == null || aiResult.getRecommendedIntensity() == null) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "AI 미션 생성에 실패했습니다.");
        }

        // 7. 응답 미션 저장
        Mission mission = missionRepository.findByConditionAndMissionDate(condition, today)

                .map(existing -> {
                    existing.update(today, aiResult.getRecommendedIntensity(), aiResult.getRecommendedTime(),
                            aiResult.getRecommendedZone(), aiResult.getRecommendedZoneDesc(), aiResult.getDetailComment());
                    existing.setIsRest(aiResult.getIsRest());
                    return existing;
                })
                .orElseGet(() -> {
                    Mission newMission = new Mission(condition, weeklyGoal, today,
                            aiResult.getRecommendedIntensity(), aiResult.getRecommendedTime(),
                            aiResult.getRecommendedZone(), aiResult.getRecommendedZoneDesc(), aiResult.getDetailComment());
                    newMission.setIsRest(aiResult.getIsRest());
                    if(aiResult.getIsRest()) newMission.setIsCompleted(true);
                    return newMission;
                });
        Mission savedMission = missionRepository.save(mission);

        log.info("미션 저장 완료");

        // 8. 응답 반환
        return MissionResponseDto.from(savedMission);
    }

    public MissionResponseDto.Status getTodayMission(long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당하는 유저가 없습니다."));

        LocalDate today = LocalDate.now();

        Condition condition = conditionRepository.findByUserAndConditionDate(user, today)
                .orElse(null);

        if (condition == null) {
            return MissionResponseDto.Status.noCondition();
        }

        Mission mission = missionRepository.findByConditionAndMissionDate(condition, today)
                .orElse(null);

        if (mission == null) {
            return MissionResponseDto.Status.noMission();
        }

        return MissionResponseDto.Status.hasMission(MissionResponseDto.from(mission));
    }

    private String buildSystemPrompt() {
        return """
                 사용자의 컨디션, 날씨, 주간 목표를 고려하여 오늘의 일일 러닝 미션을 생성해주세요.
                 반드시 아래 JSON 형식으로만 응답하세요. 다른 텍스트나 마크다운은 포함하지 마세요.
                 
                 [규칙]
                  - "완전히", "절대", "항상" 등 극단적이고 단정적인 표현은 사용하지 마세요.
                  - 권장 운동 시간은 사용자의 1회 운동 가능 시간을 초과하지 않아야 합니다.
                  - 권장 운동 강도와 러닝 존은 주간 목표와 주간 스케줄에 맞게 설정해주세요.
                  - 오늘의 컨디션, 최근 운동 상태, 통증 부위, 피로도 분석을 참고하여 미션에 반영해주세요.
                  - 컨디션 상태를 최우선으로 고려하고, 최근 평균 심박수와 평균 페이스는 체력 수준 파악을 위한 보조 지표로만 활용하세요.
                  - 컨디션이 좋을 때에 한해, 심박수가 낮고 페이스가 빠른 경우 강도를 높여주세요.
                  - recommendedIntensity는 반드시 "~강도 러닝" 형식으로 끝내주세요. 단, 저강도인 경우 "저강도 러닝 (조깅)" 형식으로 작성해주세요.
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

    private String buildMissionPrompt(User user, Condition condition,
                                   WeeklyGoal weeklyGoal, List<String> schedules, WeatherResponseDto currentWeather,
                                   int currentMaxDuration, int avgHeartRate, int avgPace) {
        String maxDurationText = currentMaxDuration == 0 ? "기록 없음" : currentMaxDuration + "분";
        return String.format("""
                        사용자 정보: %s, %d세, %.1fkg 최근 최대 러닝 지속 시간: %s
                        몸 상태: %s
                        수면: %s
                        오늘의 컨디션 분석: %s
                        주간 목표: %s
                        주간 스케줄: %s
                        주간 운동 가능 횟수: %d회
                        1회 운동 가능 시간: %d분
                        최근 7일 평균 심박수: %d
                        최근 7일 평균 페이스: %d
                        현재 날씨: 기온 %.1f°C, 습도 %d%%, 날씨 %s
                         """,
                user.getGender(),
                user.getAge(),
                user.getWeight(),
                maxDurationText,
                condition.getBodyCondition().getDescription(),
                condition.getSleepQuality().getDescription(),
                condition.getConditionFeedback(),
                weeklyGoal.getWeeklyGoal(),
                schedules,
                weeklyGoal.getFutureGoal().getWeeklyFrequency(),
                weeklyGoal.getFutureGoal().getAvailableTime(),
                avgHeartRate,
                avgPace,
                currentWeather.getTemp(),
                currentWeather.getHumidity(),
                currentWeather.getWeatherDesc()
        );
    }
}
