package com.likelion14.runcovery.home;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HomeResponseDto {
    private String nickname;
    private String scene;
    private Integer achievementRate;
    private Integer temp;
    private Integer daysRemaining;
    private String wellnessTip;

}
