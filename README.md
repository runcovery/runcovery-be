# Runcovery Wellness API

현재 코드 기준의 wellness API 실행 방법과 Postman 통합 테스트 순서입니다.

## 주요 기능

- AFTER_RUN / AFTER_CARE 피부 스캔 및 날짜별 기록 조회
- 전날 대비 AFTER_CARE 피부 점수 비교
- 러닝·날씨·피부·컨디션·설문 기반 리포트 생성
- 리포트와 카테고리별 처방전 조회
- 통증 부위 기반 YouTube 회복 영상 추천
- 피부·스트레칭 처방전 완료 상태 변경
- X-Public-Id UUID 기반 사용자 식별

## 사용자 식별

wellness API는 userId 또는 memberId 쿼리 파라미터를 사용하지 않습니다.

사용자 등록 시 클라이언트가 생성한 UUID를 전달하고, 이후 사용자 전용 요청에는 다음 헤더를 추가합니다.

~~~text
X-Public-Id: 사용자 UUID
~~~

서버의 @CurrentUserId Long userId는 UUID를 내부 DB PK로 변환한 값입니다.

## 환경변수와 서버 실행

~~~powershell
$env:SPRING_AI_OPENAI_API_KEY="OpenAI API Key"
$env:OPENWEATHER_API_KEY="OpenWeather API Key"
$env:YOUTUBE_API_KEY="YouTube Data API Key"
~~~

DB 접속 정보와 API Key는 Git에 커밋하지 않습니다. application.properties는 Git에서 제외되어 있으므로 환경별 설정이 필요합니다.

Python 피부 분석 서버:

~~~powershell
cd C:\runcovery\skin-scan-main
python -m uvicorn src.app.main:app --host 127.0.0.1 --port 8000
~~~

- Python 문서: http://localhost:8000/docs
- Python multipart Key: image
- Spring multipart Key: file

Spring Boot:

~~~powershell
cd C:\runcovery\runcovery-be
.\gradlew.bat bootRun
~~~

- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui/index.html

## Postman Environment

| 변수 | 값 |
|---|---|
| baseUrl | http://localhost:8080 |
| publicId | 클라이언트가 생성한 UUID |
| recordDate | 실제 테스트 당일 YYYY-MM-DD |
| activityRecordId | 활동 저장 응답의 recordId |
| reportId | 리포트 조회 응답의 reportId |
| prescriptionId | 처방전 목록의 prescriptionId |

사용자 등록 외 모든 요청에 X-Public-Id: {{publicId}}를 추가합니다.

컨디션과 피부 스캔 날짜는 서버의 LocalDate.now()로 저장되므로 러닝 날짜와 시각도 실제 테스트 당일로 맞춥니다.

## 통합 테스트 순서

### 1. 사용자 등록

PowerShell New-Guid 등으로 UUID를 생성합니다.

~~~http
POST {{baseUrl}}/users
Content-Type: application/json
~~~

~~~json
{
  "userId": "{{publicId}}",
  "nickname": "테스트러너",
  "age": 25,
  "gender": "남성",
  "height": 175.0,
  "weight": 70.0,
  "runningExperience": "가끔 달리고 있어요."
}
~~~

### 2. 당일 컨디션 저장

~~~http
POST {{baseUrl}}/conditions
X-Public-Id: {{publicId}}
Content-Type: application/json
~~~

~~~json
{
  "bodyCondition": "FAIR",
  "sleepQuality": "FAIR",
  "painAreas": ["F_KNEE_L", "B_THIGH_R"]
}
~~~

bodyCondition과 sleepQuality 허용값은 GOOD, FAIR, POOR입니다.

### 3. 당일 러닝 기록 저장

~~~http
POST {{baseUrl}}/activities/sync
X-Public-Id: {{publicId}}
Content-Type: application/json
~~~

~~~json
{
  "runningDuration": 1800,
  "recordDate": "{{recordDate}}",
  "distanceM": 5000,
  "avgPace": 360,
  "avgHeartRate": 145,
  "maxHeartRate": 170,
  "calories": 380,
  "cadence": 170,
  "startTime": "2026-08-17T18:00:00",
  "endTime": "2026-08-17T18:30:00",
  "lat": 37.5665,
  "lon": 126.9780
}
~~~

응답의 data.recordId를 activityRecordId로 저장합니다.

### 4. AFTER_RUN 피부 스캔

~~~http
POST {{baseUrl}}/wellness/skin/scan?type=AFTER_RUN
X-Public-Id: {{publicId}}
~~~

Postman Body는 form-data, Key는 file, 타입은 File입니다. Content-Type은 직접 추가하지 않습니다.

### 5. 웰니스 리포트 생성

~~~http
POST {{baseUrl}}/api/wellness/reports
X-Public-Id: {{publicId}}
Content-Type: application/json
~~~

~~~json
{
  "recordDate": "{{recordDate}}",
  "activityRecordId": {{activityRecordId}},
  "survey": {
    "feeling": "NORMAL",
    "energy": "TIRED",
    "sweat": "MODERATE"
  },
  "painPartCodes": ["F_KNEE_L", "B_THIGH_R"]
}
~~~

설문 Enum:

- feeling: GREAT, NORMAL, EXHAUSTED
- energy: DEPLETED, TIRED, ENERGETIC
- sweat: LOW, MODERATE, HIGH

필수 선행 데이터는 같은 사용자·날짜의 러닝 기록, 최신 AFTER_RUN 피부 기록, 최신 컨디션 기록입니다. 러닝 기록에는 위도·경도·시작 시각이 필요합니다.

응답에는 intensity, hydration, skin, stretching, recoveryVideos, uncoveredPainPartCodes가 포함됩니다.

동일 상체 또는 동일 하체는 영상 1개, 상체와 하체가 함께 선택된 경우에만 최대 2개를 반환합니다. 영상 자막을 분석하지 않으므로 steps는 응답에 노출하지 않습니다.

### 6. 리포트 조회

~~~http
GET {{baseUrl}}/api/wellness/reports
GET {{baseUrl}}/api/wellness/reports?reportDate={{recordDate}}
GET {{baseUrl}}/api/wellness/reports/{{reportId}}
X-Public-Id: {{publicId}}
~~~

### 7. 처방전 목록과 상세 조회

~~~http
GET {{baseUrl}}/api/wellness/prescriptions?reportId={{reportId}}
GET {{baseUrl}}/api/wellness/prescriptions/{{prescriptionId}}
X-Public-Id: {{publicId}}
~~~

- NUTRITION: completionSupported=false, 러닝 시간·칼로리 상세
- SKIN: completionSupported=true, 연결된 AFTER_RUN 피부 상세
- STRETCH: completionSupported=true, 영상 URL과 추천 이유

### 8. 피부·스트레칭 완료 상태 변경

피부진단 보기 또는 영상 보러가기 성공 후 호출합니다.

~~~http
PATCH {{baseUrl}}/api/wellness/prescriptions/SKIN/complete?reportId={{reportId}}
X-Public-Id: {{publicId}}
Content-Type: application/json
~~~

~~~json
{
  "isCompleted": true
}
~~~

스트레칭은 SKIN을 STRETCH로 변경합니다. NUTRITION은 완료 대상이 아니며 400을 반환합니다.

### 9. AFTER_CARE 피부 스캔

~~~http
POST {{baseUrl}}/wellness/skin/scan?type=AFTER_CARE
X-Public-Id: {{publicId}}
~~~

Body 설정은 AFTER_RUN과 같습니다.

### 10. 날짜별 피부 기록 조회

~~~http
GET {{baseUrl}}/wellness/skin/records?date={{recordDate}}
X-Public-Id: {{publicId}}
~~~

### 11. 전날 대비 AFTER_CARE 비교

~~~http
GET {{baseUrl}}/wellness/skin/comparison?date={{recordDate}}
X-Public-Id: {{publicId}}
~~~

기준일과 정확히 하루 전 날짜의 AFTER_CARE 기록이 모두 필요합니다.

## DB 확인

~~~sql
SELECT * FROM conditions
WHERE user_id = :user_id
ORDER BY condition_date DESC, condition_id DESC;

SELECT * FROM skin_record
WHERE user_id = :user_id
ORDER BY measured_date DESC, skin_id DESC;

SELECT wr.*
FROM wellness_report wr
JOIN activity_record ar ON ar.record_id = wr.record_id
WHERE ar.user_id = :user_id
ORDER BY wr.report_date DESC, wr.report_id DESC;

SELECT p.*
FROM prescription p
JOIN wellness_report wr ON wr.report_id = p.report_id
JOIN activity_record ar ON ar.record_id = wr.record_id
WHERE ar.user_id = :user_id
ORDER BY p.prescription_date DESC, p.prescription_id DESC;
~~~

## 자주 발생하는 오류

- X-Public-Id 헤더 필요: Headers에 X-Public-Id를 추가합니다.
- 유효하지 않은 사용자: POST /users로 등록한 UUID인지 확인합니다.
- Required part file is not present: form-data / file / File 설정을 확인합니다.
- Content-Type is not supported: 피부는 form-data, 리포트와 PATCH는 raw JSON입니다.
- 피부 분석 서버 연결 실패: Python 서버 127.0.0.1:8000을 확인합니다.
- AFTER_RUN 기록 없음: 러닝 날짜와 같은 날 AFTER_RUN 스캔이 필요합니다.
- 컨디션 기록 없음: 같은 사용자·날짜의 POST /conditions 기록이 필요합니다.
- Incorrect string value: MariaDB/MySQL 문자셋을 utf8mb4로 맞춥니다.

## 현재 코드 주의사항

- 동일 activityRecordId로 리포트를 반복 생성하면 중복 저장될 수 있습니다.
- 피부 이미지 원본 대신 업로드 파일명만 skinImage에 저장합니다.
- YouTube 자막은 분석하지 않고 메타데이터와 선택 부위로 추천 이유를 만듭니다.
- AI 프롬프트에는 sleepQuality가 포함되지만 bodyCondition 반영은 현재 누락되어 있습니다.
- 프론트와 백엔드 Origin이 다르면 X-Public-Id를 허용하는 CORS 설정이 필요합니다.

## 빌드와 테스트

~~~powershell
.\gradlew.bat clean test
~~~

현재 자동 테스트는 컨텍스트 로딩 1건뿐입니다. 사용자 격리, 외부 API 실패, 트랜잭션 롤백, 완료 처리와 영상 그룹화 통합 테스트를 추가해야 합니다.