package com.likelion14.runcovery.condition;

import com.likelion14.runcovery.activity.ActivityRecord;
import com.likelion14.runcovery.activity.ActivityRecordRepository;
import com.likelion14.runcovery.activity.ActivityService;
import com.likelion14.runcovery.common.OpenAiService;
import com.likelion14.runcovery.common.exception.CustomException;
import com.likelion14.runcovery.goal.ScenesResponseDto;
import com.likelion14.runcovery.mission.MissionRepository;
import com.likelion14.runcovery.user.User;
import com.likelion14.runcovery.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConditionService {

    private final ConditionRepository conditionRepository;
    private final UserRepository userRepository;
    private final MissionRepository missionRepository;
    private final ActivityRecordRepository activityRecordRepository;
    private final OpenAiService openAiService;

    public ConditionResponseDto analyzeCondition(ConditionRequestDto request) {

        // 1. 유저 조회
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당하는 유저가 없습니다."));

        // 2. 최근 4일 운동 현황 조회
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(4);
        //    - 운동 완료 횟수 (is_completed=true, is_rest=false)
        int completedCount = missionRepository.findByMissionDateBetweenAndIsCompletedTrueAndIsRestFalse(start, end).size();
        //    - 휴식 횟수 (is_rest=true)
        int restCount = missionRepository.findByMissionDateBetweenAndIsRestTrue(start, end).size();
        //    - 마지막 운동일 (activity_record.record_date 기준)
        LocalDate lastRunDate = activityRecordRepository.findTopByUserOrderByRecordDateDesc(user)
                .map(ActivityRecord::getRecordDate)
                .orElse(null);

        log.info("운동 완료 횟수 : {}, 휴식 횟수 : {}, 마지막 운동일 : {}", completedCount, restCount, lastRunDate);

        // 3. TodayCondition entity 생성 및 저장, 컨디션 체크 여부 업데이트
        LocalDate today = LocalDate.now();
        TodayCondition condition = new TodayCondition(user, today, request.getSleepQuality(), request.getBodyCondition());
        conditionRepository.save(condition);

        // 5. OpenAI에 컨디션 분석 요청 (수면, 운동기록, 통증부위, 몸상태 전달)
        ConditionResponseDto result = openAiService.getStructuredCompletion(
                buildSystemPrompt(), buildUserPrompt(user, request, completedCount, restCount, lastRunDate), ConditionResponseDto.class);


        // 6. 분석 결과로 ConditionResponseDto 반환

        return result;
    }

    private String buildSystemPrompt() {
        return """
            사용자의 컨디션 정보를 분석하여 오늘의 컨디션을 요약해주세요.
            반드시 아래 JSON 형식으로만 응답하세요. 다른 텍스트는 포함하지 마세요.
            {
              "conditionSummary": "한 줄 컨디션 요약 (예: 최고의 컨디션이에요!)",
              "conditionItems": [
                "수면 관련 한 줄 문장",
                "최근 운동 현황 관련 한 줄 문장",
                "통증 부위 관련 한 줄 문장"
              ]
            }
            """;
    }

    private String buildUserPrompt(User user, ConditionRequestDto request,
                                   int completedCount, int restCount, LocalDate lastRunDate) {
        return String.format("""
            몸 상태: %s
            수면: %s
            통증 부위: %s
            최근 4일 운동 완료: %d회
            최근 4일 휴식: %d회
            마지막 운동일: %s
            """,
                request.getBodyCondition().getDescription(),
                request.getSleepQuality().getDescription(),
                request.getPainAreas(),
                completedCount,
                restCount,
                lastRunDate
        );
    }
}
