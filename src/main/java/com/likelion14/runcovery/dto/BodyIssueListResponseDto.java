package com.likelion14.runcovery.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class BodyIssueListResponseDto {

    private List<PainAreaDto> painAreas;

    public BodyIssueListResponseDto(List<PainAreaDto> painAreas) {
        this.painAreas = painAreas;
    }
}
