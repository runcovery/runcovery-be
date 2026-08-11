package com.likelion14.runcovery.activity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivitySyncResponseDto {

    private long recordId;
    private MissionInfo mission;


    public static class MissionInfo {
        private Long missionId;
        private Boolean isCompleted;
    }

}
