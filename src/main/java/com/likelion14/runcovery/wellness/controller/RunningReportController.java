package com.likelion14.runcovery.wellness.controller;
import com.likelion14.runcovery.wellness.dto.ReportRequestDto;
import com.likelion14.runcovery.wellness.dto.ReportResponseDto;
import com.likelion14.runcovery.wellness.service.RunningReportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wellness/reports")
@RequiredArgsConstructor
@Tag(name = "Running Report", description = "맞춤형 웰니스 러닝 리포트 API")
public class RunningReportController {

    private final RunningReportService runningReportService;

    @Operation(
            summary = "맞춤형 웰니스 러닝 리포트 생성",
            description = "지정 날짜에 저장된 러닝·AFTER_RUN 피부·수면 컨디션과 설문·과거 날씨를 분석해 리포트를 저장합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "리포트 생성 및 저장 성공",
                    content = @Content(schema = @Schema(implementation = ReportResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "요청 데이터가 올바르지 않음"),
            @ApiResponse(responseCode = "404", description = "당일 러닝·피부·수면 컨디션 또는 신체 부위 데이터를 찾을 수 없음"),
            @ApiResponse(responseCode = "502", description = "날씨 또는 AI 서비스 응답 오류")
    })
    @PostMapping
    public ResponseEntity<ReportResponseDto> generateReport(
            @RequestBody ReportRequestDto request
    ) {
        ReportResponseDto response = runningReportService.generateAndSaveReport(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
