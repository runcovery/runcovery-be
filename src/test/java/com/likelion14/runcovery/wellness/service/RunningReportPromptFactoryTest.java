package com.likelion14.runcovery.wellness.service;

import com.likelion14.runcovery.activity.ActivityRecord;
import com.likelion14.runcovery.condition.BodyCondition;
import com.likelion14.runcovery.condition.Condition;
import com.likelion14.runcovery.condition.SleepQuality;
import com.likelion14.runcovery.user.User;
import com.likelion14.runcovery.wellness.dto.ReportRequestDto;
import com.likelion14.runcovery.wellness.entity.SkinRecord;
import com.likelion14.runcovery.wellness.enums.EnergyStatus;
import com.likelion14.runcovery.wellness.enums.FeelingStatus;
import com.likelion14.runcovery.wellness.enums.SkinRecordType;
import com.likelion14.runcovery.wellness.enums.SweatStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RunningReportPromptFactoryTest {

    private final RunningReportPromptFactory promptFactory = new RunningReportPromptFactory();

    @Test
    void includesConditionAndSurveyWithoutInternalUserId() {
        User user = new User(
                UUID.randomUUID(), "테스터", 25, "여성",
                BigDecimal.valueOf(165), BigDecimal.valueOf(55), "초보"
        );
        LocalDate recordDate = LocalDate.of(2026, 8, 17);
        ActivityRecord activity = new ActivityRecord(
                user, 1800, recordDate, 5000, 360, 145, 172, 320, 170,
                LocalDateTime.of(2026, 8, 17, 18, 0),
                LocalDateTime.of(2026, 8, 17, 18, 30),
                37.5, 127.0
        );
        SkinRecord skinRecord = new SkinRecord(
                user, SkinRecordType.AFTER_RUN, recordDate,
                61, 76, 89, 100, 79, 64, 85
        );
        skinRecord.setTotalScore(79);
        Condition condition = new Condition(user, recordDate, SleepQuality.POOR, BodyCondition.EXHAUSTED);
        ReportRequestDto request = ReportRequestDto.builder()
                .recordDate(recordDate)
                .survey(ReportRequestDto.SurveyData.builder()
                        .feeling(FeelingStatus.NORMAL)
                        .energy(EnergyStatus.TIRED)
                        .sweat(SweatStatus.MODERATE)
                        .build())
                .painPartCodes(List.of())
                .build();
        RunningReportPromptFactory.PromptData promptData = new RunningReportPromptFactory.PromptData(
                user, activity, skinRecord, condition, List.of(), null,
                500, 650, 0.7
        );

        String prompt = promptFactory.buildUserPrompt(request, promptData);

        assertTrue(prompt.contains("수면 상태: POOR"));
        assertTrue(prompt.contains("신체 상태: EXHAUSTED"));
        assertTrue(prompt.contains("feeling: NORMAL"));
        assertTrue(prompt.contains("권장 수분 보충량: 약 650ml"));
        assertFalse(prompt.contains("사용자 ID"));
    }
}