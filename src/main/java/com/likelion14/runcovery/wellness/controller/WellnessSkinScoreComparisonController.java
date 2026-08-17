package com.likelion14.runcovery.wellness.controller;
import com.likelion14.runcovery.wellness.dto.SkinScoreComparisonResponseDto;
import com.likelion14.runcovery.wellness.service.WellnessSkinScoreComparisonService;

import com.likelion14.runcovery.common.ApiResponse;
import com.likelion14.runcovery.common.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "7. Wellness Skin", description = "웰니스 피부 스캔/기록/비교 API")
public class WellnessSkinScoreComparisonController {

    private final WellnessSkinScoreComparisonService wellnessSkinScoreComparisonService;

    /**
     * 기준일과 전날의 AFTER_CARE 피부 점수를 비교합니다.
     * date를 생략하면 오늘을 기준으로 비교합니다.
     */
    @Operation(summary = "웰니스/피부비교")
    @GetMapping({"/comparison", "/compare"})
    public ApiResponse<SkinScoreComparisonResponseDto> compareSkinScores(
            @CurrentUserId Long userId,
            @RequestParam(name = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.ok(
                wellnessSkinScoreComparisonService.compare(userId, date)
        );
    }
}
