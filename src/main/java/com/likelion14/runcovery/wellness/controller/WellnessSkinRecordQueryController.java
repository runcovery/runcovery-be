package com.likelion14.runcovery.wellness.controller;
import com.likelion14.runcovery.wellness.dto.SkinRecordResponseDto;
import com.likelion14.runcovery.wellness.service.WellnessSkinRecordQueryService;

import com.likelion14.runcovery.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "7. Wellness Skin", description = "웰니스 피부 스캔/기록/비교 API")
public class WellnessSkinRecordQueryController {

    private final WellnessSkinRecordQueryService wellnessSkinRecordQueryService;

    @Operation(summary = "웰니스/피부기록 조회")
    @GetMapping("/skin/records")
    public ApiResponse<List<SkinRecordResponseDto>> getSkinRecords(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.ok(wellnessSkinRecordQueryService.getRecords(userId, date));
    }
}

