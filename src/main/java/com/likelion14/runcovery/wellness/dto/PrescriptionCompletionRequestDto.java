package com.likelion14.runcovery.wellness.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "처방전 완료 상태 변경 요청")
public class PrescriptionCompletionRequestDto {

    @NotNull(message = "isCompleted 값은 필수입니다.")
    @Schema(description = "true는 완료, false는 완료 취소", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isCompleted;
}