package com.likelion14.runcovery.activity;

import com.likelion14.runcovery.common.exception.CustomException;
import com.likelion14.runcovery.mission.MissionRepository;
import com.likelion14.runcovery.mission.Mission;
import com.likelion14.runcovery.user.User;
import com.likelion14.runcovery.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRecordRepository activityRecordRepository;
    private final UserRepository userRepository;
    private final MissionRepository missionRepository;

    // 활동 데이터 동기화 (저장/업데이트) 및 미션 완료 처리
    @Transactional
    public ActivitySyncResponseDto syncActivity(long userId, ActivityRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당하는 유저가 없습니다."));

        ActivityRecord activityRecord = saveOrUpdateActivity(user, request);

        Mission mission = missionRepository.findByConditionUserAndMissionDate(user, request.getRecordDate()).orElse(null);

        ActivitySyncResponseDto.MissionInfo missionInfo = null;
        if (mission != null && !mission.getIsRest()) {
            mission.complete();
            mission.setActivityId(activityRecord.getId());
            missionInfo = new ActivitySyncResponseDto.MissionInfo(mission.getId(), true);
        }

        return new ActivitySyncResponseDto(activityRecord.getId(), missionInfo);
    }

    // 오늘 날짜 기준 활동 기록 조회
    public ActivityRecordResponseDto getTodayActivity(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당하는 유저가 없습니다."));

        return activityRecordRepository.findByUserAndRecordDate(user, LocalDate.now())
                .map(ActivityRecordResponseDto::from)
                .orElse(null);
    }

    // 운동 기록 단건 조회
    public ActivityRecord getActivityRecord(long recordId) {
        return activityRecordRepository.findById(recordId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "일치하는 운동기록이 없습니다."));
    }

    // recordDate 기준 저장 또는 업데이트
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
}
