package com.likelion14.runcovery.home;

import com.likelion14.runcovery.common.ApiResponse;
import com.likelion14.runcovery.common.CurrentUserId;
import com.likelion14.runcovery.common.weather.WeatherResponseDto;
import com.likelion14.runcovery.common.weather.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/home")
@RequiredArgsConstructor
public class HomeController {
    private final HomeService homeService;

    @Operation(summary = "홈", tags = "1. User")
    @GetMapping("")
    public ApiResponse<HomeResponseDto> getHome(
            @CurrentUserId Long userId,
            @Parameter(example = "37.5665") @RequestParam Double lat,
            @Parameter(example = "126.9780") @RequestParam Double lon
    ){
        HomeResponseDto response = homeService.getHome(userId, lat, lon);
        return ApiResponse.ok(response);
    }
}
