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
import jakarta.transaction.Transactional;
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

    @Transactional
    public ConditionResponseDto analyzeCondition(long userId, ConditionRequestDto request) {

        // 1. 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당하는 유저가 없습니다."));

        // 2. 최근 4일 운동 현황 조회
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(3);
        //    - 운동 완료 횟수
        int completedCount = activityRecordRepository.findByUserAndRecordDateBetween(user, start, today).size();
        //    - 마지막 운동일
        LocalDate lastRunDate = activityRecordRepository.findTopByUserOrderByRecordDateDesc(user)
                .map(ActivityRecord::getRecordDate)
                .orElse(null);
        String lastRunDateStr = lastRunDate != null ? lastRunDate.toString() : "운동 기록 없음";

        log.info("운동 완료 횟수 : {}, 마지막 운동일 : {}", completedCount, lastRunDate);

        // 3. condition entity 생성 및 저장, 컨디션 체크 여부 업데이트
        log.info("기존 컨디션 조회 결과: {}", conditionRepository.findByUserAndConditionDate(user, today).isPresent());

        Condition condition = conditionRepository.findByUserAndConditionDate(user, today)
                .map(existing -> {
                    existing.update(request.getSleepQuality(), request.getBodyCondition());
                    return existing;
                })
                .orElseGet(() -> new Condition(user, today, request.getSleepQuality(), request.getBodyCondition()));
        conditionRepository.save(condition);

        // 4. OpenAI 응답을 임시로 conditionTitle, conditionFeedback만 파싱
        ConditionResponseDto result = openAiService.getStructuredCompletion(
                buildSystemPrompt(), buildConditionPrompt(user, request, completedCount, lastRunDateStr), ConditionResponseDto.class);

        // 5. 분석 결과 저장
        try {
            condition.updateAnalysis(result.conditionTitle(), objectMapper.writeValueAsString(result.conditionFeedback()));
        } catch (JsonProcessingException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "컨디션 피드백 변환에 실패했습니다.");
        }

        conditionRepository.save(condition);

        // 6. 분환 결과 반환
        return new ConditionResponseDto(user.getId(), condition.getConditionDate(), result.conditionTitle(), result.conditionFeedback());
}

    public ConditionResponseDto getLatestCondition(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당하는 유저가 없습니다."));

        LocalDate today = LocalDate.now();

        Condition condition = conditionRepository.findByUserAndConditionDate(user, today)
                .or(() -> conditionRepository.findFirstByUserOrderByConditionDateDesc(user))
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "기존 컨디션 기록이 없습니다."));

        try {
            List<String> feedback = objectMapper.readValue(condition.getConditionFeedback(), new TypeReference<List<String>>() {});
            return new ConditionResponseDto(user.getId(), condition.getConditionDate(), condition.getConditionTitle(), feedback);
        } catch (JsonProcessingException e) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "피드백 변환에 실패했습니다.");
        }
    }

    private String buildSystemPrompt() {
        return """
                    사용자의 컨디션 정보를 분석하여 오늘의 컨디션을 요약해주세요.
                    반드시 아래 JSON 형식으로만 응답하세요. 다른 텍스트는 포함하지 마세요.
                    
                    [규칙]
                    - 우선순위: 통증 부위 > 몸 상태 > 수면 상태 순으로 컨디션 타이틀을 결정하세요.
                    - 운동 횟수나 상태를 언급할 때 "~일 수 있어요", "~것 같아요" 등 추정하는 표현을 사용하세요.
                    - "계속", "항상", "만성", "완전히", "절대", "반드시" 등 단정적이거나 지속성을 암시하는 표현은 사용하지 마세요.
                    - 오늘의 상태를 부드럽고 공감하는 어투로 표현하세요.
                    - "~일 수 있어요", "~것 같아요" 등 추정하는 표현을 사용하세요.
                    - "계속", "항상", "만성", "완전히", "절대", "반드시" 등 단정적 표현은 사용하지 마세요.
                    - "완전 방전", "완전히" 등 극단적 표현은 사용하지 마세요.
                    - 수치(운동 횟수, 수면 시간 등)를 직접 언급하지 마세요.
                    - 피드백 3개가 서로 상충되지 않도록 일관성 있게 작성하세요.
                    - 몸 상태가 EXHAUSTED이면 수면이 좋아도 "수면은 충분했지만 몸이 피곤할 수 있어요" 식으로 전체 컨디션에 맞게 표현하세요.
                    - conditionTitle이 휴식을 권장하면 conditionItems도 휴식/회복 방향으로 일관되게 작성하세요.
                    - conditionTitle은 conditionItems의 전반적인 내용을 참고해야해요
                    
                    {
                      "conditionItems": [
                        "수면 상태를 한 줄로. 몸 상태가 EXHAUSTED이면 반드시 '수면은 충분했지만 몸이 피곤할 수 있어요.' 형식으로 작성하세요. 몸 상태가 GOOD이면 '수면이 충분해서 개운하게 시작할 수 있을 것 같아요.' 형식으로 작성하세요.",
                        "몸 상태와 최근 운동 기반 회복 상태를 한 줄로 (예: 몸이 많이 지쳐있어 오늘은 가볍게 움직이는 게 좋을 것 같아요.)",
                        "통증 부위가 있다면 러닝에 미치는 영향을 한 줄로, 없다면 아픈 곳이 없다는 것만 표현 (예 통증 있음: 종아리 쪽 불편함이 러닝 중 페이스에 영향을 줄 수 있어요. / 예 통증 없음: 아픈 곳이 없어서 다행이에요.)"
                      ],
                      "conditionTitle": "위 conditionItems 3개를 종합해서 아래 3가지 중 하나로 표현하세요. (예 좋음: 오늘 컨디션이 최고예요! / 예 보통: 오늘 컨디션이 무난해요! / 예 나쁨: 오늘 컨디션이 좋지 않아요!)"
                    }
                """;
    }

    private String buildConditionPrompt(User user, ConditionRequestDto request,
                                   int completedCount, String lastRunDate) {
        return String.format("""
            사용자 정보: %s, %d세, %.1fkg,
            몸 상태: %s
            수면: %s
            통증 부위: %s
            최근 4일 운동 : %d회
            마지막 운동일: %s
            """,
                user.getGender(),
                user.getAge(),
                user.getWeight(),
                request.getBodyCondition().getDescription(),
                request.getSleepQuality().getDescription(),
                request.getPainAreas(),
                completedCount,
                lastRunDate
        );
    }
}
