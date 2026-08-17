package com.likelion14.runcovery.user;

import com.likelion14.runcovery.activity.ActivityRecord;
import com.likelion14.runcovery.activity.ActivityRecordRepository;
import com.likelion14.runcovery.common.OpenAiService;
import com.likelion14.runcovery.common.exception.CustomException;
import com.likelion14.runcovery.condition.ConditionRepository;
import com.likelion14.runcovery.goal.WeeklyGoal;
import com.likelion14.runcovery.goal.WeeklyGoalRepository;
import com.likelion14.runcovery.mission.MissionRepository;
import com.likelion14.runcovery.mission.Mission;
import com.likelion14.runcovery.wellness.entity.Prescription;
import com.likelion14.runcovery.wellness.entity.SkinRecord;
import com.likelion14.runcovery.wellness.enums.PrescriptionCategory;
import com.likelion14.runcovery.wellness.enums.SkinRecordType;
import com.likelion14.runcovery.wellness.repository.PrescriptionRepository;
import com.likelion14.runcovery.wellness.repository.SkinRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final WeeklyGoalRepository weeklyGoalRepository;
    private final ActivityRecordRepository activityRecordRepository;
    private final MissionRepository missionRepository;
    private final ConditionRepository conditionRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final SkinRecordRepository skinRecordRepository;
    private final OpenAiService openAiService;

    private final Random random = new Random();

    private static final int ACTIVITY_SEED_DAYS = 14;
    private static final int SKIN_SEED_DAYS = 7;
    private static final double SEED_JITTER_PERCENT = 10.0;
    private static final double DEMO_LAT = 37.5665;
    private static final double DEMO_LON = 126.9780;

    public UserCreateResponseDto createUser(UserCreateRequestDto request) {
        if (userRepository.findByPublicId(request.getUserId()).isPresent()) {
            throw new CustomException(HttpStatus.CONFLICT, "이미 등록된 userId입니다");
        }
        User user = new User(
                request.getUserId(),
                request.getNickname(),
                request.getAge(),
                request.getGender(),
                request.getHeight(),
                request.getWeight(),
                request.getRunningExperience()
        );
        User savedUser = userRepository.save(user);
        seedDemoData(savedUser);
        return new UserCreateResponseDto(savedUser);
    }

    // 심사/데모용 초기 데이터: 실패해도 회원가입 자체는 막지 않는다.
    private void seedDemoData(User user) {
        try {
            seedActivityRecords(user);
            seedSkinRecords(user);
        } catch (RuntimeException exception) {
            log.error("데모 데이터 생성 실패: userId={}", user.getId(), exception);
        }
    }

    private void seedActivityRecords(User user) {
        LocalDate today = LocalDate.now();
        int baseDistance = 1000 + random.nextInt(7001);
        int basePace = 300 + random.nextInt(211);
        int baseAvgHeartRate = 130 + random.nextInt(46);

        for (int offset = ACTIVITY_SEED_DAYS - 1; offset >= 0; offset--) {
            LocalDate recordDate = today.minusDays(offset);

            int distanceM = clamp(jitterPercent(baseDistance, SEED_JITTER_PERCENT), 1000, 8000);
            int avgPace = clamp(jitterPercent(basePace, SEED_JITTER_PERCENT), 300, 510);
            int avgHeartRate = clamp(baseAvgHeartRate + jitterAbsolute(8), 130, 175);
            int maxHeartRate = avgHeartRate + 15 + random.nextInt(11);
            int runningDuration = (int) Math.round(distanceM / 1000.0 * avgPace);
            int calories = (int) Math.round(distanceM * 0.065);
            int cadence = 155 + random.nextInt(21);

            LocalDateTime startTime = recordDate.atTime(7, random.nextInt(60));
            LocalDateTime endTime = startTime.plusSeconds(runningDuration);

            activityRecordRepository.save(new ActivityRecord(
                    user, runningDuration, recordDate, distanceM, avgPace, avgHeartRate, maxHeartRate,
                    calories, cadence, startTime, endTime, DEMO_LAT, DEMO_LON));
        }
    }

    private void seedSkinRecords(User user) {
        LocalDate today = LocalDate.now();
        for (int offset = 1; offset <= SKIN_SEED_DAYS; offset++) {
            LocalDate measuredDate = today.minusDays(offset);
            saveSkinRecord(user, SkinRecordType.AFTER_RUN, measuredDate);
            saveSkinRecord(user, SkinRecordType.AFTER_CARE, measuredDate);
        }
    }

    private void saveSkinRecord(User user, SkinRecordType type, LocalDate measuredDate) {
        int redness = randomSkinScore();
        int oiliness = randomSkinScore();
        int texture = randomSkinScore();
        int pores = randomSkinScore();
        int blemishes = randomSkinScore();
        int hydration = randomSkinScore();
        int pigment = randomSkinScore();

        SkinRecord skinRecord = new SkinRecord(user, type, measuredDate,
                redness, oiliness, texture, pores, blemishes, hydration, pigment);
        skinRecord.setTotalScore(Math.round(
                (redness + oiliness + texture + pores + blemishes + hydration + pigment) / 7.0f));
        skinRecordRepository.save(skinRecord);
    }

    private int randomSkinScore() {
        return 40 + random.nextInt(51);
    }

    private int jitterPercent(int base, double percent) {
        double factor = 1 + (random.nextDouble() * 2 - 1) * percent / 100.0;
        return (int) Math.round(base * factor);
    }

    private int jitterAbsolute(int maxDelta) {
        return random.nextInt(maxDelta * 2 + 1) - maxDelta;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public UserResponseDto getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 유저가 존재하지 않습니다"));
        return new UserResponseDto(user);
    }

    @Transactional
    public MyStatsResponseDto getMyStats(long userId) {

        LocalDate today = LocalDate.now();
        LocalDate start = today.with(DayOfWeek.MONDAY);
        LocalDate end = today.with(DayOfWeek.SUNDAY);
        LocalDate startOfMonth = today.withDayOfMonth(1);

        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 유저가 존재하지 않습니다."));

        // 주간 목표 조회
        WeeklyGoal weeklyGoal = weeklyGoalRepository.findTopByUserOrderByWeekNoDesc(user).orElse(null);
        int totalCalories = weeklyGoal != null ? weeklyGoal.getExpectedCalories() : 0;

        // 주간 소모 칼로리
        int burnedCalories = activityRecordRepository.sumCaloriesByCompletedMissionsThisWeek(user, start, end);

        // 주간 미션 현황
        List<Mission> completedMissions = missionRepository.findByConditionUserAndMissionDateBetweenAndIsCompletedTrue(user, start, end);
        List<String> successDays = completedMissions.stream()
                .map(m -> m.getMissionDate().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase())
                .toList();
        MyStatsResponseDto.WeeklyMissionStats weeklyMission = new MyStatsResponseDto.WeeklyMissionStats(completedMissions.size(), successDays);

        // 이번주 컨디션 달성률
        int totalCondition = conditionRepository.countByUserAndConditionDateBetween(user, start, end);
        int checkedCondition = conditionRepository.countByUserAndConditionDateBetweenAndIsCheckedTrue(user, start, end);
        int conditionRate = totalCondition == 0 ? 0 : (int) Math.min((double) checkedCondition / totalCondition * 100, 100);

        // 이번주 피부 달성률
        int totalSkin = prescriptionRepository.countBySkinRecordUserAndPrescriptionDateBetweenAndCategory(user, start, end, PrescriptionCategory.SKIN);
        int completedSkin = prescriptionRepository.countBySkinRecordUserAndPrescriptionDateBetweenAndCategoryAndIsCompletedTrue(user, start, end, PrescriptionCategory.SKIN);
        int skinRate = totalSkin == 0 ? 0 : (int) Math.min((double) completedSkin / totalSkin * 100, 100);

        // 이번주 스트레칭 달성률
        int totalStretch = prescriptionRepository.countBySkinRecordUserAndPrescriptionDateBetweenAndCategory(user, start, end, PrescriptionCategory.STRETCH);
        int completedStretch = prescriptionRepository.countBySkinRecordUserAndPrescriptionDateBetweenAndCategoryAndIsCompletedTrue(user, start, end, PrescriptionCategory.STRETCH);
        int stretchRate = totalStretch == 0 ? 0 : (int) Math.min((double) completedStretch / totalStretch * 100, 100);

        // 이번달 피부 점수
        List<SkinRecord> skinRecords = skinRecordRepository
                .findByUserAndTypeAndMeasuredDateBetweenOrderByMeasuredDateAsc(user, SkinRecordType.AFTER_CARE, startOfMonth, today);
        List<MyStatsResponseDto.SkinScore> monthlySkinScores = skinRecords.stream()
                .map(s -> new MyStatsResponseDto.SkinScore(s.getMeasuredDate().getDayOfMonth(), s.getTotalScore()))
                .toList();

        // 사후관리 피드백 (AI)
        List<Prescription> skinPrescriptions = prescriptionRepository.findBySkinRecordUserAndPrescriptionDateBetweenAndCategory(user, start, end, PrescriptionCategory.SKIN);
        List<Prescription> stretchPrescriptions = prescriptionRepository.findBySkinRecordUserAndPrescriptionDateBetweenAndCategory(user, start, end, PrescriptionCategory.STRETCH);

        String postCareFeedback = (skinPrescriptions.isEmpty() && stretchPrescriptions.isEmpty())
                ? "이번주 사후관리 처방 내역이 없어요. 러닝 후 리포트를 받아보세요."
                : openAiService.getTextCompletion(buildSystemPrompt(),
                buildPostCareFeedbackPrompt(conditionRate, skinPrescriptions, stretchPrescriptions));

        MyStatsResponseDto.PostCareStats postCare = new MyStatsResponseDto.PostCareStats(
                conditionRate, skinRate, stretchRate, postCareFeedback);

        return new MyStatsResponseDto(userId, user.getNickname(), totalCalories, burnedCalories, weeklyMission, postCare, monthlySkinScores);
    }

    private String buildSystemPrompt() {
        return """
            사용자의 이번주 사후관리 현황을 바탕으로 한 문장으로 피드백을 생성해주세요.
            반드시 한 문장으로만 응답하세요. 다른 텍스트나 마크다운은 포함하지 마세요.
            전문 트레이너가 말하듯 신뢰감 있고 간결한 어투로 작성하세요.
            "같아요", "것 같아요" 같은 불확실한 표현은 사용하지 마세요.
                """;
    }

    private String buildPostCareFeedbackPrompt(int conditionRate,
                                               List<Prescription> skinPrescriptions,
                                               List<Prescription> stretchPrescriptions) {
        String skinDetail = skinPrescriptions.stream()
                .map(p -> String.format("- %s (수행: %s)", p.getSummary(), p.getIsCompleted() ? "O" : "X"))
                .collect(Collectors.joining("\n"));

        String stretchDetail = stretchPrescriptions.stream()
                .map(p -> String.format("- %s (수행: %s)", p.getSummary(), p.getIsCompleted() ? "O" : "X"))
                .collect(Collectors.joining("\n"));

        long skinCompleted = skinPrescriptions.stream().filter(p -> p.getIsCompleted()).count();
        long stretchCompleted = stretchPrescriptions.stream().filter(p -> p.getIsCompleted()).count();
        int totalCompleted = (int)(skinCompleted + stretchCompleted);
        int totalAll = skinPrescriptions.size() + stretchPrescriptions.size();

        return String.format("""
            이번주 사후관리 처방 및 수행 결과:
            - 컨디션 체크 달성률: %d%%
            
            피부 관리:
            %s
            
            스트레칭:
            %s
            
            전체 처방 %d개 중 %d개를 수행했습니다.
            수행한 것이 더 많으면 잘했다고 칭찬하고, 못한 것이 더 많으면 못한 처방 내용을 구체적으로 언급하며 격려해주세요.
            반드시 한 문장으로 생성해주세요.
            """,
                conditionRate, skinDetail, stretchDetail, totalAll, totalCompleted
        );
    }
}
