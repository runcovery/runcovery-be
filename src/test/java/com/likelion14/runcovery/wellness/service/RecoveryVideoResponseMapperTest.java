package com.likelion14.runcovery.wellness.service;

import com.likelion14.runcovery.wellness.dto.ReportResponseDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecoveryVideoResponseMapperTest {

    private final RecoveryVideoResponseMapper mapper = new RecoveryVideoResponseMapper();

    @Test
    void mapsEmptyPainPartsToFullBodyRecommendation() {
        YouTubeVideoSearchService.VideoResult video = new YouTubeVideoSearchService.VideoResult(
                "전신 스트레칭",
                "https://www.youtube.com/watch?v=test",
                "설명",
                120,
                List.of(),
                List.of(),
                List.of()
        );

        ReportResponseDto.RecoveryVideo response = mapper.toResponse(video);

        assertEquals("FULL_BODY", response.getBodyGroup());
        assertEquals("전신 회복 스트레칭 영상", response.getTitle());
        assertTrue(response.getRecommendationReason().contains("전신 이완"));
    }

    @Test
    void explainsWhenFallbackVideoDoesNotDirectlyCoverSelectedPart() {
        YouTubeVideoSearchService.VideoResult video = new YouTubeVideoSearchService.VideoResult(
                "상체 스트레칭",
                "https://www.youtube.com/watch?v=test",
                "설명",
                120,
                List.of("LEFT BACK 위팔(삼두)"),
                List.of(),
                List.of("B_UPPER_ARM_L")
        );

        ReportResponseDto.RecoveryVideo response = mapper.toResponse(video);

        assertEquals("UPPER_BODY", response.getBodyGroup());
        assertEquals("삼두 회복 스트레칭 영상", response.getTitle());
        assertTrue(response.getRecommendationReason().contains("직접 확인되지는 않아"));
    }

    @Test
    void usesLowerBodyGroupForLowerBodyTarget() {
        YouTubeVideoSearchService.VideoResult video = new YouTubeVideoSearchService.VideoResult(
                "무릎 스트레칭",
                "https://www.youtube.com/watch?v=test",
                "설명",
                90,
                List.of("LEFT FRONT 무릎 앞"),
                List.of("F_KNEE_L"),
                List.of()
        );

        ReportResponseDto.RecoveryVideo response = mapper.toResponse(video);

        assertEquals("LOWER_BODY", response.getBodyGroup());
        assertEquals("무릎 앞 회복 스트레칭 영상", response.getTitle());
        assertTrue(response.getRecommendationReason().contains("영상 제목에서 확인"));
    }
}