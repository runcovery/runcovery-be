package com.likelion14.runcovery.goal;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SelectedSceneRequestDto {

    @NotBlank(message = "sceneId는 필수입니다")
    private String sceneId;

    @NotBlank(message = "장면(scene)은 필수입니다")
    private String scene;

    @NotBlank(message = "추천 이유(reason)는 필수입니다")
    private String reason;
}
