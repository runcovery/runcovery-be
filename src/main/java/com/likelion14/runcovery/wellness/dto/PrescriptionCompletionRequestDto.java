package com.likelion14.runcovery.wellness.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PrescriptionCompletionRequestDto {

    @NotNull(message = "isCompleted 값은 필수입니다.")
    private Boolean isCompleted;
}