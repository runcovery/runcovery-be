package com.likelion14.runcovery.wellness.controller;

import com.likelion14.runcovery.common.ApiResponse;
import com.likelion14.runcovery.common.CurrentUserId;
import com.likelion14.runcovery.wellness.dto.SkinRecordResponseDto;
import com.likelion14.runcovery.wellness.service.WellnessSkinRecordQueryService;
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
import java.util.List;

@RestController
@RequestMapping("/wellness")
@RequiredArgsConstructor
@Tag(name = "7. Wellness Skin", description = "피부 스캔, 날짜별 기록 조회, AFTER_CARE 전날 비교 API")
public class WellnessSkinRecordQueryController {

    private final WellnessSkinRecordQueryService wellnessSkinRecordQueryService;

    @Operation(
            summary = "[5] 날짜별 피부 기록 조회",
            description = """
                    지정 날짜에 저장된 사용자의 AFTER_RUN/AFTER_CARE 피부 기록을 모두 조회합니다.
                    date를 생략하면 서버의 오늘 날짜를 사용합니다.
                    피부 스캔은 서버 오늘 날짜로 저장되므로 Swagger 테스트 시 일반적으로 date를 오늘로 지정합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공. 기록이 없으면 빈 배열 반환"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "date 형식 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "X-Public-Id 누락 또는 유효하지 않은 사용자")
    })
    @GetMapping("/skin/records")
    public ApiResponse<List<SkinRecordResponseDto>> getSkinRecords(
            @CurrentUserId Long userId,
            @Parameter(description = "조회 날짜(yyyy-MM-dd). 생략하면 오늘", example = "2026-08-17")
            @RequestParam(name = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.ok(wellnessSkinRecordQueryService.getRecords(userId, date));
    }
}