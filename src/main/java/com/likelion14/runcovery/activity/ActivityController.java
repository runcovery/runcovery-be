package com.likelion14.runcovery.activity;

import com.likelion14.runcovery.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
