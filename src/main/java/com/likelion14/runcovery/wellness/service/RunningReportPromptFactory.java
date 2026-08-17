package com.likelion14.runcovery.wellness.service;

import com.likelion14.runcovery.activity.ActivityRecord;
import com.likelion14.runcovery.body.BodyPart;
import com.likelion14.runcovery.common.weather.WeatherResponseDto;
import com.likelion14.runcovery.condition.Condition;
import com.likelion14.runcovery.user.User;
import com.likelion14.runcovery.wellness.dto.ReportRequestDto;
import com.likelion14.runcovery.wellness.entity.SkinRecord;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class RunningReportPromptFactory {

    public String buildUserPrompt(ReportRequestDto request, PromptData context) {
        ActivityRecord run = context.activity();
        SkinRecord skin = context.skinRecord();
        WeatherResponseDto weather = context.weather();
        ReportRequestDto.SurveyData survey = request.getSurvey();
        String painParts = context.painfulParts().isEmpty()
                ? "없음"
                : context.painfulParts().stream()
                        .map(this::describeBodyPart)
                        .collect(Collectors.joining(", "));

        return """
                다음은 같은 날짜에 저장된 실제 데이터입니다. 제공되지 않은 수치를 임의로 만들지 마세요.

                [사용자]
                - 성별: %s
                - 나이: %s세
                - 체중: %skg

                [러닝 데이터 - %s]
                - 활동 ID: %s
                - 거리: %sm
                - 운동 시간: %s초
                - 평균 페이스: %s초/km
                - 케이던스: %sspm
                - 평균 심박수: %sbpm
                - 최대 심박수: %sbpm
                - 소모 칼로리: %skcal
                - 시작 시각: %s
                - 종료 시각: %s
                - 위도/경도: %s, %s

                [러닝 당시 과거 날씨]
                - 기온: %s℃
                - 체감온도: %s℃
                - 습도: %s%%
                - 자외선 지수: %s
                - 날씨 설명: %s

                [AFTER_RUN 피부 점수 - 0~100]
                - 총점: %s
                - 홍조(redness): %s
                - 유분(oiliness): %s
                - 피부결(texture): %s
                - 모공(pores): %s
                - 잡티(blemishes): %s
                - 수분(hydration): %s
                - 색소침착(pigment): %s

                [당일 컨디션]
                - 수면 상태: %s
                - 수면 상태 설명: %s
                - 신체 상태: %s
                - 신체 상태 설명: %s

                [운동 후 설문]
                - feeling: %s
                - energy: %s
                - sweat: %s
                - 아픈 부위: %s

                [서버 계산 참고값]
                - 추정 시간당 발한량: %.2fL/h
                - 추정 수분 손실량: 약 %sml
                - 권장 수분 보충량: 약 %sml
                - 이 값은 운동 시간, 땀 설문, 기온, 습도를 이용한 웰니스 추정치이며 의학적 측정값이 아님
                """.formatted(
                context.user().getGender(), context.user().getAge(), context.user().getWeight(),
                run.getRecordDate(), run.getId(), run.getDistanceM(), run.getRunningDuration(), run.getAvgPace(),
                run.getCadence(), run.getAvgHeartRate(), run.getMaxHeartRate(), run.getCalories(),
                run.getStartTime(), run.getEndTime(), run.getLat(), run.getLon(),
                value(weather, WeatherResponseDto::getTemp),
                value(weather, WeatherResponseDto::getFeelsLike),
                value(weather, WeatherResponseDto::getHumidity),
                value(weather, WeatherResponseDto::getUvi),
                value(weather, WeatherResponseDto::getWeatherDesc),
                skin.getTotalScore(), skin.getRedness(), skin.getOiliness(), skin.getTexture(), skin.getPores(),
                skin.getBlemishes(), skin.getHydration(), skin.getPigment(),
                context.condition().getSleepQuality().name(),
                context.condition().getSleepQuality().getDescription(),
                context.condition().getBodyCondition().name(),
                context.condition().getBodyCondition().getDescription(),
                survey.getFeeling().name() + " (" + survey.getFeeling().getDescription() + ")",
                survey.getEnergy().name() + " (" + survey.getEnergy().getDescription() + ")",
                survey.getSweat().name() + " (" + survey.getSweat().getDescription() + ")",
                painParts,
                context.sweatRateLitersPerHour(),
                context.estimatedFluidLossMl(),
                context.recommendedIntakeMl()
        );
    }

    public String buildVerifiedVideosPrompt(List<YouTubeVideoSearchService.VideoResult> videos) {
        StringBuilder videoDetails = new StringBuilder();
        for (int index = 0; index < videos.size(); index++) {
            YouTubeVideoSearchService.VideoResult video = videos.get(index);
            videoDetails.append("\n[").append(index + 1).append("번 검증 영상]")
                    .append("\n- 대상 부위: ").append(String.join(", ", video.targetParts()))
                    .append("\n- 포함된 통증 코드: ").append(String.join(", ", video.coveredPainPartCodes()))
                    .append("\n- 실제 영상 제목: ").append(video.title())
                    .append("\n- 실제 영상 URL: ").append(video.videoUrl())
                    .append("\n- 영상 길이: ").append(video.durationSeconds()).append("초")
                    .append("\n- 영상 설명: ").append(video.description()).append("\n");
        }

        return """

                [서버가 body_part.body_name 기준으로 분류·검증한 회복 영상 - 최대 2개]
                %s
                - 회복 영상 목록과 추천 이유는 서버가 생성합니다.
                - AI는 영상 요약, 동작 단계, URL, 영상 제목을 생성하거나 수정하지 마세요.
                """.formatted(videoDetails);
    }

    public String buildSystemPrompt() {
        return """
                당신은 RunCovery의 데이터 기반 웰니스 러닝 코치입니다.
                입력에 포함된 실제 수치만 근거로 사용하고, 입력마다 결과가 달라지도록 분석하세요.
                고정된 예문을 반복하거나 제공되지 않은 수치·질환·증상을 만들어내지 마세요.

                1. 오늘의 러닝 강도
                - 기온, 체감온도, 습도, 운동시간, 거리, 페이스, 케이던스, 평균·최대 심박수를 핵심 근거로 score를 판단하세요.
                - 수면 상태, 신체 상태, feeling, energy, sweat, AFTER_RUN 피부 점수는 회복 부담과 경고 코멘트를 보정하는 근거로 사용하세요.
                - score 1~3은 LOW, 4~7은 MODERATE, 8~10은 HIGH입니다.
                - comment는 실제 입력값 중 의미 있는 근거를 최소 2개 포함한 180자 이내의 동적인 한글 문장으로 작성하세요.
                - HIGH이거나 수면 부족과 높은 심박수 등 복합 위험 신호가 있을 때만 ⚠️ 경고 표현을 사용하세요.

                2. 맞춤형 웰니스 처방전
                - hydration: 서버가 계산한 추정 수분 손실량, 권장 보충량, 실제 소모 칼로리와 날씨를 활용해 수분/영양 한 줄 솔루션을 작성하세요.
                - skin: 7가지 피부 점수, 기온, 습도, 자외선, sweat 응답을 근거로 한 줄 피부 솔루션을 작성하세요.
                - stretching: 사용자가 선택한 아픈 부위를 우선 고려하되 의료적 치료를 단정하지 않는 한 줄 스트레칭 솔루션을 작성하세요.
                - 각 solution은 핵심 수치를 포함하고 120자 이내 한 문장으로 작성하세요.

                3. 회복 영상 추천
                - 영상은 서버가 body_part.body_name 기준으로 상체·하체를 분류한 뒤 최대 2개까지 결정합니다.
                - AI는 영상의 실제 내용을 분석하거나 요약하지 않습니다.
                - recoveryVideos, 영상 URL, 영상 제목, 동작 단계는 JSON에 포함하지 마세요. 서버가 검증값과 짧은 추천 이유를 추가합니다.

                반드시 아래 구조의 JSON 객체 하나만 반환하세요. Markdown과 추가 설명은 금지합니다.
                {
                  "intensity": {
                    "score": 1,
                    "level": "LOW",
                    "comment": "데이터 근거가 포함된 오늘의 러닝 강도 코멘트"
                  },
                  "hydration": {
                    "title": "수분/영양 제목",
                    "solution": "한 줄 솔루션"
                  },
                  "skin": {
                    "title": "피부 제목",
                    "solution": "한 줄 솔루션"
                  },
                  "stretching": {
                    "title": "스트레칭 제목",
                    "solution": "한 줄 솔루션"
                  }
                }
                """;
    }

    private String describeBodyPart(BodyPart bodyPart) {
        List<String> details = new ArrayList<>();
        if (!isBlank(bodyPart.getSide())) {
            details.add(bodyPart.getSide());
        }
        if (!isBlank(bodyPart.getDirection())) {
            details.add(bodyPart.getDirection());
        }
        String suffix = details.isEmpty() ? "" : " / " + String.join(" / ", details);
        return bodyPart.getBodyName() + suffix + " [" + bodyPart.getBodyPartCode() + "]";
    }

    private <T> Object value(WeatherResponseDto weather, Function<WeatherResponseDto, T> getter) {
        return weather == null ? null : getter.apply(weather);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record PromptData(
            User user,
            ActivityRecord activity,
            SkinRecord skinRecord,
            Condition condition,
            List<BodyPart> painfulParts,
            WeatherResponseDto weather,
            int estimatedFluidLossMl,
            int recommendedIntakeMl,
            double sweatRateLitersPerHour
    ) {
    }
}