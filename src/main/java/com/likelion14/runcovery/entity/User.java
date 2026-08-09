package com.likelion14.runcovery.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false, length = 10)
    private String gender;

    @Column(nullable = false, precision = 5, scale = 1)
    private BigDecimal height;

    @Column(nullable = false, precision = 5, scale = 1)
    private BigDecimal weight;

    @Column(nullable = false)
    private String runningExperience;

    @Column(nullable = false)
    private Integer maxRunDuration;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal avgSleepHours;

    public User(String nickname, Integer age, String gender, BigDecimal height, BigDecimal weight,
                String runningExperience, Integer maxRunDuration, BigDecimal avgSleepHours) {
        this.nickname = nickname;
        this.age = age;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
        this.runningExperience = runningExperience;
        this.maxRunDuration = maxRunDuration;
        this.avgSleepHours = avgSleepHours;
    }

    public void update(String nickname, Integer age, String gender, BigDecimal height, BigDecimal weight,
                        String runningExperience, Integer maxRunDuration, BigDecimal avgSleepHours) {
        this.nickname = nickname;
        this.age = age;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
        this.runningExperience = runningExperience;
        this.maxRunDuration = maxRunDuration;
        this.avgSleepHours = avgSleepHours;
    }
}
