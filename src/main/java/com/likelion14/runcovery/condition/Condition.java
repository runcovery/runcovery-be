package com.likelion14.runcovery.condition;

import com.likelion14.runcovery.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "conditions")
public class Condition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "condition_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate conditionDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SleepQuality sleepQuality;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BodyCondition bodyCondition;

    // 기존 활동기록과 미션 수행여부로 변경
//    @Column(nullable = false)
//    private Integer activeCalories;

    @Column
    private String conditionTitle;

    @Column(length = 1000)
    private String conditionFeedback;

    private Boolean isChecked = false;

    public Condition(User user, LocalDate conditionDate, SleepQuality sleepQuality,
                     BodyCondition bodyCondition) {
        this.user = user;
        this.conditionDate = conditionDate;
        this.sleepQuality = sleepQuality;
        this.bodyCondition = bodyCondition;
        this.isChecked = true;
    }

    public void update(SleepQuality sleepQuality, BodyCondition bodyCondition) {
        this.sleepQuality = sleepQuality;
        this.bodyCondition = bodyCondition;
        this.isChecked = true;
    }

    public void updateAnalysis(String conditionTitle, String conditionFeedback) {
        this.conditionTitle = conditionTitle;
        this.conditionFeedback = conditionFeedback;
        this.isChecked = true;
    }
}
