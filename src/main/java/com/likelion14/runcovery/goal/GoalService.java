package com.likelion14.runcovery.goal;

import com.likelion14.runcovery.common.OpenAiService;
import com.likelion14.runcovery.common.exception.CustomException;
import com.likelion14.runcovery.user.User;
import com.likelion14.runcovery.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final UserRepository userRepository;
    private final OpenAiService openAiService;

    public ScenesResponseDto recommendScenesByProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 유저가 존재하지 않습니다"));

        return openAiService.getStructuredCompletion(
                buildSystemPrompt(), buildUserPrompt(user), ScenesResponseDto.class);
    }

    private String buildSystemPrompt() {
        return """
            당신은 러닝 코치이자 동기부여 전문가입니다. 사용자의 프로필 정보를 바탕으로,
            사용자가 러닝을 통해 이루고 싶어할 만한 미래의 모습(장면)을 추천합니다.
            - scenes 배열은 반드시 정확히 3개(main, alt_1, alt_2)여야 합니다.
            - scene: 사용자가 이루고 싶은 미래 모습을 짧고 생생하게 표현한 한 문장(20자 내외).
            - reason: 반드시 "[사용자 프로필 특징]한 {nickname}님에게는 이런 장면을 추천드려요!" 형식으로 작성하세요.
              예: "기본 체력이 아직 부족한 민수님에게는 이런 장면을 추천드려요!"
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
}
