package com.likelion14.runcovery.wellness.controller;

import com.likelion14.runcovery.common.CurrentUserId;
import com.likelion14.runcovery.wellness.dto.PrescriptionCompletionRequestDto;
import com.likelion14.runcovery.wellness.dto.PrescriptionQueryResponseDto;
import com.likelion14.runcovery.wellness.enums.PrescriptionCategory;
import com.likelion14.runcovery.wellness.service.PrescriptionQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/wellness/prescriptions")
@RequiredArgsConstructor
@Tag(name = "8. Wellness Prescription", description = "리포트에 연결된 수분/영양·피부·스트레칭 처방전 조회 및 완료 상태 API")
public class PrescriptionController {

    private final PrescriptionQueryService prescriptionQueryService;

    @Operation(
            summary = "[9] 리포트의 카테고리별 처방전 목록 조회",
            description = """
                    리포트 생성 후 호출합니다. reportId를 생략하면 현재 사용자의 최신 리포트를 사용합니다.
                    NUTRITION, SKIN, STRETCH 세 카테고리를 반환합니다.
                    completionSupported는 완료 상태 변경 가능 여부이며 SKIN과 STRETCH만 true입니다.
                    권장 테스트 URL은 /api/wellness/prescriptions?reportId={reportId} 입니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처방전 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "reportId가 1 미만"),
            @ApiResponse(responseCode = "401", description = "X-Public-Id 누락 또는 유효하지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "사용자 소유 리포트 또는 처방전 없음")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PrescriptionQueryResponseDto.Summary>> getPrescriptions(
            @CurrentUserId Long userId,
            @Parameter(description = "리포트 ID. 생략하면 최신 리포트", example = "20")
            @RequestParam(required = false) Long reportId
    ) {
        return ResponseEntity.ok(prescriptionQueryService.getPrescriptions(userId, reportId));
    }

    @Operation(
            summary = "[10] 처방전 상세 조회",
            description = """
                    처방전 ID에 해당하는 카테고리별 상세 내용을 조회합니다.
                    NUTRITION은 운동시간·칼로리, SKIN은 리포트에 연결된 AFTER_RUN 피부 점수,
                    STRETCH는 추천 링크·추천 이유·회복 영상 목록을 반환합니다.
                    다른 사용자의 처방전은 조회할 수 없습니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처방전 상세 조회 성공"),
            @ApiResponse(responseCode = "400", description = "prescriptionId가 1 미만"),
            @ApiResponse(responseCode = "401", description = "X-Public-Id 누락 또는 유효하지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "처방전을 찾을 수 없음")
    })
    @GetMapping(value = "/{prescriptionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PrescriptionQueryResponseDto.Detail> getPrescription(
            @Parameter(description = "목록 조회에서 받은 처방전 ID", required = true, example = "3")
            @PathVariable Long prescriptionId,
            @CurrentUserId Long userId
    ) {
        return ResponseEntity.ok(prescriptionQueryService.getPrescription(userId, prescriptionId));
    }

    @Operation(
            summary = "[11] 피부·스트레칭 처방전 완료 상태 변경",
            description = """
                    SKIN의 '피부진단 보기' 또는 STRETCH의 '영상 보러가기'를 실행할 때 호출합니다.
                    category는 SKIN 또는 STRETCH만 허용되며 NUTRITION 요청은 400을 반환합니다.
                    reportId를 생략하면 최신 리포트를 사용합니다.
                    isCompleted=true는 완료, false는 완료 취소입니다.
                    테스트 URL 예: /api/wellness/prescriptions/SKIN/complete?reportId=20
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "완료 상태 변경 성공"),
            @ApiResponse(responseCode = "400", description = "NUTRITION 요청, 필수 isCompleted 누락 또는 잘못된 파라미터"),
            @ApiResponse(responseCode = "401", description = "X-Public-Id 누락 또는 유효하지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "사용자 소유 리포트 또는 해당 카테고리 처방전 없음")
    })
    @PatchMapping(value = "/{category}/complete", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PrescriptionQueryResponseDto.Completion> updateCompletion(
            @Parameter(
                    description = "완료 처리할 카테고리. NUTRITION은 지원하지 않습니다.",
                    required = true,
                    example = "SKIN",
                    schema = @Schema(allowableValues = {"SKIN", "STRETCH"})
            )
            @PathVariable PrescriptionCategory category,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "완료 상태",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PrescriptionCompletionRequestDto.class),
                            examples = @ExampleObject(value = "{\"isCompleted\": true}")
                    )
            )
            @Valid @RequestBody PrescriptionCompletionRequestDto request,
            @CurrentUserId Long userId,
            @Parameter(description = "리포트 ID. 생략하면 최신 리포트", example = "20")
            @RequestParam(required = false) Long reportId
    ) {
        return ResponseEntity.ok(
                prescriptionQueryService.updateCompletion(userId, reportId, category, request.getIsCompleted())
        );
    }
}