package com.likelion14.runcovery.goal;

import com.likelion14.runcovery.activity.ActivityRecord;
import com.likelion14.runcovery.activity.ActivityRecordRepository;
import com.likelion14.runcovery.common.OpenAiService;
import com.likelion14.runcovery.common.exception.CustomException;
import com.likelion14.runcovery.user.User;
import com.likelion14.runcovery.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final UserRepository userRepository;
    private final FutureGoalRepository futureGoalRepository;
    private final WeeklyGoalRepository weeklyGoalRepository;
    private final WeeklyScheduleRepository weeklyScheduleRepository;
    private final ActivityRecordRepository activityRecordRepository;
    private final OpenAiService openAiService;

    private static final long WEEKLY_GOAL_VALID_DAYS = 7;

    public ScenesResponseDto recommendScenesByProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 유저가 존재하지 않습니다"));

        return openAiService.getStructuredCompletion(
                buildSystemPrompt(), buildUserPrompt(user), ScenesResponseDto.class);
    }

    public ScenesResponseDto recommendScenesByPlan(Long userId, FuturePlanRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 유저가 존재하지 않습니다"));

        return openAiService.getStructuredCompletion(
                buildPlanSystemPrompt(), buildPlanUserPrompt(user, request), ScenesResponseDto.class);
    }

    public PlanRecommendResponseDto recommendPlanByScene(Long userId, SelectedSceneRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 유저가 존재하지 않습니다"));

        RecommendedPlanDto plan = openAiService.getStructuredCompletion(
                buildPlanRecommendSystemPrompt(), buildPlanRecommendUserPrompt(user, request), RecommendedPlanDto.class);

        int baselineVolume = plan.getTargetDistance() * plan.getTargetPeriod()
                * plan.getWeeklyFrequency() * plan.getAvailableTime();

        return new PlanRecommendResponseDto(plan.getTargetDistance(), plan.getTargetPeriod(),
                plan.getWeeklyFrequency(), plan.getAvailableTime(), baselineVolume, plan.getReason());
    }

    public FutureGoalResponseDto saveFutureGoal(Long userId, FutureGoalSaveRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 유저가 존재하지 않습니다"));

        FutureGoal futureGoal = new FutureGoal(user, request.getScene(), request.getTargetDistance(),
                request.getTargetPeriod(), request.getWeeklyFrequency(), request.getAvailableTime());

        FutureGoal savedFutureGoal = futureGoalRepository.save(futureGoal);
        return new FutureGoalResponseDto(savedFutureGoal);
    }

    public FutureGoalResponseDto getFutureGoal(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 유저가 존재하지 않습니다"));

        FutureGoal futureGoal = futureGoalRepository.findFirstByUserOrderByIdDesc(user)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "설정된 미래 목표가 없습니다"));

        return new FutureGoalResponseDto(futureGoal);
    }

    public WeeklyGoalResponseDto generateWeeklyGoal(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 유저가 존재하지 않습니다"));

        FutureGoal futureGoal = futureGoalRepository.findFirstByUserOrderByIdDesc(user)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "설정된 미래 목표가 없습니다"));

        return createWeeklyGoal(user, futureGoal);
    }

    public WeeklyGoalResponseDto getCurrentWeeklyGoal(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 유저가 존재하지 않습니다"));

        FutureGoal futureGoal = futureGoalRepository.findFirstByUserOrderByIdDesc(user)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "설정된 미래 목표가 없습니다"));

        Optional<WeeklyGoal> latestWeeklyGoal = weeklyGoalRepository.findTopByFutureGoalOrderByWeekNoDesc(futureGoal);
        if (latestWeeklyGoal.isEmpty() || isExpired(latestWeeklyGoal.get())) {
            return createWeeklyGoal(user, futureGoal);
        }

        WeeklyGoal currentWeeklyGoal = latestWeeklyGoal.get();
        List<WeeklySchedule> schedules = weeklyScheduleRepository.findByWeeklyGoal(currentWeeklyGoal);

        return new WeeklyGoalResponseDto(currentWeeklyGoal, schedules);
    }

    private boolean isExpired(WeeklyGoal weeklyGoal) {
        return weeklyGoal.getCreatedAt().isBefore(LocalDateTime.now().minusDays(WEEKLY_GOAL_VALID_DAYS));
    }

    private WeeklyGoalResponseDto createWeeklyGoal(User user, FutureGoal futureGoal) {
        int currentMaxDistance = activityRecordRepository.findTop10ByUserOrderByRecordDateDesc(user).stream()
                .mapToInt(ActivityRecord::getDistanceM)
                .max().orElse(0) / 1000;
        Optional<WeeklyGoal> previousWeeklyGoal = weeklyGoalRepository.findTopByFutureGoalOrderByWeekNoDesc(futureGoal);
        int weekNo = previousWeeklyGoal.map(WeeklyGoal::getWeekNo).orElse(0) + 1;
        int totalWeeks = futureGoal.getTargetPeriod() * 4;

        RecommendedWeeklyGoalDto recommended = openAiService.getStructuredCompletion(
                buildWeeklyGoalSystemPrompt(futureGoal.getWeeklyFrequency()),
                buildWeeklyGoalUserPrompt(user, futureGoal, currentMaxDistance, weekNo, totalWeeks, previousWeeklyGoal),
                RecommendedWeeklyGoalDto.class);

        WeeklyGoal savedWeeklyGoal = weeklyGoalRepository.save(new WeeklyGoal(user, futureGoal, weekNo,
                recommended.getWeeklyGoal(), recommended.getWeeklyGoalDistance(), recommended.getExpectedCalories()));

        List<WeeklySchedule> savedSchedules = weeklyScheduleRepository.saveAll(
                recommended.getSchedules().stream()
                        .map(item -> new WeeklySchedule(savedWeeklyGoal, item.getTrainingContent()))
                        .toList());

        return new WeeklyGoalResponseDto(savedWeeklyGoal, savedSchedules);
    }

    private String buildSystemPrompt() {
        return """
            당신은 러닝 코치이자 동기부여 전문가입니다. 사용자의 프로필 정보를 바탕으로,
            사용자가 러닝을 통해 이루고 싶어할 만한 미래의 모습(장면)을 추천합니다.
            - scenes 배열은 반드시 정확히 3개(main, alt_1, alt_2)여야 합니다.
            - 각 장면 객체의 sceneId 필드 값은 반드시 문자열 "main", "alt_1", "alt_2"를 순서대로 그대로 사용하세요.
              절대 "1", "2", "3" 같은 다른 값으로 바꾸지 마세요.
            - scene: 사용자가 이루고 싶은 미래 모습을 짧게 표현한 명사구 캡션(20자 내외)입니다.
              완결된 문장이 아닌 단답형 제목이므로 "해요체" 규칙을 적용하지 말고, "~해요/~어요/~돼요" 같은 서술어 어미를 쓰지 마세요.
              예: "친구와 나란히 뛰며 지치지 않는 나" (O) / "친구와 나란히 뛰어요" (X)
            - reason: 반드시 "[사용자 프로필 특징]한 {nickname}님에게는 이런 장면을 추천드려요!" 형식의 완결된 문장으로 작성하세요.
              예: "기본 체력이 아직 부족한 민수님에게는 이런 장면을 추천드려요!"
              "[사용자 프로필 특징]"은 같은 scenes 항목의 scene 내용과 실제로 연결되는 이유여야 합니다.
              (예: scene이 "대회에서 완주하는 나"라면, reason은 완주/도전과 관련된 프로필 특징을 언급해야 합니다.)
              단, 프로필의 숫자·단위(분, 시간, kg, cm 등)를 절대 그대로 읽어주듯 인용하지 마세요.
              reason 문장 안에 숫자와 단위가 그대로 등장하면 안 됩니다. 그 의미를 자연스러운 말로 풀어서 표현하세요.
              예: "최대 연속 러닝 시간이 30분인" (X) → "아직 오래 달리는 게 익숙하지 않은" (O)
              예: "7시간의 평균 수면 시간을 갖고 있는" (X) → "수면 컨디션이 안정적인" (O)
              main, alt_1, alt_2의 reason은 각각 서로 다른 프로필 특징(예: 체력, 수면, 러닝 경험 등)에
              초점을 맞춰서 작성하고, 3개가 동일하거나 거의 비슷한 문장이 되지 않게 하세요.
            """;
    }

    private String buildUserPrompt(User user) {
        return """
            다음은 사용자의 프로필입니다:
            - 닉네임: %s
            - 나이: %d세
            - 성별: %s
            - 러닝 경험: %s
            - 1회 최대 연속 러닝 지속 시간: %d분
            - 평균 수면 시간: %s시간
            - 키: %scm, 몸무게: %skg

            이 프로필을 참고하여 동기부여가 될 만한 미래의 모습(장면) 3가지를 추천해주세요.
            """.formatted(user.getNickname(), user.getAge(), user.getGender(), user.getRunningExperience(),
                user.getMaxRunDuration(), user.getAvgSleepHours(), user.getHeight(), user.getWeight());
    }

    private String buildPlanSystemPrompt() {
        return """
            당신은 러닝 코치이자 동기부여 전문가입니다. 사용자의 프로필 정보와 사용자가 직접 설정한
            러닝 목표 계획(목표 거리, 목표 달성 기간, 주간 운동 횟수, 1회 가능 시간)을 함께 바탕으로,
            사용자가 러닝을 통해 이루고 싶어할 만한 미래의 모습(장면)을 추천합니다.
            - scenes 배열은 반드시 정확히 3개(main, alt_1, alt_2)여야 합니다.
            - 각 장면 객체의 sceneId 필드 값은 반드시 문자열 "main", "alt_1", "alt_2"를 순서대로 그대로 사용하세요.
              절대 "1", "2", "3" 같은 다른 값으로 바꾸지 마세요.
            - scene: 사용자가 이루고 싶은 미래 모습을 짧게 표현한 명사구 캡션(20자 내외)입니다.
              완결된 문장이 아닌 단답형 제목이므로 "해요체" 규칙을 적용하지 말고, "~해요/~어요/~돼요" 같은 서술어 어미를 쓰지 마세요.
              예: "친구와 나란히 뛰며 지치지 않는 나" (O) / "친구와 나란히 뛰어요" (X)
            - reason: 반드시 "[사용자 프로필/계획 특징]한 {nickname}님에게는 이런 장면을 추천드려요!" 형식의 완결된 문장으로 작성하세요.
              예: "10km 완주를 목표로 꾸준히 준비 중인 민수님에게는 이런 장면을 추천드려요!"
              "[사용자 프로필/계획 특징]"은 같은 scenes 항목의 scene 내용과 실제로 연결되는 이유여야 하며,
              프로필 특징(체력, 수면, 러닝 경험 등)뿐 아니라 계획 특징(목표 거리, 준비 기간, 주간 빈도, 1회 가능 시간)도 활용할 수 있습니다.
              단, 프로필/계획의 숫자·단위(분, 시간, kg, cm, km, 개월, 회 등)를 절대 그대로 읽어주듯 인용하지 마세요.
              reason 문장 안에 숫자와 단위가 그대로 등장하면 안 됩니다. 그 의미를 자연스러운 말로 풀어서 표현하세요.
              예: "목표 기간이 3개월인" (X) → "짧은 기간 안에 목표를 이루려는" (O)
              예: "3개월 간의 목표를 위해 운동하려는" (X) → "정해진 기간 안에 꾸준히 노력하려는" (O)
              예: "주 2회 운동 목표를 가진" (X) / "주 3회 꾸준히 운동하며" (X) → "무리하지 않는 페이스를 지키려는" (O)
              예: "목표 거리인 5km를 완주하려는" (X) → "새로운 거리에 도전하려는" (O)
              예: "1회 20분씩 운동 가능한" (X) → "짧은 시간을 알차게 활용하려는" (O)
              main, alt_1, alt_2의 reason은 각각 서로 다른 특징(예: 체력, 수면, 목표 거리, 준비 기간 등)에
              초점을 맞춰서 작성하고, 3개가 동일하거나 거의 비슷한 문장이 되지 않게 하세요.
            """;
    }

    private String buildPlanUserPrompt(User user, FuturePlanRequestDto request) {
        return """
            다음은 사용자의 프로필입니다:
            - 닉네임: %s
            - 나이: %d세
            - 성별: %s
            - 러닝 경험: %s
            - 1회 최대 연속 러닝 지속 시간: %d분
            - 평균 수면 시간: %s시간
            - 키: %scm, 몸무게: %skg

            다음은 사용자가 설정한 러닝 목표 계획입니다:
            - 목표 거리: %dkm
            - 목표 달성 기간: %d개월
            - 주간 운동 목표 횟수: %d회
            - 1회 운동 시 투자 가능한 시간: %d분

            이 프로필과 계획을 참고하여 동기부여가 될 만한 미래의 모습(장면) 3가지를 추천해주세요.
            """.formatted(user.getNickname(), user.getAge(), user.getGender(), user.getRunningExperience(),
                user.getMaxRunDuration(), user.getAvgSleepHours(), user.getHeight(), user.getWeight(),
                request.getTargetDistance(), request.getTargetPeriod(),
                request.getWeeklyFrequency(), request.getAvailableTime());
    }

    private String buildPlanRecommendSystemPrompt() {
        return """
            당신은 러닝 코치이자 목표 설계 전문가입니다. 사용자의 프로필 정보와, 사용자가 방금 선택한
            미래의 모습(장면)과 그 이유를 바탕으로, 이 사용자에게 적합한 러닝 목표 수치를 추천합니다.
            - targetDistance: 목표 거리(km, 정수). 3~42 사이의 현실적인 값으로 추천하세요.
            - targetPeriod: 목표 달성 기간(개월, 정수). 1~12 사이의 현실적인 값으로 추천하세요.
            - weeklyFrequency: 주간 운동 목표 횟수(회, 정수). 1~7 사이의 현실적인 값으로 추천하세요.
            - availableTime: 1회 운동 시 투자 가능한 시간(분, 정수). 10~120 사이의 현실적인 값으로 추천하세요.
            - 위 4개 수치는 사용자의 체력·경험·수면 등 프로필과 선택한 장면의 성격(예: 완주/도전형인지,
              친목/즐거움형인지)에 맞게 서로 조화롭게 추천하세요. 지나치게 부담스럽거나 너무 쉬운 목표는 피하세요.
            - reason: 왜 이 4개 수치가 이 사용자에게 적합한지 한 문장으로 설명하세요.
              이 필드는 장면 추천 API의 reason과 달리, 추천한 숫자를 문장에 직접 언급해도 됩니다.
              매번 똑같은 문장 틀("현재 체력과 러닝 경험을 고려했을 때 ~가 적합해요")을 반복하지 말고,
              아래처럼 다양한 표현 방식 중 자연스러운 것을 골라 작성하세요:
              - "현재 체력과 수면 패턴을 고려했을 때 3개월 5km가 적합해요" (수치 요약형)
              - "계단을 올라도 숨이 차지 않으려면 zone2로 꾸준한 운동이 필요해요" (실생활 효과·운동 팁형)
              - "무리 없이 습관을 들이려면 주 3회 30분씩이 딱 맞아요" (습관 형성 강조형)
            """;
    }

    private String buildPlanRecommendUserPrompt(User user, SelectedSceneRequestDto request) {
        return """
            다음은 사용자의 프로필입니다:
            - 닉네임: %s
            - 나이: %d세
            - 성별: %s
            - 러닝 경험: %s
            - 1회 최대 연속 러닝 지속 시간: %d분
            - 평균 수면 시간: %s시간
            - 키: %scm, 몸무게: %skg

            사용자가 방금 선택한 미래의 모습(장면)은 다음과 같습니다:
            - 장면: %s
            - 선택 이유: %s

            이 프로필과 선택한 장면을 참고하여 적합한 러닝 목표 수치(목표 거리, 목표 기간,
            주간 운동 횟수, 1회 가능 시간)와 그 추천 이유를 알려주세요.
            """.formatted(user.getNickname(), user.getAge(), user.getGender(), user.getRunningExperience(),
                user.getMaxRunDuration(), user.getAvgSleepHours(), user.getHeight(), user.getWeight(),
                request.getScene(), request.getReason());
    }

    private String buildWeeklyGoalSystemPrompt(int weeklyFrequency) {
        return """
            당신은 러닝 코치입니다. 사용자의 프로필, 최종 목표(선택한 미래 모습과 목표 수치), 최근 러닝 실력,
            그리고 이번이 전체 계획 중 몇 주차인지를 참고하여 이번 주의 주간 러닝 목표와 훈련 스케줄을 생성합니다.
            - weeklyGoal: 이번 주에 집중할 목표를 설명하는 한 문장. 완결된 문장으로 작성하고, 사용자가 선택한
              미래의 모습(장면)과 어울리는 톤을 유지하되, 장면 문구를 그대로 반복하지는 마세요.
              단, 사용자가 입력한 목표 거리/목표 기간/주간 횟수/1회 가능 시간 수치를 문장에 그대로
              인용하지 마세요. 그 의미를 자연스러운 말로 풀어서 표현하세요.
              예: "6km 목표에 한 발짝 더 다가가요" (X) → "완주까지 한 걸음 더 가까워지고 있어요" (O)
              예: "목표 페이스(5'00\\") 체감하기 및 기초 체력 향상" (O — 입력 수치를 그대로 옮긴 게 아니라
              이번 주에 느껴볼 감각을 설명한 것)
            - weeklyGoalDistance: 이번 주 여러 훈련(schedules)을 모두 합친 총 누적 러닝 거리(km, 정수)입니다.
              **목표 거리(targetDistance)는 한 번에 완주해야 하는 단일 러닝 거리이고, weeklyGoalDistance는
              이번 주 여러 날에 나눠 뛰는 거리의 합**이므로 서로 다른 개념입니다 — weeklyGoalDistance가
              targetDistance보다 커도 전혀 문제 없습니다. 다만 weeklyGoalDistance를 주간 운동 목표
              횟수로 나눈 평균 1회 훈련 거리는, 목표 기간이 끝나갈수록 목표 거리에 점점 가까워져야
              합니다 — 최종 목표는 '한 번에' 목표 거리를 완주하는 것이므로, 후반 주차에는 스케줄 중
              최소 1개가 목표 거리에 근접한 롱런이 되도록 weeklyGoalDistance를 설계하세요. 사용자의
              현재 실력(최근 최대 거리)과 전체 주차 대비 지금이 몇 %% 지점인지를 고려해 점진적으로
              늘려가세요. 초반 주차는 무리하지 않게 낮은 값으로 시작하세요.
            - expectedCalories: 이번 주 훈련을 모두 수행했을 때 예상 소모 칼로리(kcal, 정수).
            - schedules: 정확히 %d개의 훈련 스케줄을 생성하세요. 이 개수는 사용자의 주간 운동 목표 횟수와
              반드시 동일해야 합니다. 각 항목은 "하루 전체 러닝 세션"의 컨셉 하나를 나타냅니다 — 하나의
              훈련을 여러 조각(예: 15분씩 3토막)으로 쪼개서 나열하는 게 아니라, 서로 다른 날에 각각
              한 번씩 진행할 훈련을 날짜 수만큼 만드는 것입니다. 각 항목은 trainingContent(그날 하루의
              훈련 내용) 하나만 포함하세요.
              trainingContent는 가급적 구체적인 시간(분)·거리(km) 숫자보다는 어떤 컨셉/강도의 훈련인지
              위주로 설명하세요 (숫자가 살짝 섞여도 괜찮지만, 문장의 핵심은 훈련 컨셉이어야 합니다).
              예: "가벼운 회복 조깅 15분" 보다는 "가벼운 회복 조깅"
              예: "인터벌 러닝: 1분 빠른 러닝 후 1분 걷기로 두 번 반복하기" 보다는 "짧은 전력 질주와 걷기를
              반복하는 인터벌 러닝"
              날마다 강도·장소가 서로 다르게 구성하고, 최소 1개는 회복/저강도 훈련을 포함하며,
              사용자의 러닝 경험과 체력 수준에 맞게 강도를 조절하세요.
              아래는 참고용 훈련 유형 목록일 뿐입니다. 이 중 매번 다른 조합을 골라 사용자 상황에 맞게
              구체적인 문구로 새로 작성하세요. 절대 아래 문구를 그대로 복사하지 마세요.
              - 고강도: 인터벌 러닝(트랙/트레드밀), 템포런(젖산역치 페이스 유지), 언덕/계단 반복 훈련
              - 중강도: 꾸준한 페이스 조깅(공원/야외), 파틀렉(속도 변화 훈련), LSD(장거리 저속 훈련)
              - 저강도/회복: 가벼운 회복 조깅, 걷기와 가벼운 조깅 번갈아 하기, 스트레칭 중심의 액티브 리커버리
            """.formatted(weeklyFrequency);
    }

    private String buildWeeklyGoalUserPrompt(User user, FutureGoal futureGoal, int currentMaxDistance, int weekNo,
                                              int totalWeeks, Optional<WeeklyGoal> previousWeeklyGoal) {
        int progressPercent = Math.min(100, weekNo * 100 / totalWeeks);
        return """
            다음은 사용자의 프로필입니다:
            - 닉네임: %s
            - 나이: %d세
            - 성별: %s
            - 러닝 경험: %s
            - 1회 최대 연속 러닝 지속 시간: %d분
            - 평균 수면 시간: %s시간
            - 키: %scm, 몸무게: %skg

            다음은 사용자가 선택한 최종 목표입니다:
            - 목표 장면: %s
            - 목표 거리: %dkm
            - 목표 달성 기간: %d개월
            - 주간 운동 목표 횟수: %d회
            - 1회 운동 시 투자 가능한 시간: %d분

            다음은 사용자의 최근 러닝 실력입니다:
            - 최근 10회 러닝 중 최대 거리: %dkm (0이면 아직 러닝 기록이 없는 상태)

            목표 기간은 총 약 %d주이며, 이번 주는 그 중 %d주차(전체 기간의 약 %d%%에 해당)입니다.
            weeklyGoalDistance를 주간 운동 목표 횟수(%d회)로 나눈 평균 1회 훈련 거리가 이 진행률에
            맞춰 목표 거리(%dkm)에 점점 가까워지도록 설계하세요. 진행률이 100%%에 가까운 마지막
            주차라면, 스케줄 중 최소 1개는 목표 거리에 근접한 롱런이어야 합니다.

            %s

            이 정보를 참고하여 이번 주의 주간 러닝 목표와, 주간 운동 목표 횟수(%d회)만큼의 훈련 스케줄을
            추천해주세요. 스케줄 개수는 반드시 주간 운동 목표 횟수와 같아야 합니다.
            """.formatted(user.getNickname(), user.getAge(), user.getGender(), user.getRunningExperience(),
                user.getMaxRunDuration(), user.getAvgSleepHours(), user.getHeight(), user.getWeight(),
                futureGoal.getScene(), futureGoal.getTargetDistance(), futureGoal.getTargetPeriod(),
                futureGoal.getWeeklyFrequency(), futureGoal.getAvailableTime(), currentMaxDistance, totalWeeks,
                weekNo, progressPercent, futureGoal.getWeeklyFrequency(), futureGoal.getTargetDistance(),
                buildPreviousWeekContext(previousWeeklyGoal), futureGoal.getWeeklyFrequency());
    }

    private String buildPreviousWeekContext(Optional<WeeklyGoal> previousWeeklyGoal) {
        if (previousWeeklyGoal.isEmpty()) {
            return "이번이 이 목표에 대한 첫 주간 계획입니다.";
        }
        WeeklyGoal previous = previousWeeklyGoal.get();
        return """
            지난주(%d주차) 계획은 다음과 같았습니다:
            - 지난주 목표 거리: %dkm
            - 지난주 예상 소모 칼로리: %dkcal
            이 흐름을 참고해 이번 주 계획이 급격히 튀지 않고 자연스럽게 이어지도록 하세요. 연속된
            고강도 주간 뒤의 회복처럼 명확한 이유가 없다면, weeklyGoalDistance는 지난주보다 늘어나거나
            최소한 유지되어야 합니다. 특별한 이유 없이 두 주 연속으로 낮추지 마세요.
            """.formatted(previous.getWeekNo(), previous.getWeeklyGoalDistance(), previous.getExpectedCalories());
    }
}
