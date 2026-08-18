package com.likelion14.runcovery.common;

import com.likelion14.runcovery.common.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OpenAiService {

    private final ChatClient chatClient;

    // 모든 도메인 호출에 공통으로 적용되는 기본 지침 (도메인별 systemPrompt 앞에 항상 붙음)
    private static final String BASE_SYSTEM_PROMPT = """
            당신은 RunCovery의 AI 어시스턴트입니다. RunCovery는 러닝 기록과 컨디션을 분석해
            러닝부터 회복, 피부 관리까지 연결하는 통합 웰니스 러닝 서비스입니다.
            모든 응답은 한국어로 작성하세요.
            모든 조언은 의학적 진단이나 치료가 아닌 웰니스 가이드입니다. 의료적 진단·처방을 내리는
            표현(예: "~질환입니다", "~치료가 필요합니다")은 사용하지 말고, 생활 습관 관점의 권장사항으로 안내하세요.
            문장은 친근한 '해요체'로 작성하세요. 예: "오늘은 휴식이 필요해요." (O) / "오늘은 휴식이 필요합니다." (X)
            단, 코드/ID/숫자처럼 단답형 값이 필요한 필드는 이 말투 규칙을 적용하지 않아도 됩니다.
            """;

    public OpenAiService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public <T> T getStructuredCompletion(String systemPrompt, String userPrompt, Class<T> responseType) {
        try {
            return chatClient.prompt()
                    .system(BASE_SYSTEM_PROMPT + "\n" + systemPrompt)
                    .user(userPrompt)
                    .call()
                    .entity(responseType);
        } catch (Exception e) {
            log.error("OpenAI 호출 실패: {}", e.getMessage());
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "AI 응답 생성에 실패했습니다.");
        }
    }

    public String getTextCompletion(String systemPrompt, String userPrompt) {
        try {
            return chatClient.prompt()
                    .system(BASE_SYSTEM_PROMPT + "\n" + systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("OpenAI 호출 실패: {}", e.getMessage());
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "AI 응답 생성에 실패했습니다.");
        }
    }
}
