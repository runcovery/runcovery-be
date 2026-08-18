package com.likelion14.runcovery.common.weather;

import com.likelion14.runcovery.common.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@Service
public class WeatherService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openweather.api.key}")
    private String apiKey;

    public WeatherResponseDto getCurrentWeather(double lat, double lon) {
        // 기본 날씨 정보
        String weatherUrl = "https://api.openweathermap.org/data/4.0/onecall/current"
                + "?lat=" + lat
                + "&lon=" + lon
                + "&units=metric"
                + "&lang=eng"
                + "&appid=" + apiKey;

        // 미세먼지 정보
        String airUrl = "https://api.openweathermap.org/data/2.5/air_pollution"
                + "?lat=" + lat
                + "&lon=" + lon
                + "&appid=" + apiKey;

        try{

            JsonNode weather = objectMapper.readTree(restTemplate.getForObject(weatherUrl, String.class));
            JsonNode air = objectMapper.readTree(restTemplate.getForObject(airUrl, String.class));

            JsonNode data = weather.path("data").get(0);

            log.info("현재 날씨 조회 : {}", data.toString());

            return WeatherResponseDto.builder()
                    .isCurrent(Boolean.TRUE)
                    .lat(lat)
                    .lon(lon)
                    .temp(data.path("temp").asDouble())
                    .feelsLike(data.path("feels_like").asDouble())
                    .humidity(data.path("humidity").asInt())
                    .uvi(data.path("uvi").asDouble())
                    .weatherDesc(data.path("weather").get(0).path("description").asText())
                    .pm25(air.path("list").get(0).path("components").path("pm2_5").asDouble())
                    .build();

        } catch (Exception e){
            log.error("현재 날씨 정보 호출 실패 : {}", e.getMessage());
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "현재 날씨 정보를 가져오는데 실패했습니다.");
        }
    }

    public WeatherResponseDto getPastWeather(LocalDateTime startTime, double lat, double lon) {

        //log.info("startTime: {}", startTime);

        long timestamp = startTime.toEpochSecond(ZoneOffset.of("+9"));

        //log.info("timestamp {} ", timestamp);

        // 과거 날씨 정보
        String weatherUrl = "https://api.openweathermap.org/data/4.0/onecall/timeline/1h"
                + "?lat=" + lat
                + "&lon=" + lon
                + "&start=" + timestamp
                + "&units=metric"
                + "&cnt=" + 1
                + "&appid=" + apiKey;


        try {
            JsonNode weather = objectMapper.readTree(restTemplate.getForObject(weatherUrl, String.class));
            JsonNode data = weather.path("data").get(0);

            log.info("과거 날씨 조회 : {}", data.toString());

            return WeatherResponseDto.builder()
                    .isCurrent(Boolean.FALSE)
                    .lat(lat)
                    .lon(lon)
                    .temp(data.path("temp").asDouble())
                    .feelsLike(data.path("feels_like").asDouble())
                    .humidity(data.path("humidity").asInt())
                    .uvi(data.path("uvi").asDouble())
                    .weatherDesc(data.path("weather").get(0).path("description").asText())
                    .build();

        } catch (Exception e) {
            log.error("과거 날씨 정보 호출 실패 : {}", e.getMessage());
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "과거 날씨 정보를 가져오는데 실패했습니다.");
        }
    }
}
