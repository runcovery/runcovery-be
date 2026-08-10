package com.likelion14.runcovery.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponseDto {
    private Double lat;
    private Double lon;
    private Double temp;
    private Double feelsLike;
    private Integer humidity;
    private Double uvi;
    private String weatherDesc;
    private Double pm25;

}
