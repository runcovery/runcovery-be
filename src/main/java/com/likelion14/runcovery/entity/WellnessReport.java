package com.likelion14.runcovery.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class WellnessReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "record_id", nullable = false)
    private ActivityRecord activityRecord;

    @Column(nullable = false)
    private LocalDate reportDate;

    @Column(nullable = false)
    private String warningTitle;

    @Column(nullable = false)
    private Integer runningIntensity;

    public WellnessReport(ActivityRecord activityRecord, LocalDate reportDate, String warningTitle,
                           Integer runningIntensity) {
        this.activityRecord = activityRecord;
        this.reportDate = reportDate;
        this.warningTitle = warningTitle;
        this.runningIntensity = runningIntensity;
    }

    public void update(LocalDate reportDate, String warningTitle, Integer runningIntensity) {
        this.reportDate = reportDate;
        this.warningTitle = warningTitle;
        this.runningIntensity = runningIntensity;
    }
}
