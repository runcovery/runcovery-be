package com.likelion14.runcovery.mission;

import com.likelion14.runcovery.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;

    @PostMapping("/generate")
    public ApiResponse<MissionResponseDto> generateMission(@RequestParam Double lat, @RequestParam Double lon) {
        return ApiResponse.ok(missionService.generateMission(lat, lon));
    }

    @GetMapping("/today")
    public ApiResponse<MissionResponseDto> getTodayMission() {
        MissionResponseDto result = missionService.getTodayMission();
        if (result == null) {
            return ApiResponse.ok("아직 오늘의 미션이 생성되지 않았습니다.");
        }
        return ApiResponse.ok(result);
    }
}
