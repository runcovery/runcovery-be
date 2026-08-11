package com.likelion14.runcovery.service;

import com.likelion14.runcovery.common.exception.CustomException;
import com.likelion14.runcovery.dto.WeatherResponseDto;
import com.likelion14.runcovery.entity.ActivityRecord;
import com.likelion14.runcovery.repository.ActivityRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRecordRepository activityRecordRepository;

    public ActivityRecord getActivityRecord(long recordId) {
        return activityRecordRepository.findById(recordId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "일치하는 운동기록이 없습니다."));
    }

}
