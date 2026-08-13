package com.likelion14.runcovery.goal;

import com.likelion14.runcovery.common.AppConstants;
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
    public ApiResponse<ScenesResponseDto> recommendScenesByProfile() {
        return ApiResponse.ok(goalService.recommendScenesByProfile(AppConstants.DEFAULT_USER_ID));
    }

    // 러닝 계획(수치) 기반 미래 장면 추천
    @PostMapping("/future/scenes/recommend/plan")
    public ApiResponse<ScenesResponseDto> recommendScenesByPlan(
            @Valid @RequestBody FuturePlanRequestDto request) {
        return ApiResponse.ok(goalService.recommendScenesByPlan(AppConstants.DEFAULT_USER_ID, request));
    }

    // 선택한 장면 기반 미래목표 수치 추천
    @PostMapping("/future/plan/recommend")
    public ApiResponse<PlanRecommendResponseDto> recommendPlan(
            @Valid @RequestBody SelectedSceneRequestDto request) {
        return ApiResponse.ok(goalService.recommendPlanByScene(AppConstants.DEFAULT_USER_ID, request));
    }

    // 미래목표 저장
    @PostMapping("/future")
    public ResponseEntity<ApiResponse<FutureGoalResponseDto>> saveFutureGoal(
            @Valid @RequestBody FutureGoalSaveRequestDto request) {
        FutureGoalResponseDto response = goalService.saveFutureGoal(AppConstants.DEFAULT_USER_ID, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, HttpStatus.CREATED));
    }

    // 미래목표 조회
    @GetMapping("/future")
    public ApiResponse<FutureGoalResponseDto> getFutureGoal() {
        return ApiResponse.ok(goalService.getFutureGoal(AppConstants.DEFAULT_USER_ID));
    }

    // 주간목표 생성
    @PostMapping("/weekly/generate")
    public ResponseEntity<ApiResponse<WeeklyGoalResponseDto>> generateWeeklyGoal() {
        WeeklyGoalResponseDto response = goalService.generateWeeklyGoal(AppConstants.DEFAULT_USER_ID);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, HttpStatus.CREATED));
    }

    // 이번 주 주간목표 조회
    @GetMapping("/weekly/current")
    public ApiResponse<WeeklyGoalResponseDto> getCurrentWeeklyGoal() {
        return ApiResponse.ok(goalService.getCurrentWeeklyGoal(AppConstants.DEFAULT_USER_ID));
    }
}
