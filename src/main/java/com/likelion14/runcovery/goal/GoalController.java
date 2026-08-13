package com.likelion14.runcovery.goal;

import com.likelion14.runcovery.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    // 미래목표 저장
    @PostMapping("/future")
    public ResponseEntity<ApiResponse<FutureGoalResponseDto>> saveFutureGoal(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody FutureGoalSaveRequestDto request) {
        FutureGoalResponseDto response = goalService.saveFutureGoal(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, HttpStatus.CREATED));
    }

    // 미래목표 조회
    @GetMapping("/future")
    public ApiResponse<FutureGoalResponseDto> getFutureGoal(@RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.ok(goalService.getFutureGoal(userId));
    }

    // 주간목표 생성
    @PostMapping("/weekly/generate")
    public ResponseEntity<ApiResponse<WeeklyGoalResponseDto>> generateWeeklyGoal(
            @RequestHeader("X-User-Id") Long userId) {
        WeeklyGoalResponseDto response = goalService.generateWeeklyGoal(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, HttpStatus.CREATED));
    }
}
