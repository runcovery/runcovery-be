INSERT INTO users (user_id, nickname, age, gender, height, weight, running_experience, max_run_duration, avg_sleep_hours) VALUES
(1, '강냉이', 22, '남성', 175.0, 70.0, '뛰다가 오래 쉬었어요.', 30, 7.00);

INSERT INTO body_part (body_part_code, body_name, side, direction) VALUES
('B_NECK', '뒷목', NULL, 'BACK'),
('B_UPPER_BACK_L', '등 상부', 'LEFT', 'BACK'),
('B_UPPER_BACK_R', '등 상부', 'RIGHT', 'BACK'),
('B_MID_BACK_L', '등 중부', 'LEFT', 'BACK'),
('B_MID_BACK_R', '등 중부', 'RIGHT', 'BACK'),
('B_LOWER_BACK_L', '허리', 'LEFT', 'BACK'),
('B_LOWER_BACK_R', '허리', 'RIGHT', 'BACK'),
('B_SHOULDER_L', '어깨', 'LEFT', 'BACK'),
('B_SHOULDER_R', '어깨', 'RIGHT', 'BACK'),
('B_UPPER_ARM_L', '위팔(삼두)', 'LEFT', 'BACK'),
('B_UPPER_ARM_R', '위팔(삼두)', 'RIGHT', 'BACK'),
('B_LOWER_ARM_L', '아래팔', 'LEFT', 'BACK'),
('B_LOWER_ARM_R', '아래팔', 'RIGHT', 'BACK'),
('B_GLUTES_L', '엉덩이', 'LEFT', 'BACK'),
('B_GLUTES_R', '엉덩이', 'RIGHT', 'BACK'),
('B_THIGH_L', '허벅지 뒤', 'LEFT', 'BACK'),
('B_THIGH_R', '허벅지 뒤', 'RIGHT', 'BACK'),
('B_KNEE_L', '오금', 'LEFT', 'BACK'),
('B_KNEE_R', '오금', 'RIGHT', 'BACK'),
('B_CALF_L', '종아리', 'LEFT', 'BACK'),
('B_CALF_R', '종아리', 'RIGHT', 'BACK'),
('F_NECK', '목 앞면', NULL, 'FRONT'),
('F_SHOULDER_L', '어깨', 'LEFT', 'FRONT'),
('F_SHOULDER_R', '어깨', 'RIGHT', 'FRONT'),
('F_CHEST_L', '가슴', 'LEFT', 'FRONT'),
('F_CHEST_R', '가슴', 'RIGHT', 'FRONT'),
('F_ABS', '복부', NULL, 'FRONT'),
('F_OBLIQUE_L', '옆구리', 'LEFT', 'FRONT'),
('F_OBLIQUE_R', '옆구리', 'RIGHT', 'FRONT'),
('F_UPPER_ARM_L', '위팔(이두)', 'LEFT', 'FRONT'),
('F_UPPER_ARM_R', '위팔(이두)', 'RIGHT', 'FRONT'),
('F_LOWER_ARM_L', '아래팔', 'LEFT', 'FRONT'),
('F_LOWER_ARM_R', '아래팔', 'RIGHT', 'FRONT'),
('F_PELVIS_L', '서혜부/골반', 'LEFT', 'FRONT'),
('F_PELVIS_R', '서혜부/골반', 'RIGHT', 'FRONT'),
('F_THIGH_L', '허벅지 앞', 'LEFT', 'FRONT'),
('F_THIGH_R', '허벅지 앞', 'RIGHT', 'FRONT'),
('F_KNEE_L', '무릎 앞', 'LEFT', 'FRONT'),
('F_KNEE_R', '무릎 앞', 'RIGHT', 'FRONT'),
('F_SHIN_L', '정강이', 'LEFT', 'FRONT'),
('F_SHIN_R', '정강이', 'RIGHT', 'FRONT');

INSERT INTO future_goal (future_id, user_id, scene, target_distance, target_period, weekly_frequency, available_time, achievement_rate) VALUES
(1, 1, '계단을 올라도 숨이 차지 않는 나', 5, 3, 2, 15, 0);

INSERT INTO weekly_goal (week_id, user_id, future_id, week_no, weekly_goal, weekly_goal_distance, expected_calories) VALUES
(1, 1, 1, 1, '목표 페이스 체감하기 및 기초 체력 향상', 11, 2800);

INSERT INTO weekly_schedule (training_id, week_id, training_content) VALUES
(1, 1, '중강도 조깅 (야외 평지)'),
(2, 1, '저강도/ 지속주 LSD (야외 장거리)');

INSERT INTO today_condition (condition_id, user_id, condition_date, sleep_hours, body_condition, active_calories, is_checked) VALUES
(1, 1, '2026-08-09', 7.00, 'good', 450, TRUE);

INSERT INTO today_mission (mission_id, condition_id, week_id, mission_date, recommended_intensity, recommended_time, recommended_zone, recommended_zone_desc, detail_comment, is_completed, is_rest) VALUES
(1, 1, 1, '2026-08-09', '권장 강도 | 중 · 고강도 러닝', '20분 내외로 도전해보세요.', 'Zone 3~4', '편안하게 대화할 수 있는 강도', '워밍업(5분)-메인(10분)-쿨다움(5분)', FALSE, FALSE);

# INSERT INTO activity_record (record_id, user_id, running_duration, record_date, distance_m, avg_pace, avg_heart_rate, max_heart_rate, calories, cadence, start_time, end_time) VALUES
# (1, 1, 53, '2026-08-08', 5000, 360, 145, 165, 350, 160, '2026-08-08 08:00:00', '2026-08-08 08:53:00');

INSERT INTO activity_record (record_id, user_id, running_duration, record_date, distance_m, avg_pace, avg_heart_rate, max_heart_rate, calories, cadence, start_time, end_time, lat, lon) VALUES
(1, 1, 53, '2026-08-08', 5000, 360, 145, 165, 350, 160, '2026-08-08 08:00:00', '2026-08-08 08:53:00', 37.5665, 126.9780);

INSERT INTO wellness_report (report_id, record_id, report_date, warning_title, running_intensity) VALUES
(1, 1, '2026-08-08', '운동 강도에 주의하세요', 7);

INSERT INTO skin_record (skin_id, user_id, type, measured_date, total_score, redness, oiliness, texture, pores, blemishes, hydration, pigment, skin_image) VALUES
(1, 1, 'AFTER_RUN', '2026-08-08', 78, 35, 42, 25, 30, 20, 65, 18, '/images/skin_001.jpg');

INSERT INTO prescription (prescription_id, report_id, skin_id, prescription_date, category, title, summary, detail, is_completed, recommended_link, skin_result) VALUES
(1, 1, 1, '2026-08-08', 'STRETCH', '허벅지 부분이 계속적으로 불편합니다.', '아픈 부위에 폼롤러로 스트레칭을 근육을 풀어주면 좋습니다.',
'STEP 1 종아리 & 아킬레스건 집중 스트레칭 (부위별 30초)
벽을 손으로 짚고 한쪽 다리를 뒤로 길게 뺍니다. 뒤쪽 발뒤꿈치를 바닥에 완전히 붙이고 골반을 앞으로 밀어 종아리를 늘려줍니다.
STEP 2 종아리 & 아킬레스건 집중 스트레칭 (부위별 30초)
벽을 손으로 짚고 한쪽 다리를 뒤로 길게 뺍니다. 뒤쪽 발뒤꿈치를 바닥에 완전히 붙이고 골반을 앞으로 밀어 종아리를 늘려줍니다.
STEP 3 종아리 & 아킬레스건 집중 스트레칭 (부위별 30초)
벽을 손으로 짚고 한쪽 다리를 뒤로 길게 뺍니다. 뒤쪽 발뒤꿈치를 바닥에 완전히 붙이고 골반을 앞으로 밀어 종아리를 늘려줍니다.',
FALSE, 'https://youtu.be/yl2yhch2_ys?si=pYx-PUTUJ52aeYml', NULL),
(2, 1, 1, '2026-08-08', 'SKIN', '야외 러닝으로 자외선 노출 및 열감이 심합니다.', '모공 확장을 막기 위해 즉각적인 쿨링 세안이 필요합니다.',
NULL, FALSE, NULL, '측정 결과 얼굴에 열감이 많아보여요!'),
(3, 1, 1, '2026-08-08', 'NUTRITION', '땀 배출량이 최고 수준입니다.', '땀 배출량이 많기 때문에 지금 즉시 수분 500ml와 전해질을 보충해 근손실을 막으세요.',
NULL, FALSE, NULL, NULL);

INSERT INTO body_issue (user_id, body_part_code, is_painful) VALUES
(1, 'B_CALF_L', TRUE);
