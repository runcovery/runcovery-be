package com.likelion14.runcovery.wellness.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.likelion14.runcovery.body.BodyPart;
import com.likelion14.runcovery.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class YouTubeVideoSearchService {

    private static final String YOUTUBE_API_BASE_URL = "https://www.googleapis.com/youtube/v3";
    private static final int MAX_VIDEO_DURATION_SECONDS = 180;
    private static final int MAX_SEARCH_RESULTS = 20;
    private static final int MAX_DESCRIPTION_LENGTH = 1_000;

    private final WebClient.Builder webClientBuilder;

    @Value("${youtube.api.key:${YOUTUBE_API_KEY:}}")
    private String apiKey;

    @Value("${wellness.youtube.timeout:10s}")
    private Duration requestTimeout;

    public VideoResult findRecoveryVideo(List<BodyPart> painfulParts) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new CustomException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "YouTube 영상 검색을 위한 YOUTUBE_API_KEY가 설정되지 않았습니다."
            );
        }

        List<BodyPart> safePainfulParts = painfulParts == null ? List.of() : painfulParts;
        String query = buildSearchQuery(safePainfulParts);

        try {
            SearchResponse searchResponse = webClient()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("part", "snippet")
                            .queryParam("q", query)
                            .queryParam("type", "video")
                            .queryParam("videoDuration", "short")
                            .queryParam("videoEmbeddable", "true")
                            .queryParam("videoSyndicated", "true")
                            .queryParam("safeSearch", "strict")
                            .queryParam("relevanceLanguage", "ko")
                            .queryParam("regionCode", "KR")
                            .queryParam("order", "relevance")
                            .queryParam("maxResults", MAX_SEARCH_RESULTS)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(SearchResponse.class)
                    .timeout(requestTimeout)
                    .block();

            List<String> videoIds = extractVideoIds(searchResponse);
            if (videoIds.isEmpty()) {
                throw noVideoFound(safePainfulParts);
            }

            VideosResponse videosResponse = webClient()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/videos")
                            .queryParam("part", "snippet,contentDetails")
                            .queryParam("id", String.join(",", videoIds))
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(VideosResponse.class)
                    .timeout(requestTimeout)
                    .block();

            Map<String, VideoItem> detailsById = indexVideoDetails(videosResponse);
            List<List<String>> requiredKeywordGroups = buildRequiredKeywordGroups(safePainfulParts);

            return videoIds.stream()
                    .map(detailsById::get)
                    .filter(Objects::nonNull)
                    .map(this::toCandidate)
                    .filter(Objects::nonNull)
                    .filter(candidate -> candidate.durationSeconds() < MAX_VIDEO_DURATION_SECONDS)
                    .filter(candidate -> isRelevant(candidate, requiredKeywordGroups))
                    .findFirst()
                    .map(candidate -> new VideoResult(
                            decodeBasicHtmlEntities(candidate.title()),
                            "https://www.youtube.com/watch?v=" + candidate.videoId(),
                            abbreviate(candidate.description(), MAX_DESCRIPTION_LENGTH),
                            candidate.durationSeconds()
                    ))
                    .orElseThrow(() -> noVideoFound(safePainfulParts));
        } catch (CustomException exception) {
            throw exception;
        } catch (WebClientResponseException exception) {
            log.warn("YouTube Data API returned HTTP {}", exception.getStatusCode().value());
            throw new CustomException(
                    HttpStatus.BAD_GATEWAY,
                    "YouTube 영상 검색에 실패했습니다. (HTTP " + exception.getStatusCode().value() + ")"
            );
        } catch (WebClientRequestException exception) {
            log.warn("Failed to connect to YouTube Data API: {}", exception.getClass().getSimpleName());
            throw new CustomException(HttpStatus.BAD_GATEWAY, "YouTube 영상 검색 서버에 연결할 수 없습니다.");
        } catch (RuntimeException exception) {
            if (hasCause(exception, TimeoutException.class)) {
                throw new CustomException(HttpStatus.GATEWAY_TIMEOUT, "YouTube 영상 검색 응답 시간이 초과되었습니다.");
            }
            log.warn("Unexpected YouTube video search failure: {}", exception.getClass().getSimpleName());
            throw new CustomException(HttpStatus.BAD_GATEWAY, "YouTube 영상 검색 중 오류가 발생했습니다.");
        }
    }

    private WebClient webClient() {
        return webClientBuilder.clone()
                .baseUrl(YOUTUBE_API_BASE_URL)
                .build();
    }

    private List<String> extractVideoIds(SearchResponse response) {
        if (response == null || response.items() == null) {
            return List.of();
        }
        return response.items().stream()
                .filter(Objects::nonNull)
                .map(SearchItem::id)
                .filter(Objects::nonNull)
                .map(SearchId::videoId)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();
    }

    private Map<String, VideoItem> indexVideoDetails(VideosResponse response) {
        if (response == null || response.items() == null) {
            return Map.of();
        }
        return response.items().stream()
                .filter(item -> item != null && item.id() != null)
                .collect(Collectors.toMap(
                        VideoItem::id,
                        item -> item,
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));
    }

    private VideoCandidate toCandidate(VideoItem item) {
        if (item.snippet() == null || item.contentDetails() == null
                || item.contentDetails().duration() == null) {
            return null;
        }
        try {
            long seconds = Duration.parse(item.contentDetails().duration()).getSeconds();
            if (seconds <= 0 || seconds > Integer.MAX_VALUE) {
                return null;
            }
            return new VideoCandidate(
                    item.id(),
                    item.snippet().title(),
                    item.snippet().description(),
                    (int) seconds
            );
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    private boolean isRelevant(VideoCandidate candidate, List<List<String>> requiredKeywordGroups) {
        if (requiredKeywordGroups.isEmpty()) {
            return true;
        }
        String searchableText = ((candidate.title() == null ? "" : candidate.title())
                + " "
                + (candidate.description() == null ? "" : candidate.description()))
                .toLowerCase(Locale.ROOT);

        return requiredKeywordGroups.stream()
                .allMatch(group -> group.stream()
                        .map(keyword -> keyword.toLowerCase(Locale.ROOT))
                        .anyMatch(searchableText::contains));
    }

    private String buildSearchQuery(List<BodyPart> painfulParts) {
        if (painfulParts.isEmpty()) {
            return "러닝 후 전신 회복 스트레칭 3분";
        }

        Set<String> terms = new LinkedHashSet<>();
        for (BodyPart bodyPart : painfulParts) {
            if (bodyPart.getBodyName() != null && !bodyPart.getBodyName().isBlank()) {
                terms.add(bodyPart.getBodyName().trim());
            }
            terms.addAll(primarySearchTerms(bodyPart.getBodyPartCode()));
        }
        terms.add("스트레칭");
        terms.add("3분");
        return String.join(" ", terms);
    }

    private List<List<String>> buildRequiredKeywordGroups(List<BodyPart> painfulParts) {
        List<List<String>> groups = new ArrayList<>();
        for (BodyPart bodyPart : painfulParts) {
            LinkedHashSet<String> keywords = new LinkedHashSet<>();
            if (bodyPart.getBodyName() != null && !bodyPart.getBodyName().isBlank()) {
                keywords.add(bodyPart.getBodyName().trim());
            }
            keywords.addAll(relevanceTerms(bodyPart.getBodyPartCode()));
            groups.add(List.copyOf(keywords));
        }
        return groups;
    }

    private List<String> primarySearchTerms(String bodyPartCode) {
        String code = bodyPartCode == null ? "" : bodyPartCode.toUpperCase(Locale.ROOT);
        if (code.contains("KNEE") && code.startsWith("B_")) {
            return List.of("햄스트링");
        }
        if (code.contains("KNEE")) {
            return List.of("무릎");
        }
        if (code.contains("THIGH") && code.startsWith("B_")) {
            return List.of("햄스트링");
        }
        if (code.contains("THIGH")) {
            return List.of("대퇴사두근");
        }
        if (code.contains("CALF")) {
            return List.of("종아리");
        }
        if (code.contains("SHIN")) {
            return List.of("정강이");
        }
        if (code.contains("GLUTES")) {
            return List.of("둔근");
        }
        if (code.contains("LOWER_BACK")) {
            return List.of("허리");
        }
        if (code.contains("SHOULDER")) {
            return List.of("어깨");
        }
        return List.of();
    }

    private List<String> relevanceTerms(String bodyPartCode) {
        String code = bodyPartCode == null ? "" : bodyPartCode.toUpperCase(Locale.ROOT);
        if (code.contains("KNEE") && code.startsWith("B_")) {
            return List.of("오금", "햄스트링", "무릎 뒤", "hamstring");
        }
        if (code.contains("KNEE")) {
            return List.of("무릎", "knee");
        }
        if (code.contains("THIGH") && code.startsWith("B_")) {
            return List.of("허벅지 뒤", "햄스트링", "hamstring");
        }
        if (code.contains("THIGH")) {
            return List.of("허벅지", "대퇴사두", "quadriceps", "quad");
        }
        if (code.contains("CALF")) {
            return List.of("종아리", "calf");
        }
        if (code.contains("SHIN")) {
            return List.of("정강이", "shin");
        }
        if (code.contains("GLUTES")) {
            return List.of("엉덩이", "둔근", "glute");
        }
        if (code.contains("LOWER_BACK")) {
            return List.of("허리", "요추", "lower back");
        }
        if (code.contains("SHOULDER")) {
            return List.of("어깨", "shoulder");
        }
        if (code.contains("NECK")) {
            return List.of("목", "neck");
        }
        if (code.contains("CHEST")) {
            return List.of("가슴", "chest");
        }
        if (code.contains("FOOT")) {
            return List.of("발", "foot");
        }
        if (code.contains("ARM")) {
            return List.of("팔", "arm");
        }
        if (code.contains("PELVIS")) {
            return List.of("골반", "pelvis", "hip");
        }
        if (code.contains("BACK")) {
            return List.of("등", "back");
        }
        return List.of();
    }

    private CustomException noVideoFound(List<BodyPart> painfulParts) {
        String target = painfulParts.isEmpty()
                ? "전신"
                : painfulParts.stream()
                        .map(BodyPart::getBodyName)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.joining(", "));
        return new CustomException(
                HttpStatus.BAD_GATEWAY,
                target + " 부위를 모두 포함하는 3분 미만의 검증된 스트레칭 영상을 찾지 못했습니다."
        );
    }

    private String decodeBasicHtmlEntities(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">");
    }

    private String abbreviate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        return normalized.length() <= maxLength ? normalized : normalized.substring(0, maxLength);
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

    public record VideoResult(
            String title,
            String videoUrl,
            String description,
            int durationSeconds
    ) {
    }

    private record VideoCandidate(
            String videoId,
            String title,
            String description,
            int durationSeconds
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SearchResponse(List<SearchItem> items) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SearchItem(SearchId id) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SearchId(String videoId) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record VideosResponse(List<VideoItem> items) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record VideoItem(String id, VideoSnippet snippet, ContentDetails contentDetails) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record VideoSnippet(String title, String description) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ContentDetails(String duration) {
    }
}