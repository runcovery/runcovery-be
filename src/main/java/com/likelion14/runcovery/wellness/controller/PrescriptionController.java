package com.likelion14.runcovery.wellness.controller;

import com.likelion14.runcovery.wellness.dto.PrescriptionQueryResponseDto;
import com.likelion14.runcovery.wellness.service.PrescriptionQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/wellness/prescriptions")
@RequiredArgsConstructor
@Tag(name = "Wellness Prescription", description = "맞춤형 웰니스 처방전 조회 API")
public class PrescriptionController {

    private final PrescriptionQueryService prescriptionQueryService;

    @Operation(
            summary = "웰니스 처방전 조회",
            description = "reportId가 없으면 사용자의 최신 리포트에 연결된 처방전을 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "처방전 목록 조회 성공")
    @GetMapping
    public ResponseEntity<List<PrescriptionQueryResponseDto.Summary>> getPrescriptions(
            @RequestParam(defaultValue = "1") Long userId,
            @RequestParam(required = false) Long reportId
    ) {
        return ResponseEntity.ok(prescriptionQueryService.getPrescriptions(userId, reportId));
    }

    @Operation(summary = "웰니스 처방전 상세 조회")
    @ApiResponse(responseCode = "200", description = "처방전 상세 조회 성공")
    @ApiResponse(responseCode = "404", description = "처방전을 찾을 수 없음")
    @GetMapping("/{prescriptionId}")
    public ResponseEntity<PrescriptionQueryResponseDto.Detail> getPrescription(
            @PathVariable Long prescriptionId,
            @RequestParam(defaultValue = "1") Long userId
    ) {
        return ResponseEntity.ok(
                prescriptionQueryService.getPrescription(userId, prescriptionId)
        );
    }
}