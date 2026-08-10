package com.likelion14.runcovery.service;

import com.likelion14.runcovery.common.exception.CustomException;
import com.likelion14.runcovery.dto.WeatherResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
public class WeatherService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openweather.api.key}")
    private String apiKey;

    public WeatherResponseDto getCurrentWeather(Double lat, Double lon){
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

            log.info("날씨 조회 : {}", data.toString());

            return WeatherResponseDto.builder()
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
            log.error("날씨 정보 호출 실패: {}", e.getMessage());
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "날씨 정보를 가져오는데 실패했습니다.");
        }
    }
}
