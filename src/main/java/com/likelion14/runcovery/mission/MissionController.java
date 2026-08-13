package com.likelion14.runcovery.mission;

import com.likelion14.runcovery.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;

    @PostMapping("/generate")
    public ApiResponse<MissionResponseDto> generateMission(@RequestParam Double lat, @RequestParam Double lon) {
        return ApiResponse.ok(missionService.generateMission(lat, lon));
    }
}
