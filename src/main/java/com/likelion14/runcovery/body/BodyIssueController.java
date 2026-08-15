package com.likelion14.runcovery.body;

import com.likelion14.runcovery.common.AppConstants;
import com.likelion14.runcovery.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/body-issues")
@RequiredArgsConstructor
@Tag(name = "4. Body Issue", description = "통증부위 등록/조회 API")
public class BodyIssueController {

    private final BodyIssueService bodyIssueService;

    @Operation(summary = "통증부위/조회")
    @GetMapping
    public ApiResponse<BodyIssueListResponseDto> getBodyIssues() {
        return ApiResponse.ok(bodyIssueService.getBodyIssues(AppConstants.DEFAULT_USER_ID));
    }

    @Operation(summary = "통증부위/저장")
    @PutMapping
    public ApiResponse<BodyIssueSaveResponseDto> saveBodyIssues(
            @Valid @RequestBody BodyIssueSaveRequestDto request
    ) {
        return ApiResponse.ok(bodyIssueService.saveBodyIssues(AppConstants.DEFAULT_USER_ID, request));
    }
}
