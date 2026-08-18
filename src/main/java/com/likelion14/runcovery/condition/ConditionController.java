package com.likelion14.runcovery.condition;

import com.likelion14.runcovery.common.ApiResponse;
import com.likelion14.runcovery.common.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/conditions")
@RequiredArgsConstructor
@Tag(name = "3. Condition", description = "컨디션 체크/오늘의 미션 API")
public class ConditionController {

    private final ConditionService conditionService;

    @Operation(
            summary = "컨디션/분석,저장",
            description = """
                    bodyCondition : EXHAUSTED / FAIR / GOOD \n
                    sleepQuality : POOR / FAIR / GOOD
                    """
    )
    @PostMapping
    public ResponseEntity<ApiResponse<ConditionResponseDto>> analyzeCondition (
            @CurrentUserId Long userId,
            @RequestBody @Valid ConditionRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(conditionService.analyzeCondition(userId, request), HttpStatus.CREATED));
    }

    @Operation(summary = "컨디션/조회")
    @GetMapping("/latest")
    public ApiResponse<ConditionResponseDto> getLatestCondition(@CurrentUserId Long userId) {
        return ApiResponse.ok(conditionService.getLatestCondition(userId));
    }
}
