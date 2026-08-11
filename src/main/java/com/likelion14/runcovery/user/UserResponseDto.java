package com.likelion14.runcovery.user;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class UserResponseDto {

    private Long userId;
    private String nickname;
    private Integer age;
    private String gender;
    private BigDecimal height;
    private BigDecimal weight;
    private String runningExperience;
    private Integer maxRunDuration;
    private BigDecimal avgSleepHours;

    public UserResponseDto(User user) {
        this.userId = user.getId();
        this.nickname = user.getNickname();
        this.age = user.getAge();
        this.gender = user.getGender();
        this.height = user.getHeight();
        this.weight = user.getWeight();
        this.runningExperience = user.getRunningExperience();
        this.maxRunDuration = user.getMaxRunDuration();
        this.avgSleepHours = user.getAvgSleepHours();
    }
}
