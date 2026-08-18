package com.likelion14.runcovery.wellness.controller;

import com.likelion14.runcovery.common.CurrentUserId;
import com.likelion14.runcovery.wellness.dto.ReportRequestDto;
import com.likelion14.runcovery.wellness.dto.ReportResponseDto;
import com.likelion14.runcovery.wellness.dto.RunningReportPreviewResponseDto;
import com.likelion14.runcovery.wellness.dto.WellnessReportQueryResponseDto;
import com.likelion14.runcovery.wellness.service.RunningReportPreviewService;
import com.likelion14.runcovery.wellness.service.RunningReportService;
import com.likelion14.runcovery.wellness.service.WellnessReportQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/wellness/reports")
@RequiredArgsConstructor
@Tag(name = "6. Running Report", description = "러닝·컨디션·AFTER_RUN 피부·날씨를 이용한 AI 웰니스 리포트 생성/조회 API")
public class RunningReportController {

    private final RunningReportService runningReportService;
    private final RunningReportPreviewService runningReportPreviewService;
    private final WellnessReportQueryService wellnessReportQueryService;

    @Operation(
            summary = "[6-1] 웰니스 리포트 작성 화면 조회",
            description = """
                    현재 사용자가 소유한 activityRecordId의 러닝 기록을 조회합니다.
                    활동 기록의 startTime과 위도/경도를 기준으로 OpenWeather 과거 날씨를 조회하여
                    자외선 지수, 기온, 습도와 총 거리, 러닝 시간, 평균 페이스, 케이던스를 함께 반환합니다.
                    이 응답을 화면에 표시한 뒤 같은 activityRecordId를 리포트 생성 API에 전달합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "리포트 작성 화면 조회 성공",
                    content = @Content(schema = @Schema(implementation = RunningReportPreviewResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 activityRecordId 또는 시작 시각·위치 누락"),
            @ApiResponse(responseCode = "401", description = "X-Public-Id 누락 또는 유효하지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "다른 사용자의 러닝 기록 접근"),
            @ApiResponse(responseCode = "404", description = "러닝 기록을 찾을 수 없음"),
            @ApiResponse(responseCode = "502", description = "OpenWeather 연결 또는 응답 오류"),
            @ApiResponse(responseCode = "503", description = "OpenWeather API Key 미설정"),
            @ApiResponse(responseCode = "504", description = "OpenWeather 응답 시간 초과")
    })
    @GetMapping(value = "/preview", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RunningReportPreviewResponseDto> getReportPreview(
            @CurrentUserId Long userId,
            @Parameter(description = "화면에 표시할 사용자 소유 러닝 기록 ID", required = true, example = "14")
            @RequestParam Long activityRecordId
    ) {
        return ResponseEntity.ok(runningReportPreviewService.getPreview(userId, activityRecordId));
    }

    @Operation(
            summary = "[7] 맞춤형 웰니스 러닝 리포트 생성 및 저장",
            description = """
                    실행 전 동일 사용자·동일 날짜의 다음 데이터가 필요합니다.
                    1) POST /conditions로 생성한 당일 컨디션
                    2) POST /activities/sync로 저장한 러닝 기록
                    3) POST /wellness/skin/scan?type=AFTER_RUN으로 저장한 피부 기록

                    activityRecordId를 지정하면 해당 사용자 소유의 러닝 기록을 사용합니다.
                    생략하면 recordDate에 저장된 기록 중 리포트 생성 시각과 가장 가까운 기록을 선택합니다.
                    bodyCondition과 sleepQuality 모두 AI 분석에 포함됩니다.
                    같은 러닝 기록으로 다시 요청하면 기존 리포트와 처방전을 갱신합니다.
                    같은 상체 또는 같은 하체 통증은 영상 1개, 상·하체가 모두 있으면 최대 2개를 반환합니다.
                    성공 응답은 ApiResponse 래퍼가 없는 ReportResponseDto이며 HTTP 201입니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "리포트 생성 및 저장 성공",
                    content = @Content(schema = @Schema(implementation = ReportResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "필수 설문 누락, 미래 날짜, 잘못된 통증 코드 또는 요청 형식 오류"),
            @ApiResponse(responseCode = "401", description = "X-Public-Id 누락 또는 유효하지 않은 사용자"),
            @ApiResponse(responseCode = "403", description = "다른 사용자의 activityRecordId 접근"),
            @ApiResponse(responseCode = "404", description = "러닝·AFTER_RUN 피부·당일 컨디션 데이터 없음"),
            @ApiResponse(responseCode = "502", description = "OpenAI, OpenWeather 또는 YouTube 응답 오류"),
            @ApiResponse(responseCode = "503", description = "외부 API Key 미설정"),
            @ApiResponse(responseCode = "504", description = "외부 서비스 응답 시간 초과")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReportResponseDto> generateReport(
            @CurrentUserId Long userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "recordDate와 activityRecordId는 같은 러닝 기록을 가리켜야 합니다. painPartCodes는 body_part의 코드입니다.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ReportRequestDto.class),
                            examples = @ExampleObject(
                                    name = "하체 통증 리포트 생성 예시",
                                    value = """
                                            {
                                              "recordDate": "2026-08-17",
                                              "activityRecordId": 8,
                                              "survey": {
                                                "feeling": "NORMAL",
                                                "energy": "TIRED",
                                                "sweat": "MODERATE"
                                              },
                                              "painPartCodes": [
                                                "F_KNEE_L",
                                                "B_THIGH_R"
                                              ]
                                            }
                                            """
                            )
                    )
            )
            @RequestBody ReportRequestDto request
    ) {
        ReportResponseDto response = runningReportService.generateAndSaveReport(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "[8] 최신 또는 날짜별 웰니스 리포트 조회",
            description = "reportDate를 생략하면 사용자의 최신 리포트를, 지정하면 해당 날짜의 최신 리포트를 조회합니다. 응답의 reportId는 처방전 조회에 사용합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리포트 조회 성공"),
            @ApiResponse(responseCode = "400", description = "reportDate 형식 오류"),
            @ApiResponse(responseCode = "401", description = "X-Public-Id 누락 또는 유효하지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "리포트를 찾을 수 없음")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WellnessReportQueryResponseDto> getReport(
            @CurrentUserId Long userId,
            @Parameter(description = "리포트 날짜(yyyy-MM-dd). 생략하면 최신 리포트", example = "2026-08-17")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportDate
    ) {
        return ResponseEntity.ok(wellnessReportQueryService.getLatestOrByDate(userId, reportDate));
    }

    @Operation(
            summary = "[8-1] reportId로 웰니스 리포트 조회",
            description = "현재 X-Public-Id 사용자가 소유한 리포트만 조회할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리포트 조회 성공"),
            @ApiResponse(responseCode = "400", description = "reportId가 1 미만"),
            @ApiResponse(responseCode = "401", description = "X-Public-Id 누락 또는 유효하지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "리포트를 찾을 수 없음")
    })
    @GetMapping(value = "/{reportId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WellnessReportQueryResponseDto> getReportById(
            @Parameter(description = "조회할 리포트 ID", required = true, example = "20")
            @PathVariable Long reportId,
            @CurrentUserId Long userId
    ) {
        return ResponseEntity.ok(wellnessReportQueryService.getById(userId, reportId));
    }
}