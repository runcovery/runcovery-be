package com.likelion14.runcovery.wellness.controller;
import com.likelion14.runcovery.wellness.dto.SkinRecordResponseDto;
import com.likelion14.runcovery.wellness.service.WellnessSkinRecordQueryService;

import com.likelion14.runcovery.common.ApiResponse;
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
public class WellnessSkinRecordQueryController {

    private final WellnessSkinRecordQueryService wellnessSkinRecordQueryService;

    @GetMapping("/skin/records")
    public ApiResponse<List<SkinRecordResponseDto>> getSkinRecords(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ApiResponse.ok(wellnessSkinRecordQueryService.getRecords(userId, date));
    }
}

