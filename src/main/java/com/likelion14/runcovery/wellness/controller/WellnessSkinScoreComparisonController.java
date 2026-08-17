package com.likelion14.runcovery.wellness.controller;

import com.likelion14.runcovery.common.ApiResponse;
import com.likelion14.runcovery.common.CurrentUserId;
import com.likelion14.runcovery.wellness.dto.SkinScoreComparisonResponseDto;
import com.likelion14.runcovery.wellness.service.WellnessSkinScoreComparisonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/wellness/skin")
@RequiredArgsConstructor
@Tag(name = "7. Wellness Skin", description = "피부 스캔, 날짜별 기록 조회, AFTER_CARE 전날 비교 API")
public class WellnessSkinScoreComparisonController {

    private final WellnessSkinScoreComparisonService wellnessSkinScoreComparisonService;

    @Operation(
            summary = "[6] 전날 대비 AFTER_CARE 피부 점수 비교",
            description = """
                    기준일과 바로 전날의 AFTER_CARE 기록만 비교합니다.
                    date를 생략하면 서버의 오늘 날짜가 기준일입니다.
                    성공하려면 동일 사용자에게 기준일과 기준일-1일의 AFTER_CARE 기록이 모두 있어야 합니다.
                    difference는 기준일 점수 - 전날 점수이며 양수는 점수 상승, 음수는 점수 하락입니다.
                    테스트 URL 예: /wellness/skin/comparison?date=2026-08-17
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "비교 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "date 형식 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "X-Public-Id 누락 또는 유효하지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "기준일 또는 전날 AFTER_CARE 기록 없음")
    })
    @GetMapping("/comparison")
    public ApiResponse<SkinScoreComparisonResponseDto> compareSkinScores(
            @CurrentUserId Long userId,
            @Parameter(description = "비교 기준일(yyyy-MM-dd). 생략하면 오늘", example = "2026-08-17")
            @RequestParam(name = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.ok(wellnessSkinScoreComparisonService.compare(userId, date));
    }
}