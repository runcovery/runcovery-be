package com.likelion14.runcovery.goal;

import com.likelion14.runcovery.common.AppConstants;
import com.likelion14.runcovery.common.ApiResponse;
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
    public ApiResponse<ScenesResponseDto> recommendScenesByProfile() {
        return ApiResponse.ok(goalService.recommendScenesByProfile(AppConstants.DEFAULT_USER_ID));
    }

    @Operation(summary = "미래목표/수치기반 장면 추천", tags = "2. Goal")
    @PostMapping("/future/scenes/recommend/plan")
    public ApiResponse<ScenesResponseDto> recommendScenesByPlan(
            @Valid @RequestBody FuturePlanRequestDto request) {
        return ApiResponse.ok(goalService.recommendScenesByPlan(AppConstants.DEFAULT_USER_ID, request));
    }

    @Operation(summary = "미래목표/수치 추천", tags = "2. Goal")
    @PostMapping("/future/plan/recommend")
    public ApiResponse<PlanRecommendResponseDto> recommendPlan(
            @Valid @RequestBody SelectedSceneRequestDto request) {
        return ApiResponse.ok(goalService.recommendPlanByScene(AppConstants.DEFAULT_USER_ID, request));
    }

    @Operation(summary = "미래목표/저장", tags = "2. Goal")
    @PostMapping("/future")
    public ResponseEntity<ApiResponse<FutureGoalResponseDto>> saveFutureGoal(
            @Valid @RequestBody FutureGoalSaveRequestDto request) {
        FutureGoalResponseDto response = goalService.saveFutureGoal(AppConstants.DEFAULT_USER_ID, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, HttpStatus.CREATED));
    }

    @Operation(summary = "미래목표/조회", tags = "2. Goal")
    @GetMapping("/future")
    public ApiResponse<FutureGoalResponseDto> getFutureGoal() {
        return ApiResponse.ok(goalService.getFutureGoal(AppConstants.DEFAULT_USER_ID));
    }

    @Operation(summary = "주간목표/생성", tags = "2. Goal")
    @PostMapping("/weekly/generate")
    public ResponseEntity<ApiResponse<WeeklyGoalResponseDto>> generateWeeklyGoal() {
        WeeklyGoalResponseDto response = goalService.generateWeeklyGoal(AppConstants.DEFAULT_USER_ID);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, HttpStatus.CREATED));
    }

    @Operation(summary = "주간목표/이번주 조회", tags = "2. Goal")
    @GetMapping("/weekly/current")
    public ApiResponse<WeeklyGoalResponseDto> getCurrentWeeklyGoal() {
        return ApiResponse.ok(goalService.getCurrentWeeklyGoal(AppConstants.DEFAULT_USER_ID));
    }
}
