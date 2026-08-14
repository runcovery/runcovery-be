package com.likelion14.runcovery.wellness.service;
import com.likelion14.runcovery.wellness.dto.SkinScoreComparisonResponse;
import com.likelion14.runcovery.wellness.entity.SkinRecord;
import com.likelion14.runcovery.wellness.enums.SkinRecordType;
import com.likelion14.runcovery.wellness.repository.SkinRecordQueryRepository;

import com.likelion14.runcovery.common.exception.CustomException;
import com.likelion14.runcovery.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WellnessSkinScoreComparisonService {

    private static final SkinRecordType COMPARISON_TYPE = SkinRecordType.AFTER_CARE;

    private final UserRepository userRepository;
    private final SkinRecordQueryRepository skinRecordQueryRepository;

    public SkinScoreComparisonResponse compare(Long userId, LocalDate measuredDate) {
        validateUserId(userId);

        if (!userRepository.existsById(userId)) {
            throw new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");
        }

        LocalDate today = measuredDate == null ? LocalDate.now() : measuredDate;
        LocalDate previousDay = today.minusDays(1);

        SkinRecord todayRecord = findRecord(userId, today, "오늘");
        SkinRecord previousDayRecord = findRecord(userId, previousDay, "전날");

        return new SkinScoreComparisonResponse(
                COMPARISON_TYPE,
                SkinScoreComparisonResponse.SkinScoreSnapshot.from(todayRecord),
                SkinScoreComparisonResponse.SkinScoreSnapshot.from(previousDayRecord),
                SkinScoreComparisonResponse.SkinScoreDifference.between(todayRecord, previousDayRecord)
        );
    }

    private SkinRecord findRecord(Long userId, LocalDate measuredDate, String dateLabel) {
        return skinRecordQueryRepository
                .findFirstByUser_IdAndTypeAndMeasuredDateOrderByIdDesc(
                        userId,
                        COMPARISON_TYPE,
                        measuredDate
                )
                .orElseThrow(() -> new CustomException(
                        HttpStatus.NOT_FOUND,
                        dateLabel + " AFTER_CARE 피부 측정 기록이 없습니다."
                ));
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "userId는 필수입니다.");
        }
    }
}
