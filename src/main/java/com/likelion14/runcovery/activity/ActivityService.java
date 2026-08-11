package com.likelion14.runcovery.activity;

import com.likelion14.runcovery.common.exception.CustomException;
import com.likelion14.runcovery.user.User;
import com.likelion14.runcovery.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRecordRepository activityRecordRepository;
    private final UserRepository userRepository;

    public ActivitySyncResponseDto syncActivity(ActivityRequestDto request) {
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당하는 유저가 없습니다."));

        ActivityRecord activity = new ActivityRecord (
                user,
                request.getRunningDuration(),
                request.getRecordDate(),
                request.getDistanceM(),
                request.getAvgPace(),
                request.getAvgHeartRate(),
                request.getMaxHeartRate(),
                request.getCalories(),
                request.getCadence(),
                request.getStartTime(),
                request.getEndTime(),
                request.getLat(),
                request.getLon()
        );

        // 미션테이블 complete 변경 필요

        // 활동테이블 저장
        ActivityRecord savedActivity = activityRecordRepository.save(activity);

        return new ActivitySyncResponseDto(savedActivity.getId(),null);
    }

    public ActivityRecord getActivityRecord(long recordId) {
        return activityRecordRepository.findById(recordId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "일치하는 운동기록이 없습니다."));
    }

}
