package com.likelion14.runcovery.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class BodyIssueSaveRequestDto {

    @NotEmpty(message = "통증 부위 목록은 필수입니다")
    @Valid
    private List<PainAreaDto> painAreas;
}
