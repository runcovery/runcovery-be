package com.likelion14.runcovery.user;

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
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
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
                request.getRunningExperience(),
                request.getMaxRunDuration(),
                request.getAvgSleepHours()
        );
        User savedUser = userRepository.save(user);
        return new UserCreateResponseDto(savedUser);
    }

    public UserResponseDto getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 유저가 존재하지 않습니다"));
        return new UserResponseDto(user);
    }

    @Transactional
    public MyStatsResponseDto getMyStats() {

        LocalDate today = LocalDate.now();
        LocalDate start = today.with(DayOfWeek.MONDAY);
        LocalDate end = today.with(DayOfWeek.SUNDAY);
        LocalDate startOfMonth = today.withDayOfMonth(1);

        // 유저 조회
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 유저가 존재하지 않습니다."));

        // 주간 목표 조회
        WeeklyGoal weeklyGoal = weeklyGoalRepository.findTopByUserOrderByWeekNoDesc(user).orElse(null);
        int totalCalories = weeklyGoal != null ? weeklyGoal.getExpectedCalories() : 0;

        // 주간 소모 칼로리
        int burnedCalories = activityRecordRepository.sumCaloriesByCompletedMissionsThisWeek(start, end);

        // 주간 미션 현황
        List<Mission> completedMissions = missionRepository.findByMissionDateBetweenAndIsCompletedTrue(start, end);
        List<String> successDays = completedMissions.stream()
                .map(m -> m.getMissionDate().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase())
                .toList();
        MyStatsResponseDto.WeeklyMissionStats weeklyMission = new MyStatsResponseDto.WeeklyMissionStats(completedMissions.size(), successDays);

        // 이번주 컨디션 달성률
        int totalCondition = conditionRepository.countByUserAndConditionDateBetween(user, start, end);
        int checkedCondition = conditionRepository.countByUserAndConditionDateBetweenAndIsCheckedTrue(user, start, end);
        int conditionRate = totalCondition == 0 ? 0 : (int) Math.min((double) checkedCondition / totalCondition * 100, 100);

        // 이번주 피부 달성률
        int totalSkin = prescriptionRepository.countByPrescriptionDateBetweenAndCategory(start, end, PrescriptionCategory.SKIN);
        int completedSkin = prescriptionRepository.countByPrescriptionDateBetweenAndCategoryAndIsCompletedTrue(start, end, PrescriptionCategory.SKIN);
        int skinRate = totalSkin == 0 ? 0 : (int) Math.min((double) completedSkin / totalSkin * 100, 100);

        // 이번주 스트레칭 달성률
        int totalStretch = prescriptionRepository.countByPrescriptionDateBetweenAndCategory(start, end, PrescriptionCategory.STRETCH);
        int completedStretch = prescriptionRepository.countByPrescriptionDateBetweenAndCategoryAndIsCompletedTrue(start, end, PrescriptionCategory.STRETCH);
        int stretchRate = totalStretch == 0 ? 0 : (int) Math.min((double) completedStretch / totalStretch * 100, 100);

        // 이번달 피부 점수
        List<SkinRecord> skinRecords = skinRecordRepository
                .findByUserAndTypeAndMeasuredDateBetweenOrderByMeasuredDateAsc(user, SkinRecordType.AFTER_CARE, startOfMonth, today);
        List<MyStatsResponseDto.SkinScore> monthlySkinScores = skinRecords.stream()
                .map(s -> new MyStatsResponseDto.SkinScore(s.getMeasuredDate().getDayOfMonth(), s.getTotalScore()))
                .toList();

        // 사후관리 피드백 (AI)
        List<Prescription> skinPrescriptions = prescriptionRepository
                .findByPrescriptionDateBetweenAndCategory(start, end, PrescriptionCategory.SKIN);
        List<Prescription> stretchPrescriptions = prescriptionRepository
                .findByPrescriptionDateBetweenAndCategory(start, end, PrescriptionCategory.STRETCH);

        String postCareFeedback = (skinPrescriptions.isEmpty() && stretchPrescriptions.isEmpty())
                ? "이번주 사후관리 처방 내역이 없어요. 러닝 후 리포트를 받아보세요."
                : openAiService.getTextCompletion(buildSystemPrompt(),
                buildPostCareFeedbackPrompt(conditionRate, skinPrescriptions, stretchPrescriptions));

        MyStatsResponseDto.PostCareStats postCare = new MyStatsResponseDto.PostCareStats(
                conditionRate, skinRate, stretchRate, postCareFeedback);

        return new MyStatsResponseDto(user.getNickname(), totalCalories, burnedCalories, weeklyMission, postCare, monthlySkinScores);
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
