package com.likelion14.runcovery.condition;

import com.likelion14.runcovery.activity.ActivityRecord;
import com.likelion14.runcovery.activity.ActivityRecordRepository;
import com.likelion14.runcovery.activity.ActivityService;
import com.likelion14.runcovery.common.exception.CustomException;
import com.likelion14.runcovery.mission.MissionRepository;
import com.likelion14.runcovery.user.User;
import com.likelion14.runcovery.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConditionService {

    private final UserRepository userRepository;
    private final MissionRepository missionRepository;
    private final ActivityRecordRepository activityRecordRepository;

    public ConditionResponseDto analyzeCondition(ConditionRequestDto request) {

        // 1. 유저 조회
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당하는 유저가 없습니다."));

        // 2. 최근 4일 운동 현황 조회
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(4);
        //    - 운동 완료 횟수 (is_completed=true, is_rest=false)
        int completedCount = missionRepository.findByMissionDateBetweenAndIsCompletedTrueAndIsRestFalse(start, end).size();
        //    - 휴식 횟수 (is_rest=true)
        int restCount = missionRepository.findByMissionDateBetweenAndIsRestTrue(start, end).size();
        //    - 마지막 운동일 (activity_record.record_date 기준)
        LocalDate lastRunDate = activityRecordRepository.findTopByUserOrderByRecordDateDesc(user)
                .map(ActivityRecord::getRecordDate)
                .orElse(null);

        log.info("운동 완료 횟수 : {}, 휴식 횟수 : {}, 마지막 운동일 : {}", completedCount, restCount, lastRunDate);

        // 3. sleepQuality → sleepHours 변환


        // 4. TodayCondition entity 생성 및 저장, 컨디션 체크 여부 업데이트

        // 5. OpenAI에 컨디션 분석 요청 (수면, 운동기록, 통증부위, 몸상태 전달)

        // 6. 분석 결과로 ConditionResponseDto 반환

        return new ConditionResponseDto();
    }
}
