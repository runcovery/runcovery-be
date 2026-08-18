package com.likelion14.runcovery.home;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


public record HomeResponseDto(
        Long userId,
        String nickname,
        String scene,
        Integer achievementRate,
        Integer temp,
        Integer daysRemaining,
        String wellnessTip
) {}
