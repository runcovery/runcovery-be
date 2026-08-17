package com.likelion14.runcovery.wellness.controller;
import com.likelion14.runcovery.wellness.dto.SkinRecordResponseDto;
import com.likelion14.runcovery.wellness.enums.SkinRecordType;
import com.likelion14.runcovery.wellness.service.WellnessSkinScanService;

import com.likelion14.runcovery.common.ApiResponse;
import com.likelion14.runcovery.common.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/wellness")
@RequiredArgsConstructor
@Tag(name = "7. Wellness Skin", description = "웰니스 피부 스캔/기록/비교 API")
public class WellnessSkinScanController {

    private final WellnessSkinScanService wellnessSkinScanService;

    @Operation(summary = "웰니스/피부스캔")
    @PostMapping(value = "/skin/scan", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<SkinRecordResponseDto> scanSkin(
            @CurrentUserId Long userId,
            @RequestParam(name = "type", defaultValue = "AFTER_RUN") SkinRecordType type,
            @RequestPart("file") MultipartFile image
    ) {
        return ApiResponse.ok(SkinRecordResponseDto.from(
                wellnessSkinScanService.scanAndSave(userId, type, image)
        ));
    }
}

