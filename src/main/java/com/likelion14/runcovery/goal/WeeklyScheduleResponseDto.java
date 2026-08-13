package com.likelion14.runcovery.goal;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

@Getter
@JsonPropertyOrder({ "trainingId", "trainingContent" })
public class WeeklyScheduleResponseDto {

    private final Long trainingId;
    private final String trainingContent;

    public WeeklyScheduleResponseDto(WeeklySchedule schedule) {
        this.trainingId = schedule.getId();
        this.trainingContent = schedule.getTrainingContent();
    }
}
