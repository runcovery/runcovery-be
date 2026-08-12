package com.likelion14.runcovery.condition;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConditionService {

    public ConditionResponseDto analyzeCondition(ConditionRequestDto request) {

        // 1. 유저 조회

        // 2. 최근 4일 운동 현황 조회
        //    - 운동 완료 횟수 (is_completed=true, is_rest=false)
        //    - 휴식 횟수 (is_rest=true)
        //    - 마지막 운동일 (activity_record.record_date 기준)

        // 3. sleepQuality → sleepHours 변환

        // 4. TodayCondition entity 생성 및 저장, 컨디션 체크 여부 업데이트

        // 5. OpenAI에 컨디션 분석 요청 (수면, 운동기록, 통증부위, 몸상태 전달)

        // 6. 분석 결과로 ConditionResponseDto 반환

        return new ConditionResponseDto();
    }
}
