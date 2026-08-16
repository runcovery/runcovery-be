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
    private static final int MAX_RECOVERY_VIDEOS = 2;

    private final WebClient.Builder webClientBuilder;

    @Value("${youtube.api.key:${YOUTUBE_API_KEY:}}")
    private String apiKey;

    @Value("${wellness.youtube.timeout:10s}")
    private Duration requestTimeout;

    /**
     * 상체·하체라는 큰 신체 그룹 단위로 최대 두 개의 회복 영상을 찾습니다.
     * 같은 그룹의 통증 부위가 여러 개여도 영상은 하나만 반환하며,
     * 상체와 하체가 함께 선택된 경우에만 영상이 두 개가 됩니다.
     */
    public List<VideoResult> findRecoveryVideos(List<BodyPart> painfulParts) {
        List<BodyPart> safePainfulParts = painfulParts == null ? List.of() : painfulParts;
        if (safePainfulParts.isEmpty()) {
            return List.of(findRecoveryVideo(List.of()));
        }

        return buildRecoveryGroups(safePainfulParts).stream()
                .limit(MAX_RECOVERY_VIDEOS)
                .map(group -> findGroupRecoveryVideo(group.group(), group.bodyParts()))
                .toList();
    }

    private VideoResult findGroupRecoveryVideo(RecoveryGroup group, List<BodyPart> bodyParts) {
        try {
            // 선택 부위를 모두 제목에서 확인할 수 있는 통합 영상을 우선 사용합니다.
            return findRecoveryVideo(bodyParts);
        } catch (CustomException exception) {
            if (!isNoVideoFound(exception)) {
                throw exception;
            }
            log.info("No exact {} recovery video found. Falling back to a group recovery video.", group);
        }

        // 같은 그룹의 부위가 많아도 영상 수를 늘리지 않고, 상체 또는 하체 공통 회복 영상을 하나만 사용합니다.
        BodyPart groupPart = new BodyPart(
                group == RecoveryGroup.LOWER_BODY ? "LOWER_BODY" : "UPPER_BODY",
                group == RecoveryGroup.LOWER_BODY ? "하체" : "상체",
                null,
                null
        );
        VideoResult groupVideo = findRecoveryVideo(List.of(groupPart));
        return applyCoverage(groupVideo, bodyParts);
    }

    private VideoResult applyCoverage(VideoResult video, List<BodyPart> bodyParts) {
        List<String> coveredCodes = bodyParts.stream()
                .filter(bodyPart -> isMentionedInMetadata(video, bodyPart))
                .map(BodyPart::getBodyPartCode)
                .distinct()
                .toList();
        List<String> uncoveredCodes = bodyPartCodes(bodyParts).stream()
                .filter(code -> !coveredCodes.contains(code))
                .toList();

        return new VideoResult(
                video.title(),
                video.videoUrl(),
                video.description(),
                video.durationSeconds(),
                bodyPartNames(bodyParts),
                coveredCodes,
                uncoveredCodes
        );
    }

    private boolean isMentionedInMetadata(VideoResult video, BodyPart bodyPart) {
        // 부위 포함 여부도 제목에서 확인된 경우에만 true로 표시합니다.
        String searchableText = (video.title() == null ? "" : video.title())
                .toLowerCase(Locale.ROOT);
        List<String> keywords = new ArrayList<>();
        if (bodyPart.getBodyName() != null && !bodyPart.getBodyName().isBlank()) {
            keywords.add(bodyPart.getBodyName());
        }
        keywords.addAll(relevanceTerms(bodyPart.getBodyPartCode()));
        return keywords.stream()
                .map(keyword -> keyword.toLowerCase(Locale.ROOT))
                .anyMatch(searchableText::contains);
    }

    private boolean isNoVideoFound(CustomException exception) {
        return exception.getMessage() != null
                && exception.getMessage().contains("검증된 스트레칭 영상을 찾지 못했습니다.");
    }
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
                            candidate.durationSeconds(),
                            bodyPartNames(safePainfulParts),
                            bodyPartCodes(safePainfulParts),
                            List.of()
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
        // 제목에 선택 부위가 드러난 영상만 통합 영상으로 인정합니다.
        // 설명란의 단순 키워드 나열만으로 다른 부위까지 다룬다고 판단하지 않습니다.
        String searchableText = (candidate.title() == null ? "" : candidate.title())
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
        if (code.equals("LOWER_BODY")) {
            return List.of("하체");
        }
        if (code.equals("UPPER_BODY")) {
            return List.of("상체");
        }
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
        if (code.equals("LOWER_BODY")) {
            return List.of("하체", "lower body");
        }
        if (code.equals("UPPER_BODY")) {
            return List.of("상체", "upper body");
        }
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
    private List<RecoveryVideoGroup> buildRecoveryGroups(List<BodyPart> painfulParts) {
        Map<RecoveryGroup, List<BodyPart>> grouped = new LinkedHashMap<>();
        for (BodyPart bodyPart : painfulParts) {
            grouped.computeIfAbsent(classifyGroup(bodyPart), ignored -> new ArrayList<>()).add(bodyPart);
        }

        return grouped.entrySet().stream()
                .sorted((left, right) -> Integer.compare(left.getKey().priority(), right.getKey().priority()))
                .map(entry -> new RecoveryVideoGroup(entry.getKey(), List.copyOf(entry.getValue())))
                .toList();
    }

    private RecoveryGroup classifyGroup(BodyPart bodyPart) {
        return isLowerBody(bodyPart) ? RecoveryGroup.LOWER_BODY : RecoveryGroup.UPPER_BODY;
    }

    private boolean isLowerBody(BodyPart bodyPart) {
        String bodyName = bodyPart == null || bodyPart.getBodyName() == null
                ? ""
                : bodyPart.getBodyName().replace(" ", "");
        if (List.of("무릎", "오금", "허벅지", "종아리", "정강이", "발목", "발", "골반", "서혜부", "엉덩이", "둔근")
                .stream()
                .anyMatch(bodyName::contains)) {
            return true;
        }

        // 마스터 데이터의 부위명이 비어 있거나 새 코드가 추가된 경우를 위한 보조 판별입니다.
        String code = normalizedCode(bodyPart);
        return code.contains("KNEE") || code.contains("THIGH") || code.contains("CALF")
                || code.contains("SHIN") || code.contains("FOOT") || code.contains("ANKLE")
                || code.contains("GLUTE") || code.contains("PELVIS") || code.contains("HIP");
    }

    private String normalizedCode(BodyPart bodyPart) {
        return bodyPart == null || bodyPart.getBodyPartCode() == null
                ? ""
                : bodyPart.getBodyPartCode().toUpperCase(Locale.ROOT);
    }

    private List<String> bodyPartNames(List<BodyPart> bodyParts) {
        return bodyParts.stream()
                .filter(Objects::nonNull)
                .map(bodyPart -> {
                    List<String> details = new ArrayList<>();
                    if (bodyPart.getSide() != null && !bodyPart.getSide().isBlank()) {
                        details.add(bodyPart.getSide().trim());
                    }
                    if (bodyPart.getDirection() != null && !bodyPart.getDirection().isBlank()) {
                        details.add(bodyPart.getDirection().trim());
                    }
                    if (bodyPart.getBodyName() != null && !bodyPart.getBodyName().isBlank()) {
                        details.add(bodyPart.getBodyName().trim());
                    }
                    return String.join(" ", details);
                })
                .filter(name -> !name.isBlank())
                .distinct()
                .toList();
    }

    private List<String> bodyPartCodes(List<BodyPart> bodyParts) {
        return bodyParts.stream()
                .filter(Objects::nonNull)
                .map(BodyPart::getBodyPartCode)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(code -> !code.isBlank())
                .distinct()
                .toList();
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
            int durationSeconds,
            List<String> targetParts,
            List<String> coveredPainPartCodes,
            List<String> uncoveredPainPartCodes
    ) {
    }

    private enum RecoveryGroup {
        LOWER_BODY(0),
        UPPER_BODY(1);

        private final int priority;

        RecoveryGroup(int priority) {
            this.priority = priority;
        }

        int priority() {
            return priority;
        }
    }

    private record RecoveryVideoGroup(
            RecoveryGroup group,
            List<BodyPart> bodyParts
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