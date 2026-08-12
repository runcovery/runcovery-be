package com.likelion14.runcovery.wellness;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SkinScanResponseDto(
        @JsonProperty("condition_scores") ConditionScores conditionScores
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ConditionScores(
            Integer redness,
            Integer oiliness,
            Integer texture,
            Integer pores,
            Integer blemishes,
            Integer hydration,
            Integer pigment
    ) {}
}
