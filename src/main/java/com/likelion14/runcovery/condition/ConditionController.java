package com.likelion14.runcovery.condition;

import com.likelion14.runcovery.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/conditions")
@RequiredArgsConstructor
public class ConditionController {

    private final ConditionService conditionService;

    @PostMapping
    public ApiResponse<ConditionResponseDto> analyzeCondition (@RequestBody @Valid ConditionRequestDto request) {
        return ApiResponse.ok(conditionService.analyzeCondition(request));
    }

    @GetMapping("/latest")
    public ApiResponse<ConditionResponseDto> getLatestCondition() {
        ConditionResponseDto result = conditionService.getLatestCondition();
        return ApiResponse.ok(result);
    }
}
