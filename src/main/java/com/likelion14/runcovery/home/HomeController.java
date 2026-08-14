package com.likelion14.runcovery.home;

import com.likelion14.runcovery.common.ApiResponse;
import com.likelion14.runcovery.common.weather.WeatherResponseDto;
import com.likelion14.runcovery.common.weather.WeatherService;
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

    @GetMapping("")
    public ApiResponse<HomeResponseDto> getHome(@RequestParam Double lat, @RequestParam Double lon){
        HomeResponseDto response = homeService.getHome(lat, lon);
        return ApiResponse.ok(response);
    }
}
