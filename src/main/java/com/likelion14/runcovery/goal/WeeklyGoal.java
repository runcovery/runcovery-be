package com.likelion14.runcovery.goal;

import com.likelion14.runcovery.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class WeeklyGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "week_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "future_id", nullable = false)
    private FutureGoal futureGoal;

    @Column(nullable = false)
    private Integer weekNo;

    @Column(nullable = false)
    private String weeklyGoal;

    @Column(nullable = false)
    private Integer weeklyGoalDistance;

    @Column(nullable = false)
    private Integer expectedCalories;

    public WeeklyGoal(User user, FutureGoal futureGoal, Integer weekNo, String weeklyGoal,
                       Integer weeklyGoalDistance, Integer expectedCalories) {
        this.user = user;
        this.futureGoal = futureGoal;
        this.weekNo = weekNo;
        this.weeklyGoal = weeklyGoal;
        this.weeklyGoalDistance = weeklyGoalDistance;
        this.expectedCalories = expectedCalories;
    }

    public void update(Integer weekNo, String weeklyGoal, Integer weeklyGoalDistance, Integer expectedCalories) {
        this.weekNo = weekNo;
        this.weeklyGoal = weeklyGoal;
        this.weeklyGoalDistance = weeklyGoalDistance;
        this.expectedCalories = expectedCalories;
    }
}
