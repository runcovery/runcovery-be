package com.likelion14.runcovery.activity;

import com.likelion14.runcovery.common.exception.CustomException;
import com.likelion14.runcovery.user.User;
import com.likelion14.runcovery.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRecordRepository activityRecordRepository;
    private final UserRepository userRepository;

    @Transactional
    public ActivitySyncResponseDto syncActivity(ActivityRequestDto request) {
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당하는 유저가 없습니다."));

        ActivityRecord activityRecord = saveOrUpdateActivity(user, request);

        // 미션 완료여부 업데이트

        return new ActivitySyncResponseDto(activityRecord.getId(),null);
    }

    public ActivityRecord getActivityRecord(long recordId) {
        return activityRecordRepository.findById(recordId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "일치하는 운동기록이 없습니다."));
    }

    private ActivityRecord saveOrUpdateActivity(User user, ActivityRequestDto request) {
        return activityRecordRepository.findByUserAndRecordDate(user, request.getRecordDate())
                .map(existing -> {
                    existing.update(
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
                            request.getLon());
                    return activityRecordRepository.save(existing);
                })
                .orElseGet(() -> activityRecordRepository.save(
                        new ActivityRecord(
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
                )));
    }

    public ActivityRecordResponseDto getTodayActivity() {
        LocalDate today = LocalDate.now();
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당하는 유저가 없습니다."));

        return activityRecordRepository.findByUserAndRecordDate(user, today)
                .map(record -> new ActivityRecordResponseDto(
                        record.getId(),
                        record.getRecordDate(),
                        record.getRunningDuration(),
                        record.getDistanceM(),
                        record.getAvgPace(),
                        record.getAvgHeartRate(),
                        record.getMaxHeartRate(),
                        record.getCalories(),
                        record.getCadence(),
                        record.getStartTime(),
                        record.getEndTime()
                ))
                .orElse(null);
    }
}
