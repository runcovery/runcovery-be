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
    private final WeatherService weatherService;

    @GetMapping("")
    public ApiResponse<WeatherResponseDto> getWeatherInfo(
            @RequestParam Double lat,
            @RequestParam Double lon){
        WeatherResponseDto weather = weatherService.getCurrentWeather(lat, lon);
        return ApiResponse.ok(weather);
    }
}
