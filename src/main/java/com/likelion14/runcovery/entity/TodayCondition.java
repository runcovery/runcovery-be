package com.likelion14.runcovery.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TodayCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "condition_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate conditionDate;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal sleepHours;

    @Column(nullable = false)
    private String bodyCondition;

    @Column(nullable = false)
    private Integer activeCalories;

    private Boolean isChecked = false;

    public TodayCondition(User user, LocalDate conditionDate, BigDecimal sleepHours,
                           String bodyCondition, Integer activeCalories) {
        this.user = user;
        this.conditionDate = conditionDate;
        this.sleepHours = sleepHours;
        this.bodyCondition = bodyCondition;
        this.activeCalories = activeCalories;
    }

    public void update(LocalDate conditionDate, BigDecimal sleepHours, String bodyCondition, Integer activeCalories) {
        this.conditionDate = conditionDate;
        this.sleepHours = sleepHours;
        this.bodyCondition = bodyCondition;
        this.activeCalories = activeCalories;
    }
}
