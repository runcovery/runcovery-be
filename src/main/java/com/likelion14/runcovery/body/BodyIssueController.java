package com.likelion14.runcovery.body;

import com.likelion14.runcovery.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/body-issues")
@RequiredArgsConstructor
public class BodyIssueController {

    private final BodyIssueService bodyIssueService;

    // 통증부위 조회
    @GetMapping
    public ApiResponse<BodyIssueListResponseDto> getBodyIssues(@RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.ok(bodyIssueService.getBodyIssues(userId));
    }

    // 통증부위 저장
    @PutMapping
    public ApiResponse<BodyIssueSaveResponseDto> saveBodyIssues(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody BodyIssueSaveRequestDto request
    ) {
        return ApiResponse.ok(bodyIssueService.saveBodyIssues(userId, request));
    }
}
