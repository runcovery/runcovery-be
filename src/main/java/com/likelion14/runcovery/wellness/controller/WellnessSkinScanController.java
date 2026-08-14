package com.likelion14.runcovery.wellness.controller;
import com.likelion14.runcovery.wellness.entity.SkinRecord;
import com.likelion14.runcovery.wellness.enums.SkinRecordType;
import com.likelion14.runcovery.wellness.service.WellnessSkinScanService;

import com.likelion14.runcovery.common.ApiResponse;
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
public class WellnessSkinScanController {

    private final WellnessSkinScanService wellnessSkinScanService;

    @PostMapping(value = "/skin/scan", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<SkinRecord> scanSkin(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "type", defaultValue = "AFTER_RUN") SkinRecordType type,
            @RequestPart("file") MultipartFile image
    ) {
        return ApiResponse.ok(wellnessSkinScanService.scanAndSave(userId, type, image));
    }
}

