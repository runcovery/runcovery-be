package com.likelion14.runcovery.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class FutureGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "future_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String scene;

    @Column(nullable = false)
    private Integer targetDistance;

    @Column(nullable = false)
    private Integer targetPeriod;

    @Column(nullable = false)
    private Integer weeklyFrequency;

    @Column(nullable = false)
    private Integer availableTime;

    @Column(precision = 5, scale = 2)
    private BigDecimal achievementRate = BigDecimal.ZERO;

    public FutureGoal(User user, String scene, Integer targetDistance, Integer targetPeriod,
                       Integer weeklyFrequency, Integer availableTime) {
        this.user = user;
        this.scene = scene;
        this.targetDistance = targetDistance;
        this.targetPeriod = targetPeriod;
        this.weeklyFrequency = weeklyFrequency;
        this.availableTime = availableTime;
    }

    public void update(String scene, Integer targetDistance, Integer targetPeriod,
                        Integer weeklyFrequency, Integer availableTime) {
        this.scene = scene;
        this.targetDistance = targetDistance;
        this.targetPeriod = targetPeriod;
        this.weeklyFrequency = weeklyFrequency;
        this.availableTime = availableTime;
    }
}
