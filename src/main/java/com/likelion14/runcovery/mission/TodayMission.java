package com.likelion14.runcovery.mission;

import com.likelion14.runcovery.condition.TodayCondition;
import com.likelion14.runcovery.goal.WeeklyGoal;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TodayMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mission_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "condition_id", nullable = false)
    private TodayCondition todayCondition;

    @ManyToOne
    @JoinColumn(name = "week_id", nullable = false)
    private WeeklyGoal weeklyGoal;

    @Column(nullable = false)
    private LocalDate missionDate;

    @Column(nullable = false, length = 50)
    private String recommendedIntensity;

    @Column(nullable = false, length = 50)
    private String recommendedTime;

    @Column(nullable = false, length = 50)
    private String recommendedZone;

    @Column(nullable = false, length = 500)
    private String recommendedZoneDesc;

    @Column(nullable = false, length = 500)
    private String detailComment;

    private Boolean isCompleted = false;

    private Boolean isRest = false;

    public TodayMission(TodayCondition todayCondition, WeeklyGoal weeklyGoal, LocalDate missionDate,
                         String recommendedIntensity, String recommendedTime, String recommendedZone,
                         String recommendedZoneDesc, String detailComment) {
        this.todayCondition = todayCondition;
        this.weeklyGoal = weeklyGoal;
        this.missionDate = missionDate;
        this.recommendedIntensity = recommendedIntensity;
        this.recommendedTime = recommendedTime;
        this.recommendedZone = recommendedZone;
        this.recommendedZoneDesc = recommendedZoneDesc;
        this.detailComment = detailComment;
    }

    public void update(LocalDate missionDate, String recommendedIntensity, String recommendedTime,
                        String recommendedZone, String recommendedZoneDesc, String detailComment) {
        this.missionDate = missionDate;
        this.recommendedIntensity = recommendedIntensity;
        this.recommendedTime = recommendedTime;
        this.recommendedZone = recommendedZone;
        this.recommendedZoneDesc = recommendedZoneDesc;
        this.detailComment = detailComment;
    }

    public void complete() {
        this.isCompleted = true;
    }
}
