package com.likelion14.runcovery.goal;

import com.likelion14.runcovery.common.ApiResponse;
import com.likelion14.runcovery.common.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "미래목표/프로필기반 장면 추천", tags = "2. Goal")
    @PostMapping("/future/scenes/recommend/profile")
    public ApiResponse<ScenesResponseDto> recommendScenesByProfile(@CurrentUserId Long userId) {
        return ApiResponse.ok(goalService.recommendScenesByProfile(userId));
    }

    @Operation(summary = "미래목표/수치기반 장면 추천", tags = "2. Goal")
    @PostMapping("/future/scenes/recommend/plan")
    public ApiResponse<ScenesResponseDto> recommendScenesByPlan(
            @CurrentUserId Long userId,
            @Valid @RequestBody FuturePlanRequestDto request) {
        return ApiResponse.ok(goalService.recommendScenesByPlan(userId, request));
    }

    @Operation(summary = "미래목표/수치 추천", tags = "2. Goal")
    @PostMapping("/future/plan/recommend")
    public ApiResponse<PlanRecommendResponseDto> recommendPlan(
            @CurrentUserId Long userId,
            @Valid @RequestBody SelectedSceneRequestDto request) {
        return ApiResponse.ok(goalService.recommendPlanByScene(userId, request));
    }

    @Operation(summary = "미래목표/저장", tags = "2. Goal")
    @PostMapping("/future")
    public ResponseEntity<ApiResponse<FutureGoalResponseDto>> saveFutureGoal(
            @CurrentUserId Long userId,
            @Valid @RequestBody FutureGoalSaveRequestDto request) {
        FutureGoalResponseDto response = goalService.saveFutureGoal(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, HttpStatus.CREATED));
    }

    @Operation(summary = "미래목표/조회", tags = "2. Goal")
    @GetMapping("/future")
    public ApiResponse<FutureGoalResponseDto> getFutureGoal(@CurrentUserId Long userId) {
        return ApiResponse.ok(goalService.getFutureGoal(userId));
    }

    @Operation(summary = "주간목표/생성", tags = "2. Goal")
    @PostMapping("/weekly/generate")
    public ResponseEntity<ApiResponse<WeeklyGoalResponseDto>> generateWeeklyGoal(@CurrentUserId Long userId) {
        WeeklyGoalResponseDto response = goalService.generateWeeklyGoal(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, HttpStatus.CREATED));
    }

    @Operation(summary = "주간목표/이번주 조회", tags = "2. Goal")
    @GetMapping("/weekly/current")
    public ApiResponse<WeeklyGoalResponseDto> getCurrentWeeklyGoal(@CurrentUserId Long userId) {
        return ApiResponse.ok(goalService.getCurrentWeeklyGoal(userId));
    }
}
