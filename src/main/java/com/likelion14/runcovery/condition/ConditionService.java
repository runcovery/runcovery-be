package com.likelion14.runcovery.condition;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion14.runcovery.activity.ActivityRecord;
import com.likelion14.runcovery.activity.ActivityRecordRepository;
import com.likelion14.runcovery.common.OpenAiService;
import com.likelion14.runcovery.common.exception.CustomException;
import com.likelion14.runcovery.mission.MissionRepository;
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
public class ConditionService {

    private final ConditionRepository conditionRepository;
    private final UserRepository userRepository;
    private final MissionRepository missionRepository;
    private final ActivityRecordRepository activityRecordRepository;
    private final OpenAiService openAiService;
    private final ObjectMapper objectMapper;

    public ConditionResponseDto analyzeCondition(ConditionRequestDto request) {

        // 1. 유저 조회
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당하는 유저가 없습니다."));

        // 2. 최근 4일 운동 현황 조회
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(4);
        //    - 운동 완료 횟수
        int completedCount = missionRepository.findByMissionDateBetweenAndIsCompletedTrueAndIsRestFalse(start, today).size();
        //    - 휴식 횟수
        int restCount = missionRepository.findByMissionDateBetweenAndIsRestTrue(start, today).size();
        //    - 마지막 운동일
        LocalDate lastRunDate = activityRecordRepository.findTopByUserOrderByRecordDateDesc(user)
                .map(ActivityRecord::getRecordDate)
                .orElse(null);
        String lastRunDateStr = lastRunDate != null ? lastRunDate.toString() : "운동 기록 없음";

        log.info("운동 완료 횟수 : {}, 휴식 횟수 : {}, 마지막 운동일 : {}", completedCount, restCount, lastRunDate);

        // 3. condition entity 생성 및 저장, 컨디션 체크 여부 업데이트
        log.info("기존 컨디션 조회 결과: {}", conditionRepository.findByUserAndConditionDate(user, today).isPresent());

        Condition condition = conditionRepository.findByUserAndConditionDate(user, today)
                .map(existing -> {
                    existing.update(request.getSleepQuality(), request.getBodyCondition());
                    return existing;
                })
                .orElseGet(() -> new Condition(user, today, request.getSleepQuality(), request.getBodyCondition()));
        conditionRepository.save(condition);

        // 4. OpenAI에 컨디션 분석 요청 (수면, 운동기록, 통증부위, 몸상태 전달)
        ConditionResponseDto result = openAiService.getStructuredCompletion(
                buildSystemPrompt(), buildUserPrompt(user, request, completedCount, restCount, lastRunDateStr), ConditionResponseDto.class);

        // 5. 분석 결과 저장
        try {
            condition.updateAnalysis(result.getConditionTitle(), objectMapper.writeValueAsString(result.getConditionFeedback()));
        } catch (JsonProcessingException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "컨디션 피드백 변환에 실패했습니다.");
        }

        conditionRepository.save(condition);

        // 6. 분석 결과 반환
        return new ConditionResponseDto(condition.getConditionDate(), result.getConditionTitle(), result.getConditionFeedback());
    }

    public ConditionResponseDto getLatestCondition() {
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당하는 유저가 없습니다."));

        LocalDate today = LocalDate.now();

        Condition condition = conditionRepository.findByUserAndConditionDate(user, today)
                .or(() -> conditionRepository.findFirstByUserOrderByConditionDateDesc(user))
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "컨디션 기록이 없습니다."));

        try {
            List<String> feedback = objectMapper.readValue(condition.getConditionFeedback(), new TypeReference<List<String>>() {});
            return new ConditionResponseDto(condition.getConditionDate(), condition.getConditionTitle(), feedback);
        } catch (JsonProcessingException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "피드백 변환에 실패했습니다.");
        }
    }

    private String buildSystemPrompt() {
        return """
            사용자의 컨디션 정보를 분석하여 오늘의 컨디션을 요약해주세요.
            반드시 아래 JSON 형식으로만 응답하세요. 다른 텍스트는 포함하지 마세요.
            통증 부위 관련 문장은 "계속", "항상", "만성" 등 지속성을 암시하는 표현을 사용하지 마세요.
            "완전히", "절대", "항상" 등 극단적이고 단정적인 표현은 사용하지 마세요.
            오늘의 상태만 부드럽게 표현해주세요.
            {
              "conditionTitle": "오늘 컨디션을 한 줄로 표현 (예: 최고의 컨디션이에요!)",
              "conditionItems": [
                "수면 시간과 상태를 한 줄로 (예: 수면 7시간 이하, 그럭저럭 잘 잤어요.)",
                "최근 운동 기반 몸 회복 상태 한 줄로 (예: 최근 2일 휴식으로 몸이 가벼울 거예요.)",
                "통증 부위가 있다면 공감 한 줄로, 없다면 아픈 곳이 없어서 좋은 컨디션임을 표현 (예: 아픈 곳이 없어서 최상의 컨디션이에요.)"
              ]
            }
        """;
    }

    private String buildUserPrompt(User user, ConditionRequestDto request,
                                   int completedCount, int restCount, String lastRunDate) {
        return String.format("""
            사용자 정보: %s, %d세, %.1fkg,
            몸 상태: %s
            수면: %s
            통증 부위: %s
            최근 4일 운동 완료: %d회
            최근 4일 휴식: %d회
            마지막 운동일: %s
            """,
                user.getGender(),
                user.getAge(),
                user.getWeight(),
                request.getBodyCondition().getDescription(),
                request.getSleepQuality().getDescription(),
                request.getPainAreas(),
                completedCount,
                restCount,
                lastRunDate
        );
    }
}
