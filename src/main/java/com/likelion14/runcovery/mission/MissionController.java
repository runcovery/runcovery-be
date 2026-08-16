package com.likelion14.runcovery.mission;

import com.likelion14.runcovery.common.ApiResponse;
import com.likelion14.runcovery.common.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/missions")
@RequiredArgsConstructor
@Tag(name = "3. Condition", description = "컨디션 체크/오늘의 미션 API")
public class MissionController {

    private final MissionService missionService;

    @Operation(summary = "일일미션 생성")
    @PostMapping("/generate")
    public ApiResponse<MissionResponseDto> generateMission(
            @CurrentUserId Long userId,
            @Parameter(example = "37.5665") @RequestParam Double lat,
            @Parameter(example = "126.9780") @RequestParam Double lon
    ) {
        return ApiResponse.ok(missionService.generateMission(userId, lat, lon));
    }

    @Operation(summary = "일일미션/조회")
    @GetMapping("/today")
    public ApiResponse<MissionResponseDto> getTodayMission(@CurrentUserId Long userId) {
        MissionResponseDto result = missionService.getTodayMission(userId);
        if (result == null) {
            return ApiResponse.ok("아직 오늘의 미션이 생성되지 않았습니다.");
        }
        return ApiResponse.ok(result);
    }
}
