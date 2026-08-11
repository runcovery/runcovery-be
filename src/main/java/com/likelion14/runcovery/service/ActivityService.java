package com.likelion14.runcovery.service;

import com.likelion14.runcovery.common.exception.CustomException;
import com.likelion14.runcovery.repository.ActivityRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRecordRepository activityRecordRepository;

    //특정 운동기록의 시작시간 반환
    public LocalDateTime getActivityStartTime(long recordId) {
        return activityRecordRepository.findById(recordId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "일치하는 활동 기록이 없습니다."))
                .getStartTime();
    }

}
