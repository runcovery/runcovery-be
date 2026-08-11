package com.likelion14.runcovery.activity;

import com.likelion14.runcovery.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ActivityRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Column(nullable = false)
    private Integer runningDuration;

    @Column(nullable = false)
    private LocalDate recordDate;

    @Column(name = "distance_m", nullable = false)
    private Integer distanceM;

    @Column(nullable = false)
    private Integer avgPace;

    @Column(nullable = false)
    private Integer avgHeartRate;

    @Column(nullable = false)
    private Integer maxHeartRate;

    @Column(nullable = false)
    private Integer calories;

    @Column(nullable = false)
    private Integer cadence;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column
    private Double lat;

    @Column
    private Double lon;

    public ActivityRecord(User user, Integer runningDuration, LocalDate recordDate, Integer distanceM,
                           Integer avgPace, Integer avgHeartRate, Integer maxHeartRate, Integer calories,
                           Integer cadence, LocalDateTime startTime, LocalDateTime endTime, Double lat, Double lon) {
        this.user = user;
        this.runningDuration = runningDuration;
        this.recordDate = recordDate;
        this.distanceM = distanceM;
        this.avgPace = avgPace;
        this.avgHeartRate = avgHeartRate;
        this.maxHeartRate = maxHeartRate;
        this.calories = calories;
        this.cadence = cadence;
        this.startTime = startTime;
        this.endTime = endTime;
        this.lat = lat;
        this.lon = lon;
    }

    public void update(Integer runningDuration, LocalDate recordDate, Integer distanceM, Integer avgPace,
                        Integer avgHeartRate, Integer maxHeartRate, Integer calories, Integer cadence,
                        LocalDateTime startTime, LocalDateTime endTime, Double lat, Double lon) {
        this.runningDuration = runningDuration;
        this.recordDate = recordDate;
        this.distanceM = distanceM;
        this.avgPace = avgPace;
        this.avgHeartRate = avgHeartRate;
        this.maxHeartRate = maxHeartRate;
        this.calories = calories;
        this.cadence = cadence;
        this.startTime = startTime;
        this.endTime = endTime;
        this.lat = lat;
        this.lon = lon;
    }
}
