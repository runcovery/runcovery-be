package com.likelion14.runcovery.body;

import lombok.Getter;

@Getter
public class BodyIssueSaveResponseDto {

    private boolean updated;
    private int updatedCount;

    public BodyIssueSaveResponseDto(boolean updated, int updatedCount) {
        this.updated = updated;
        this.updatedCount = updatedCount;
    }
}
