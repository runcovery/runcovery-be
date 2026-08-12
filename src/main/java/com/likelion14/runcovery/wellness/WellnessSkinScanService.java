package com.likelion14.runcovery.wellness;

import com.likelion14.runcovery.common.exception.CustomException;
import com.likelion14.runcovery.user.User;
import com.likelion14.runcovery.user.UserRepository;
import com.likelion14.runcovery.wellness.SkinScanResponseDto.ConditionScores;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.time.LocalDate;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class WellnessSkinScanService {

    private final UserRepository userRepository;
    private final SkinRecordRepository skinRecordRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${wellness.skin-scan.url:http://localhost:8000/scan}")
    private String skinScanUrl;

    @Value("${wellness.skin-scan.timeout:30s}")
    private Duration skinScanTimeout;

    @Value("${wellness.skin-scan.max-response-size:20971520}")
    private int maxResponseSize;

    public SkinRecord scanAndSave(Long memberId, SkinRecordType type, MultipartFile image) {
        validateRequest(memberId, type, image);

        User user = userRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        ConditionScores scores = requestConditionScores(image);
        validateScores(scores);

        SkinRecord record = new SkinRecord(
                user,
                type,
                LocalDate.now(),
                scores.redness(),
                scores.oiliness(),
                scores.texture(),
                scores.pores(),
                scores.blemishes(),
                scores.hydration(),
                scores.pigment()
        );
        record.setTotalScore(calculateTotalScore(scores));
        // MultipartFile exposes the client-supplied file name, not the client local absolute path.
        record.setSkinImage(image.getOriginalFilename());

        return skinRecordRepository.save(record);
    }

    private ConditionScores requestConditionScores(MultipartFile image) {
        MultipartBodyBuilder body = new MultipartBodyBuilder();
        body.part("image", image.getResource())
                .filename(image.getOriginalFilename() == null ? "image" : image.getOriginalFilename())
                .contentType(MediaType.parseMediaType(image.getContentType()));

        try {
            SkinScanResponseDto response = webClientBuilder.clone()
                    .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(maxResponseSize))
                    .build()
                    .post()
                    .uri(skinScanUrl)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(body.build()))
                    .retrieve()
                    .bodyToMono(SkinScanResponseDto.class)
                    .timeout(skinScanTimeout)
                    .block();

            if (response == null || response.conditionScores() == null) {
                throw new CustomException(
                        HttpStatus.BAD_GATEWAY,
                        "피부 분석 서버 응답에 condition_scores가 없습니다."
                );
            }
            return response.conditionScores();
        } catch (CustomException exception) {
            throw exception;
        } catch (WebClientResponseException exception) {
            log.warn(
                    "Skin scan server returned HTTP {}: {}",
                    exception.getStatusCode().value(),
                    exception.getResponseBodyAsString()
            );
            throw new CustomException(
                    HttpStatus.BAD_GATEWAY,
                    "피부 분석 서버가 오류를 반환했습니다. (HTTP "
                            + exception.getStatusCode().value() + ")"
            );
        } catch (WebClientRequestException exception) {
            log.warn("Failed to connect to skin scan server", exception);
            throw new CustomException(HttpStatus.BAD_GATEWAY, "피부 분석 서버에 연결할 수 없습니다.");
        } catch (DataBufferLimitException exception) {
            throw new CustomException(HttpStatus.BAD_GATEWAY, "피부 분석 서버 응답이 너무 큽니다.");
        } catch (DecodingException exception) {
            log.warn("Failed to decode skin scan response", exception);
            throw new CustomException(HttpStatus.BAD_GATEWAY, "피부 분석 서버 응답 형식이 올바르지 않습니다.");
        } catch (RuntimeException exception) {
            if (hasCause(exception, TimeoutException.class)) {
                throw new CustomException(HttpStatus.GATEWAY_TIMEOUT, "피부 분석 서버 응답 시간이 초과되었습니다.");
            }
            if (hasCause(exception, DataBufferLimitException.class)) {
                throw new CustomException(HttpStatus.BAD_GATEWAY, "피부 분석 서버 응답이 너무 큽니다.");
            }
            if (hasCause(exception, DecodingException.class)) {
                throw new CustomException(HttpStatus.BAD_GATEWAY, "피부 분석 서버 응답 형식이 올바르지 않습니다.");
            }
            throw exception;
        }
    }

    private void validateRequest(Long memberId, SkinRecordType type, MultipartFile image) {
        if (memberId == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "memberId는 필수입니다.");
        }
        if (type == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "피부 측정 유형은 필수입니다.");
        }
        if (image == null || image.isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미지 파일은 필수입니다.");
        }
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "이미지 형식의 파일만 업로드할 수 있습니다.");
        }
    }

    private void validateScores(ConditionScores scores) {
        validateScore("redness", scores.redness());
        validateScore("oiliness", scores.oiliness());
        validateScore("texture", scores.texture());
        validateScore("pores", scores.pores());
        validateScore("blemishes", scores.blemishes());
        validateScore("hydration", scores.hydration());
        validateScore("pigment", scores.pigment());
    }

    private void validateScore(String name, Integer score) {
        if (score == null || score < 0 || score > 100) {
            throw new CustomException(
                    HttpStatus.BAD_GATEWAY,
                    "피부 분석 서버가 유효하지 않은 " + name + " 점수를 반환했습니다."
            );
        }
    }

    private int calculateTotalScore(ConditionScores scores) {
        int sum = scores.redness() + scores.oiliness() + scores.texture()
                + scores.pores() + scores.blemishes() + scores.hydration() + scores.pigment();
        return Math.round(sum / 7.0f);
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
