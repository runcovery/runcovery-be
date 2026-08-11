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
            - scene: 사용자가 이루고 싶은 미래 모습을 짧게 표현한 명사구 캡션(20자 내외)입니다.
              완결된 문장이 아닌 단답형 제목이므로 "해요체" 규칙을 적용하지 말고, "~해요/~어요/~돼요" 같은 서술어 어미를 쓰지 마세요.
              예: "친구와 나란히 뛰며 지치지 않는 나" (O) / "친구와 나란히 뛰어요" (X)
            - reason: 반드시 "[사용자 프로필 특징]한 {nickname}님에게는 이런 장면을 추천드려요!" 형식의 완결된 문장으로 작성하세요.
              예: "기본 체력이 아직 부족한 민수님에게는 이런 장면을 추천드려요!"
              "[사용자 프로필 특징]"은 같은 scenes 항목의 scene 내용과 실제로 연결되는 이유여야 합니다.
              (예: scene이 "대회에서 완주하는 나"라면, reason은 완주/도전과 관련된 프로필 특징을 언급해야 합니다.)
              단, 프로필의 숫자·단위(분, 시간, kg, cm 등)를 그대로 읽어주듯 인용하지 마세요.
              그 의미를 자연스러운 말로 풀어서 표현하세요.
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
}
