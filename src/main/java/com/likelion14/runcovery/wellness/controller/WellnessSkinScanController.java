package com.likelion14.runcovery.wellness.controller;

import com.likelion14.runcovery.common.ApiResponse;
import com.likelion14.runcovery.common.CurrentUserId;
import com.likelion14.runcovery.wellness.dto.SkinRecordResponseDto;
import com.likelion14.runcovery.wellness.enums.SkinRecordType;
import com.likelion14.runcovery.wellness.service.WellnessSkinScanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "7. Wellness Skin", description = "피부 스캔, 날짜별 기록 조회, AFTER_CARE 전날 비교 API")
public class WellnessSkinScanController {

    private final WellnessSkinScanService wellnessSkinScanService;

    @Operation(
            summary = "[4] 운동 후/관리 후 피부 스캔",
            description = """
                    Swagger 우측 상단 Authorize에 사용자의 public UUID를 입력한 뒤 실행합니다.
                    Request body는 multipart/form-data이며 파일 항목 이름은 반드시 file이어야 합니다.
                    type은 AFTER_RUN(운동 후) 또는 AFTER_CARE(관리 후)입니다.
                    이미지는 Python 피부 분석 서버의 image 항목으로 전달되고, 점수만 DB에 저장됩니다.
                    응답에는 내부 userId와 이미지 파일명이 포함되지 않습니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "피부 분석 및 저장 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "파일 누락, 빈 파일, 잘못된 type 또는 분석 점수 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "X-Public-Id 누락 또는 유효하지 않은 사용자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "413", description = "업로드 허용 크기 초과"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "415", description = "지원하지 않는 Content-Type"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "Python 피부 분석 서버 연결 또는 응답 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "504", description = "Python 피부 분석 서버 응답 시간 초과")
    })
    @PostMapping(value = "/skin/scan", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<SkinRecordResponseDto> scanSkin(
            @CurrentUserId Long userId,
            @Parameter(
                    description = "스캔 시점. AFTER_RUN은 리포트 생성에 사용하고 AFTER_CARE는 전날 비교에 사용합니다.",
                    example = "AFTER_RUN",
                    schema = @Schema(allowableValues = {"AFTER_RUN", "AFTER_CARE"})
            )
            @RequestParam(name = "type", defaultValue = "AFTER_RUN") SkinRecordType type,
            @Parameter(
                    description = "분석할 얼굴 이미지. 항목 이름은 반드시 file로 유지합니다.",
                    required = true,
                    schema = @Schema(type = "string", format = "binary")
            )
            @RequestPart("file") MultipartFile image
    ) {
        return ApiResponse.ok(SkinRecordResponseDto.from(
                wellnessSkinScanService.scanAndSave(userId, type, image)
        ));
    }
}