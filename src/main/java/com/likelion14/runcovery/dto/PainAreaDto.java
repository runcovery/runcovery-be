package com.likelion14.runcovery.dto;

import com.likelion14.runcovery.entity.BodyIssue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PainAreaDto {

    @NotBlank(message = "신체 부위 코드는 필수입니다")
    private String bodyPartCode;

    @NotNull(message = "통증 여부는 필수입니다")
    private Boolean isPainful;

    public PainAreaDto(BodyIssue bodyIssue) {
        this.bodyPartCode = bodyIssue.getBodyPart().getBodyPartCode();
        this.isPainful = bodyIssue.getIsPainful();
    }
}
