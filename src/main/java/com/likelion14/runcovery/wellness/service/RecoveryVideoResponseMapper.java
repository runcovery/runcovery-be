package com.likelion14.runcovery.wellness.service;

import com.likelion14.runcovery.wellness.dto.ReportResponseDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class RecoveryVideoResponseMapper {

    private static final List<String> LOWER_BODY_NAMES = List.of(
            "무릎", "오금", "허벅지", "종아리", "정강이", "발",
            "골반", "서혜부", "엉덩이", "둔근", "발목"
    );

    public ReportResponseDto.RecoveryVideo toResponse(YouTubeVideoSearchService.VideoResult video) {
        BodyGroup bodyGroup = resolveBodyGroup(video.targetParts());
        return ReportResponseDto.RecoveryVideo.builder()
                .title(buildTitle(video.targetParts()))
                .videoUrl(video.videoUrl())
                .sourceTitle(video.title())
                .durationSeconds(video.durationSeconds())
                .bodyGroup(bodyGroup.name())
                .recommendationReason(buildRecommendationReason(
                        bodyGroup,
                        video.targetParts(),
                        video.coveredPainPartCodes(),
                        video.uncoveredPainPartCodes()
                ))
                .targetParts(video.targetParts())
                .coveredPainPartCodes(video.coveredPainPartCodes())
                .uncoveredPainPartCodes(video.uncoveredPainPartCodes())
                .build();
    }

    private BodyGroup resolveBodyGroup(List<String> targetParts) {
        if (targetParts == null || targetParts.isEmpty()) {
            return BodyGroup.FULL_BODY;
        }
        return targetParts.stream().anyMatch(this::isLowerBodyName)
                ? BodyGroup.LOWER_BODY
                : BodyGroup.UPPER_BODY;
    }

    private boolean isLowerBodyName(String bodyName) {
        return bodyName != null && LOWER_BODY_NAMES.stream().anyMatch(bodyName::contains);
    }

    private String buildRecommendationReason(
            BodyGroup bodyGroup,
            List<String> targetParts,
            List<String> coveredPainPartCodes,
            List<String> uncoveredPainPartCodes
    ) {
        if (bodyGroup == BodyGroup.FULL_BODY) {
            return "특정 통증 부위가 선택되지 않아 러닝 후 전신 이완과 회복을 돕는 영상을 추천합니다.";
        }

        String parts = displayBodyNames(targetParts);
        String target = parts.isBlank() ? "선택 부위" : parts;
        if (coveredPainPartCodes == null || coveredPainPartCodes.isEmpty()) {
            return target + "가 영상 제목에서 직접 확인되지는 않아 "
                    + bodyGroup.label + " 전반의 긴장 완화를 돕는 일반 회복 영상을 추천합니다.";
        }
        if (uncoveredPainPartCodes != null && !uncoveredPainPartCodes.isEmpty()) {
            return target + " 중 일부가 영상 제목에서 확인되어 "
                    + bodyGroup.label + " 회복에 참고할 수 있는 영상을 추천합니다.";
        }
        return target + "가 영상 제목에서 확인되어 해당 부위의 긴장 완화와 운동 후 회복을 돕는 영상을 추천합니다.";
    }

    private String buildTitle(List<String> targetParts) {
        String target = displayBodyNames(targetParts);
        return (target.isBlank() ? "전신" : target) + " 회복 스트레칭 영상";
    }

    private String displayBodyNames(List<String> targetParts) {
        if (targetParts == null || targetParts.isEmpty()) {
            return "";
        }
        return targetParts.stream()
                .filter(Objects::nonNull)
                .map(this::displayBodyName)
                .filter(name -> !name.isBlank())
                .distinct()
                .collect(Collectors.joining(", "));
    }

    private String displayBodyName(String targetPart) {
        String bodyName = targetPart
                .replaceAll("^(?:(?:LEFT|RIGHT|FRONT|BACK)\\s*)+", "")
                .trim();
        int openingParenthesis = bodyName.indexOf('(');
        int closingParenthesis = bodyName.indexOf(')', openingParenthesis + 1);
        if (openingParenthesis >= 0 && closingParenthesis > openingParenthesis + 1) {
            return bodyName.substring(openingParenthesis + 1, closingParenthesis).trim();
        }
        return bodyName;
    }

    private enum BodyGroup {
        UPPER_BODY("상체"),
        LOWER_BODY("하체"),
        FULL_BODY("전신");

        private final String label;

        BodyGroup(String label) {
            this.label = label;
        }
    }
}