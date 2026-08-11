package com.likelion14.runcovery.activity;

import com.likelion14.runcovery.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @PostMapping("/sync")
    public ApiResponse<ActivitySyncResponseDto> syncActivity(@RequestBody @Valid ActivityRequestDto request) {
        ActivitySyncResponseDto activity = activityService.syncActivity(request);
        return ApiResponse.ok(activity);
    }

    @GetMapping("/today")
    public ApiResponse<ActivityRecordResponseDto> getTodayActivity() {
        ActivityRecordResponseDto result = activityService.getTodayActivity();
        if (result == null) {
            return ApiResponse.ok(null, "오늘 러닝 기록이 없습니다.");
        }
        return ApiResponse.ok(result);
    }
}
