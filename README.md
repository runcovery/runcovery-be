```markdown
# 맞춤형 웰니스 러닝 리포트 API

## 기능 개요

러닝 기록, 운동 당시 날씨, AFTER_RUN 피부 점수, 수면 컨디션, 운동 설문, 아픈 부위를 기반으로 맞춤형 웰니스 리포트를 생성합니다.

리포트에는 다음 내용이 포함됩니다.

- 러닝 강도 점수 및 코멘트
- 수분/영양 처방
- 피부 관리 처방
- 스트레칭 처방
- 아픈 부위 기반 YouTube 회복 영상
- 스트레칭 단계별 안내

현재 회원가입 기능이 제거되어 테스트 사용자 ID는 `1`로 고정되어 있습니다.

---

## 패키지 구조

```text
wellness/
├── controller
│   ├── RunningReportController.java
│   ├── PrescriptionController.java
│   ├── WellnessSkinScanController.java
│   ├── WellnessSkinRecordQueryController.java
│   └── WellnessSkinScoreComparisonController.java
│
├── service
│   ├── RunningReportService.java
│   ├── WellnessReportQueryService.java
│   ├── PrescriptionQueryService.java
│   ├── PrescriptionQueryService.java
│   ├── WellnessSkinScanService.java
│   └── YouTubeVideoSearchService.java
│
├── dto
│   ├── ReportRequestDto.java
│   ├── ReportResponseDto.java
│   ├── WellnessReportQueryResponseDto.java
│   └── PrescriptionQueryResponseDto.java
│
├── entity
│   ├── WellnessReport.java
│   ├── Prescription.java
│   └── SkinRecord.java
│
├── repository
│   ├── WellnessReportRepository.java
│   ├── PrescriptionRepository.java
│   └── SkinRecordRepository.java
│
└── enums
    ├── FeelingStatus.java
    ├── EnergyStatus.java
    ├── SweatStatus.java
    ├── PrescriptionCategory.java
    └── SkinRecordType.java
```

---

## 외부 서버 및 API Key 설정

### Python 피부 분석 서버

```powershell
cd C:\runcovery\skin-scan-main

python -m uvicorn src.app.main:app --host 127.0.0.1 --port 8000
```

Python API 문서:

```text
http://localhost:8000/docs
```

### Spring Boot 서버

```powershell
cd C:\runcovery\runcovery-be

.\gradlew.bat bootRun
```

Spring Swagger:

```text
http://localhost:8080/swagger-ui/index.html
```

### 환경변수

```powershell
$env:SPRING_AI_OPENAI_API_KEY="OpenAI_API_KEY"
$env:OPENWEATHER_API_KEY="OpenWeather_API_KEY"
$env:YOUTUBE_API_KEY="YouTube_Data_API_KEY"
```

API Key를 README나 Git 저장소에 직접 작성하지 않습니다.

---

# API 테스트 순서

리포트 생성 전에 러닝 기록, 수면 컨디션, AFTER_RUN 피부 기록이 저장되어 있어야 합니다.

## 1. 러닝 기록 저장

```http
POST http://localhost:8080/activities/sync
```

Header:

```text
Content-Type: application/json
```

Body:

```json
{
  "runningDuration": 1800,
  "recordDate": "2026-08-14",
  "distanceM": 5000,
  "avgPace": 360,
  "avgHeartRate": 145,
  "maxHeartRate": 170,
  "calories": 380,
  "cadence": 170,
  "startTime": "2026-08-14T18:00:00",
  "endTime": "2026-08-14T18:30:00",
  "lat": 37.5665,
  "lon": 126.9780
}
```

`recordDate`는 테스트 당일 날짜로 변경합니다.

---

## 2. 수면 컨디션 저장

```http
POST http://localhost:8080/conditions
```

Header:

```text
Content-Type: application/json
```

Body:

```json
{
  "bodyCondition": "FAIR",
  "sleepQuality": "FAIR",
  "painAreas": [
    "B_KNEE_L",
    "F_THIGH_L"
  ]
}
```

가능한 값:

```text
bodyCondition: GOOD, FAIR, POOR
sleepQuality: GOOD, FAIR, POOR
```

---

## 3. AFTER_RUN 피부 스캔

```http
POST http://localhost:8080/wellness/skin/scan?userId=1&type=AFTER_RUN
```

Postman 설정:

```text
Body > form-data
Key: file
Type: File
Value: 피부 이미지
```

주의사항:

- Key 이름은 반드시 `file`
- Type은 반드시 `File`
- Body는 `raw`가 아닌 `form-data`
- 날짜는 서버의 오늘 날짜로 저장됨

---

# 리포트 API

## 4. 리포트 생성 및 저장

```http
POST http://localhost:8080/api/wellness/reports
```

Header:

```text
Content-Type: application/json
```

Body:

```json
{
  "recordDate": "2026-08-14",
  "survey": {
    "feeling": "NORMAL",
    "energy": "TIRED",
    "sweat": "MODERATE"
  },
  "painPartCodes": [
    "B_KNEE_L",
    "F_THIGH_L"
  ]
}
```

설문 Enum:

```text
feeling:
GREAT, NORMAL, EXHAUSTED

energy:
DEPLETED, TIRED, ENERGETIC

sweat:
LOW, MODERATE, HIGH
```

리포트 생성 시 다음 데이터가 저장됩니다.

- `wellness_report`: 러닝 강도 점수 및 코멘트
- `prescription` NUTRITION: 수분/영양 처방
- `prescription` SKIN: 피부 처방
- `prescription` STRETCH: 스트레칭 처방 및 YouTube 링크
- `body_issue`: 사용자가 선택한 아픈 부위

응답 상태 코드는 `201 Created`입니다.

---

# 리포트 조회 API

## 최신 리포트 조회

```http
GET http://localhost:8080/api/wellness/reports?userId=1
```

가장 최근에 생성된 리포트를 반환합니다.

## 날짜별 리포트 조회

```http
GET http://localhost:8080/api/wellness/reports?userId=1&reportDate=2026-08-14
```

## 리포트 ID 조회

```http
GET http://localhost:8080/api/wellness/reports/1?userId=1
```

응답 예시:

```json
{
  "reportId": 1,
  "userId": 1,
  "activityRecordId": 1,
  "reportDate": "2026-08-14",
  "runningIntensity": 7,
  "intensityLevel": "MODERATE",
  "comment": "평균 심박수와 운동 시간을 고려하면 중간 강도의 운동이었어요."
}
```

---

# 처방전 조회 API

## 최신 리포트의 처방전 목록 조회

```http
GET http://localhost:8080/api/wellness/prescriptions?userId=1
```

## 특정 리포트의 처방전 목록 조회

```http
GET http://localhost:8080/api/wellness/prescriptions?userId=1&reportId=1
```

응답 예시:

```json
[
  {
    "prescriptionId": 1,
    "reportId": 1,
    "prescriptionDate": "2026-08-14",
    "category": "NUTRITION",
    "title": "운동 후 수분 보충",
    "summary": "운동 후 충분한 수분과 전해질을 보충해 주세요.",
    "isCompleted": false
  },
  {
    "prescriptionId": 2,
    "reportId": 1,
    "prescriptionDate": "2026-08-14",
    "category": "SKIN",
    "title": "운동 후 피부 진정",
    "summary": "땀을 씻어내고 수분 크림으로 피부를 진정시켜 주세요.",
    "isCompleted": false
  },
  {
    "prescriptionId": 3,
    "reportId": 1,
    "prescriptionDate": "2026-08-14",
    "category": "STRETCH",
    "title": "오금, 허벅지 앞 스트레칭",
    "summary": "무릎과 허벅지 앞쪽을 중심으로 스트레칭해 주세요.",
    "isCompleted": false
  }
]
```

가능한 처방전 카테고리:

```text
NUTRITION
SKIN
STRETCH
```

## 처방전 상세 조회

```http
GET http://localhost:8080/api/wellness/prescriptions/3?userId=1
```

응답 예시:

```json
{
  "prescriptionId": 3,
  "reportId": 1,
  "skinRecordId": 1,
  "prescriptionDate": "2026-08-14",
  "category": "STRETCH",
  "title": "오금, 허벅지 앞 스트레칭",
  "summary": "무릎과 허벅지 앞쪽을 중심으로 스트레칭해 주세요.",
  "detail": "STEP 1: 오금 주변을 천천히 늘려 주세요.\nSTEP 2: 허벅지 앞쪽을 20초간 유지해 주세요.",
  "isCompleted": false,
  "recommendedLink": "https://www.youtube.com/watch?v=실제영상ID",
  "skinResult": null
}
```

---

# DB 조회

```sql
SELECT *
FROM wellness_report
WHERE record_id IN (
    SELECT record_id
    FROM activity_record
    WHERE user_id = 1
)
ORDER BY report_id DESC;
```

```sql
SELECT *
FROM prescription
WHERE report_id = 1
ORDER BY category;
```

```sql
SELECT *
FROM body_issue
WHERE user_id = 1;
```

---

# 오류 해결

## Required part 'file' is not present

피부 스캔 요청에서 발생합니다.

- Body가 `form-data`인지 확인
- Key가 `file`인지 확인
- Type이 `File`인지 확인
- 이미지 파일이 선택되어 있는지 확인

## Content-Type is not supported

리포트 API는 JSON 요청입니다.

```text
Body > raw > JSON
Content-Type: application/json
```

피부 스캔 API는 `form-data`를 사용합니다.

## YouTube API Key 오류

```powershell
$env:YOUTUBE_API_KEY="YouTube_Data_API_KEY"
```

환경변수 등록 후 Spring Boot 서버를 재시작합니다.

## 리포트는 조회되지만 처방전 목록이 비어 있음

기존에 생성된 리포트는 처방전 저장 로직이 적용되기 전 데이터일 수 있습니다.

리포트 생성 API를 다시 호출하면 다음 3개 처방전이 저장됩니다.

```text
NUTRITION
SKIN
STRETCH
```

## 당일 피부 기록을 찾을 수 없음

리포트 생성 전에 반드시 다음 API를 먼저 호출해야 합니다.

```http
POST /wellness/skin/scan?userId=1&type=AFTER_RUN
```

러닝 기록, 수면 컨디션, 피부 스캔의 날짜가 서로 같은지 확인합니다.
```
