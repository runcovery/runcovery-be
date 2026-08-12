package com.likelion14.runcovery.goal;

import com.likelion14.runcovery.common.ApiResponse;
import jakarta.validation.Valid;
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

    // 러닝 계획(수치) 기반 미래 장면 추천
    @PostMapping("/future/scenes/recommend/plan")
    public ApiResponse<ScenesResponseDto> recommendScenesByPlan(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody FuturePlanRequestDto request) {
        return ApiResponse.ok(goalService.recommendScenesByPlan(userId, request));
    }

    // 선택한 장면 기반 미래목표 수치 추천
    @PostMapping("/future/plan/recommend")
    public ApiResponse<PlanRecommendResponseDto> recommendPlan(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody SelectedSceneRequestDto request) {
        return ApiResponse.ok(goalService.recommendPlanByScene(userId, request));
    }
}
