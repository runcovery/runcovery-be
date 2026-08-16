package com.likelion14.runcovery.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class UserCreateRequestDto {

    @NotNull(message = "userId는 필수입니다")
    private UUID userId;

    @NotBlank(message = "닉네임은 필수입니다")
    private String nickname;

    @NotNull(message = "나이는 필수입니다")
    private Integer age;

    @NotBlank(message = "성별은 필수입니다")
    private String gender;

    @NotNull(message = "키는 필수입니다")
    private BigDecimal height;

    @NotNull(message = "몸무게는 필수입니다")
    private BigDecimal weight;

    @NotBlank(message = "러닝 경험은 필수입니다")
    private String runningExperience;
}
