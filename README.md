생성만
```markdown
# 맞춤형 웰니스 러닝 리포트 API

## 1. 기능 개요

러닝 기록, 운동 당시 날씨, AFTER_RUN 피부 점수, 수면 컨디션, 운동 설문, 아픈 부위 정보를 기반으로 맞춤형 웰니스 리포트를 생성합니다.

생성된 리포트는 다음 정보를 포함합니다.

- 오늘의 러닝 강도 점수
- 러닝 강도별 코멘트
- 수분/영양 처방
- 피부 관리 처방
- 아픈 부위 기반 스트레칭 처방
- 실제 YouTube 스트레칭 영상
- 회복 스트레칭 단계별 안내

현재 회원가입 기능이 제거되어 테스트 사용자 ID는 `1`로 고정되어 있습니다.

---

## 2. 사전 준비

리포트 API를 호출하기 전에 아래 데이터가 필요합니다.

- `users.user_id = 1` 사용자 데이터
- 당일 `activity_record` 러닝 기록
- 당일 `today_condition` 수면 컨디션
- 당일 `skin_record`의 `type = AFTER_RUN` 피부 스캔 기록
- `body_part` 마스터 데이터
- OpenAI API Key
- OpenWeather API Key
- YouTube Data API Key
- Python 피부 분석 서버 실행

리포트 기준 날짜는 기본적으로 오늘 날짜입니다.

과거 날짜를 사용하려면 해당 날짜에 러닝·피부·컨디션 데이터가 모두 존재해야 합니다.

---

## 3. API Key 설정

PowerShell에서 아래 환경변수를 등록합니다.

```powershell
$env:SPRING_AI_OPENAI_API_KEY="OpenAI_API_KEY"
$env:OPENWEATHER_API_KEY="OpenWeather_API_KEY"
$env:YOUTUBE_API_KEY="YouTube_Data_API_KEY"
```

그 후 백엔드 서버를 재시작합니다.

API Key를 README 또는 Git 저장소에 직접 작성하지 않습니다.

현재 `application.properties`에 API Key가 직접 노출되어 있다면 보안을 위해 해당 키를 폐기하고 재발급하는 것을 권장합니다.

---

## 4. Python 피부 분석 서버 실행

별도의 PowerShell 창에서 실행합니다.

```powershell
cd C:\runcovery\skin-scan-main

python -m uvicorn src.app.main:app --host 127.0.0.1 --port 8000
```

정상 실행되면 다음 주소에서 API 문서를 확인할 수 있습니다.

```text
http://localhost:8000/docs
```

Python 서버가 실행되지 않은 상태에서 피부 스캔 API를 호출하면 연결 오류가 발생합니다.

---

## 5. Spring Boot 서버 실행

```powershell
cd C:\runcovery\runcovery-be

.\gradlew.bat bootRun
```

기본 서버 주소:

```text
http://localhost:8080
```

Swagger 주소:

```text
http://localhost:8080/swagger-ui/index.html
```

---

# 6. Postman 테스트 순서

리포트 API는 기존에 저장된 러닝·피부·컨디션 데이터를 사용하므로 아래 순서대로 테스트해야 합니다.

---

## STEP 1. 러닝 기록 저장

### 요청

```http
POST http://localhost:8080/activities/sync
```

### Headers

```text
Content-Type: application/json
```

### Body

Postman에서 `Body > raw > JSON`을 선택합니다.

아래 예시의 `2026-08-14`는 실제 테스트 실행 날짜로 변경합니다.

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

### 주요 필드

| 필드 | 설명 |
|---|---|
| runningDuration | 러닝 시간(초) |
| recordDate | 러닝 기록 날짜 |
| distanceM | 러닝 거리(m) |
| avgPace | 평균 페이스(초/km) |
| avgHeartRate | 평균 심박수 |
| maxHeartRate | 최대 심박수 |
| calories | 소모 칼로리 |
| cadence | 케이던스 |
| startTime | 러닝 시작 시간 |
| endTime | 러닝 종료 시간 |
| lat | 러닝 위치 위도 |
| lon | 러닝 위치 경도 |

리포트 생성 시 `lat`, `lon`, `startTime`을 기준으로 운동 당시 날씨를 조회하므로 반드시 입력해야 합니다.

---

## STEP 2. 오늘의 수면 컨디션 저장

### 요청

```http
POST http://localhost:8080/conditions
```

### Headers

```text
Content-Type: application/json
```

### Body

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

### 가능한 bodyCondition 값

```text
GOOD
FAIR
POOR
```

### 가능한 sleepQuality 값

```text
GOOD
FAIR
POOR
```

### painAreas 예시

```text
B_KNEE_L
F_THIGH_L
B_CALF_R
F_SHOULDER_L
```

`conditions` API는 현재 사용자 ID `1`과 오늘 날짜를 기준으로 컨디션을 저장합니다.

---

## STEP 3. AFTER_RUN 피부 스캔

### 요청

```http
POST http://localhost:8080/wellness/skin/scan?userId=1&type=AFTER_RUN
```

### Postman Body 설정

1. `Body` 선택
2. `form-data` 선택
3. Key에 `file` 입력
4. Key 타입을 `File`로 변경
5. 이미지 파일 선택
6. `Content-Type` 헤더를 직접 추가하지 않음

### Form-data 예시

| Key | Type | Value |
|---|---|---|
| file | File | 러닝 후 피부 이미지 |

주의할 점:

- Key 이름은 반드시 `file`이어야 합니다.
- 타입은 `Text`가 아니라 `File`이어야 합니다.
- Body 타입은 `raw`가 아니라 `form-data`여야 합니다.
- Python 서버의 필드명이 `image`여도 Spring API에서는 `file`을 사용합니다.
- Spring 서버가 내부적으로 `file`을 Python 서버의 `image` 필드로 변환합니다.

정상적으로 저장되면 `skin_record`에 `AFTER_RUN` 데이터가 저장됩니다.

---

## STEP 4. 웰니스 러닝 리포트 생성

### 요청

```http
POST http://localhost:8080/api/wellness/reports
```

### Headers

```text
Content-Type: application/json
```

### Body

Postman에서 `Body > raw > JSON`을 선택합니다.

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

`recordDate`는 STEP 1의 러닝 기록 날짜와 STEP 3의 피부 스캔 날짜와 같아야 합니다.

`activityRecordId`는 선택 필드입니다.

특정 러닝 기록 ID를 직접 지정하려면 다음처럼 입력합니다.

```json
{
  "recordDate": "2026-08-14",
  "activityRecordId": 1,
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

`activityRecordId`를 생략하면 `recordDate`와 사용자 ID `1`을 기준으로 러닝 기록을 조회합니다.

---

## 7. 설문 Enum 값

### feeling

| 값 | 화면 의미 |
|---|---|
| GREAT | 너무 좋았어요! |
| NORMAL | 보통이에요 |
| EXHAUSTED | 너무 힘들었어요. |

### energy

| 값 | 화면 의미 |
|---|---|
| DEPLETED | 완전 방전이에요 |
| TIRED | 적당히 지쳤어요 |
| ENERGETIC | 에너지가 넘쳐요! |

### sweat

| 값 | 화면 의미 |
|---|---|
| LOW | 쾌적해요(거의 안 흘림) |
| MODERATE | 적당히 났어요 |
| HIGH | 흠뻑 젖었어요 |

---

## 8. 정상 응답 예시

HTTP Status:

```text
201 Created
```

Response Body:

```json
{
  "intensity": {
    "score": 7,
    "level": "MODERATE",
    "comment": "평균 심박수 145bpm과 30분 러닝 시간을 고려하면 오늘은 중간 강도의 운동이었어요."
  },
  "hydration": {
    "title": "운동 후 수분 보충",
    "solution": "추정 수분 손실량을 고려해 물과 전해질을 충분히 보충해 주세요."
  },
  "skin": {
    "title": "운동 후 피부 진정 관리",
    "solution": "운동 후 땀과 열감을 씻어내고 수분 크림으로 피부를 진정시켜 주세요."
  },
  "stretching": {
    "title": "무릎과 허벅지 스트레칭",
    "solution": "무릎과 허벅지 주변을 중심으로 통증이 없는 범위에서 천천히 스트레칭해 주세요."
  },
  "recoveryVideo": {
    "title": "오금, 허벅지 앞 스트레칭 영상",
    "videoUrl": "https://www.youtube.com/watch?v=실제검색된영상ID",
    "sourceTitle": "실제 YouTube 영상 제목",
    "durationSeconds": 175,
    "summaryBasis": "VIDEO_METADATA_NOT_TRANSCRIPT",
    "steps": [
      {
        "label": "STEP 1",
        "description": "오금 주변을 천천히 늘리고 20초간 유지해 주세요."
      },
      {
        "label": "STEP 2",
        "description": "허벅지 앞쪽을 당기되 무릎에 통증이 생기면 즉시 중단해 주세요."
      }
    ]
  }
}
```

---

## 9. DB 저장 결과 확인

리포트 생성 후 아래 테이블에 데이터가 저장됩니다.

```sql
SELECT *
FROM activity_record
WHERE user_id = 1
ORDER BY record_date DESC;
```

```sql
SELECT *
FROM skin_record
WHERE user_id = 1
ORDER BY measured_date DESC;
```

```sql
SELECT *
FROM today_condition
WHERE user_id = 1
ORDER BY condition_date DESC;
```

```sql
SELECT *
FROM body_issue
WHERE user_id = 1;
```

```sql
SELECT *
FROM wellness_report
WHERE user_id = 1
ORDER BY report_id DESC;
```

현재 리포트 생성 API를 여러 번 호출하면 `wellness_report` 데이터가 중복 저장될 수 있습니다.

---

## 10. 오류 해결

### Required part 'file' is not present

피부 스캔 API에서 발생하는 오류입니다.

확인 사항:

- Body가 `form-data`인지 확인
- Key 이름이 `file`인지 확인
- Key 타입이 `File`인지 확인
- 이미지 파일이 실제로 선택되어 있는지 확인

---

### Content-Type is not supported

리포트 API에서 발생하는 오류입니다.

리포트 API는 JSON 요청이므로 다음과 같이 설정합니다.

```text
Body > raw > JSON
Content-Type: application/json
```

피부 스캔 API는 JSON이 아니라 `form-data`를 사용합니다.

---

### 필수 파라미터가 없습니다: memberId

현재 코드 기준 피부 스캔 API의 파라미터명은 `memberId`가 아니라 `userId`입니다.

```text
/wellness/skin/scan?userId=1&type=AFTER_RUN
```

리포트 API는 사용자 ID를 별도로 받지 않으며 내부적으로 사용자 ID `1`을 사용합니다.

---

### YouTube API Key가 설정되지 않았습니다

다음 환경변수를 등록한 후 백엔드 서버를 재시작합니다.

```powershell
$env:YOUTUBE_API_KEY="YouTube_Data_API_Key"
```

---

### 당일 AFTER_RUN 피부 기록을 찾을 수 없음

리포트 생성 전에 반드시 아래 API를 호출해야 합니다.

```http
POST http://localhost:8080/wellness/skin/scan?userId=1&type=AFTER_RUN
```

또한 피부 스캔 날짜와 러닝 기록의 `recordDate`가 같아야 합니다.

---

### 당일 수면 컨디션을 찾을 수 없음

리포트 생성 전에 다음 API를 호출해야 합니다.

```http
POST http://localhost:8080/conditions
```

---

### 운동 당시 날씨 조회 실패

다음 값을 확인합니다.

- `openweather.api.key` 설정 여부
- 러닝 기록의 `lat`, `lon` 값
- 러닝 기록의 `startTime` 값
- 인터넷 연결 상태

---

### Python 피부 분석 서버 연결 실패

다음 서버가 실행 중인지 확인합니다.

```text
http://localhost:8000/scan
```

실행 명령:

```powershell
cd C:\runcovery\skin-scan-main

python -m uvicorn src.app.main:app --host 127.0.0.1 --port 8000
```

---

## 11. API 문서

Springdoc Swagger:

```text
http://localhost:8080/swagger-ui/index.html
```

Python 피부 분석 서버 Swagger:

```text
http://localhost:8000/docs
```
```
