package com.likelion14.runcovery.mission;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MissionAiResult {
    private String recommendedIntensity;
    private String recommendedTime;
    private String recommendedZone;
    private String recommendedZoneDesc;
    private String detailComment;
    private Boolean isRest;
}
