package com.likelion14.runcovery.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class WeeklySchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "training_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "week_id", nullable = false)
    private WeeklyGoal weeklyGoal;

    @Column(nullable = false, length = 500)
    private String trainingContent;

    public WeeklySchedule(WeeklyGoal weeklyGoal, String trainingContent) {
        this.weeklyGoal = weeklyGoal;
        this.trainingContent = trainingContent;
    }

    public void update(String trainingContent) {
        this.trainingContent = trainingContent;
    }
}
