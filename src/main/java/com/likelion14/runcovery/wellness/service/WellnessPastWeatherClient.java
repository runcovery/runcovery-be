package com.likelion14.runcovery.wellness.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion14.runcovery.common.exception.CustomException;
import com.likelion14.runcovery.common.weather.WeatherResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class WellnessPastWeatherClient {

    private static final String PAST_WEATHER_URL =
            "https://api.openweathermap.org/data/4.0/onecall/timeline/1h";

    private final org.springframework.web.reactive.function.client.WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${openweather.api.key}")
    private String apiKey;

    @Value("${wellness.weather.timeout:10s}")
    private Duration requestTimeout;

    public WeatherResponseDto getPastWeather(LocalDateTime startTime, double lat, double lon) {
        if (startTime == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "러닝 시작 시각이 없습니다.");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new CustomException(HttpStatus.SERVICE_UNAVAILABLE, "OPENWEATHER_API_KEY가 설정되지 않았습니다.");
        }

        long timestamp = startTime.atZone(ZoneId.of("Asia/Seoul")).toEpochSecond();

        try {
            String responseBody = webClientBuilder.clone()
                    .build()
                    .get()
                    .uri(PAST_WEATHER_URL, uriBuilder -> uriBuilder
                            .queryParam("lat", lat)
                            .queryParam("lon", lon)
                            .queryParam("start", timestamp)
                            .queryParam("units", "metric")
                            .queryParam("cnt", 1)
                            .queryParam("appid", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(requestTimeout)
                    .block();

            if (responseBody == null || responseBody.isBlank()) {
                throw new CustomException(HttpStatus.BAD_GATEWAY, "과거 날씨 응답이 비어 있습니다.");
            }

            JsonNode data = objectMapper.readTree(responseBody).path("data").path(0);
            if (data.isMissingNode() || data.isNull()) {
                throw new CustomException(HttpStatus.BAD_GATEWAY, "과거 날씨 응답 형식이 올바르지 않습니다.");
            }

            return WeatherResponseDto.builder()
                    .isCurrent(Boolean.FALSE)
                    .lat(lat)
                    .lon(lon)
                    .temp(data.path("temp").asDouble())
                    .feelsLike(data.path("feels_like").asDouble())
                    .humidity(data.path("humidity").asInt())
                    .uvi(data.path("uvi").asDouble())
                    .weatherDesc(data.path("weather").path(0).path("description").asText())
                    .build();
        } catch (CustomException exception) {
            throw exception;
        } catch (WebClientResponseException exception) {
            log.warn("OpenWeather returned HTTP {}", exception.getStatusCode().value());
            throw new CustomException(
                    HttpStatus.BAD_GATEWAY,
                    "과거 날씨 서버가 오류를 반환했습니다. (HTTP "
                            + exception.getStatusCode().value() + ")"
            );
        } catch (WebClientRequestException exception) {
            log.warn("Failed to connect to OpenWeather", exception);
            throw new CustomException(HttpStatus.BAD_GATEWAY, "과거 날씨 서버에 연결할 수 없습니다.");
        } catch (JsonProcessingException | DecodingException exception) {
            log.warn("Failed to decode OpenWeather response", exception);
            throw new CustomException(HttpStatus.BAD_GATEWAY, "과거 날씨 응답 형식이 올바르지 않습니다.");
        } catch (RuntimeException exception) {
            if (hasCause(exception, TimeoutException.class)) {
                throw new CustomException(HttpStatus.GATEWAY_TIMEOUT, "과거 날씨 서버 응답 시간이 초과되었습니다.");
            }
            throw exception;
        }
    }

    private boolean hasCause(Throwable throwable, Class<? extends Throwable> causeType) {
        Throwable current = throwable;
        while (current != null) {
            if (causeType.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}