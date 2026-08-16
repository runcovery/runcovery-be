package com.likelion14.runcovery.activity;

import com.likelion14.runcovery.common.ApiResponse;
import com.likelion14.runcovery.common.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/activities")
@RequiredArgsConstructor
@Tag(name = "5. Activity", description = "러닝 활동기록 동기화/조회 API")
public class ActivityController {

    private final ActivityService activityService;

    @Operation(summary = "활동/데이터 동기화")
    @PostMapping("/sync")
    public ApiResponse<ActivitySyncResponseDto> syncActivity(
            @CurrentUserId Long userId,
            @RequestBody @Valid ActivityRequestDto request) {
        ActivitySyncResponseDto activity = activityService.syncActivity(userId, request);
        return ApiResponse.ok(activity);
    }

    @Operation(summary = "활동/조회")
    @GetMapping("/today")
    public ApiResponse<ActivityRecordResponseDto> getTodayActivity(@CurrentUserId Long userId) {
        ActivityRecordResponseDto result = activityService.getTodayActivity(userId);
        if (result == null) {
            return ApiResponse.ok(null, "오늘 러닝 기록이 없습니다.");
        }
        return ApiResponse.ok(result);
    }
}
