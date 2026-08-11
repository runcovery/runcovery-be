package com.likelion14.runcovery.goal;

import com.likelion14.runcovery.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    // 프로필 기반 미래 장면 추천
    @PostMapping("/future/scenes/recommend/profile")
    public ApiResponse<ScenesResponseDto> recommendScenesByProfile(@RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.ok(goalService.recommendScenesByProfile(userId));
    }
}
