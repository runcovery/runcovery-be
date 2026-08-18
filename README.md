<div align="center">

# 🏃 RunCovery

### 러닝 기록을 넘어, 회복까지 관리하는 AI 웰니스 러닝 코치

프로필·목표·러닝 기록·날씨·컨디션·피부 상태를 종합해  
사용자에게 필요한 운동 계획과 맞춤형 사후 관리 처방전을 제공합니다.

<br/>

![Java](https://img.shields.io/badge/Java-21-007396?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![MariaDB](https://img.shields.io/badge/MariaDB-10.6-003545?style=flat-square&logo=mariadb&logoColor=white)
![OpenAI](https://img.shields.io/badge/OpenAI-API-412991?style=flat-square&logo=openai&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-CI%2FCD-2088FF?style=flat-square&logo=githubactions&logoColor=white)

</div>

---

## 📌 프로젝트 소개

기존 러닝 서비스는 거리, 시간, 페이스, 칼로리와 같은 러닝 기록에 집중합니다.  
하지만 운동 후 수분 보충, 피부 관리, 통증 부위 스트레칭과 같은 회복 관리는 대부분 사용자의 몫으로 남아 있습니다.

사용자는 관리가 필요하다는 사실은 알지만 다음과 같은 내용을 정확히 판단하기 어렵습니다.

- 오늘 운동 강도가 내 컨디션에 적절했는지
- 땀을 흘린 만큼 수분과 영양을 어떻게 보충해야 하는지
- 운동 후 피부 상태에 어떤 관리가 필요한지
- 통증이 있는 부위를 어떻게 스트레칭해야 하는지
- 수면과 몸 상태를 고려했을 때 휴식이 필요한지

**RunCovery**는 러닝 전 계획부터 운동 후 회복까지 하나의 흐름으로 연결합니다.

러닝 기록, 날씨, 수면 상태, 몸 상태, 피부 스캔, 운동 후 설문을 AI가 종합적으로 분석해 사용자에게 맞춤형 웰니스 리포트와 처방전을 제공합니다.

---

## ✨ 핵심 가치

| 기존 러닝 서비스 | RunCovery |
|---|---|
| 거리·시간·페이스 기록 | 러닝 기록과 컨디션을 함께 분석 |
| 운동 수행 결과 제공 | 운동 강도와 회복 상태 분석 |
| 운동 후 관리는 사용자 판단 | AI 기반 수분·피부·스트레칭 처방 |
| 획일적인 운동 정보 | 사용자 상태에 따른 개인화 피드백 |
| 단순 통계 중심 | 러닝 전 계획부터 사후 관리까지 연결 |

---

## 🔄 사용자 흐름

```text
프로필 및 목표 입력
        ↓
오늘의 컨디션 체크
        ↓
AI 러닝 목표 및 미션 생성
        ↓
러닝 데이터 동기화
        ↓
운동 후 AFTER_RUN 피부 스캔
        ↓
운동 만족도·에너지·땀·통증 부위 설문
        ↓
러닝·날씨·컨디션·피부 데이터 통합 분석
        ↓
AI 웰니스 리포트 및 처방전 생성
        ↓
피부 관리 및 스트레칭 수행
        ↓
AFTER_CARE 피부 스캔
        ↓
전날 및 관리 전후 피부 상태 비교
```

---

## 🚀 주요 기능

### 🎯 목표 설정

- 사용자 프로필과 운동 경험을 기반으로 목표 추천
- 미래 목표 장면 AI 생성
- 최근 러닝 기록을 반영한 주간 목표 생성
- 주간 목표 달성 현황 조회 및 갱신

### 📅 일일 루틴

- 수면 상태와 몸 상태를 포함한 오늘의 컨디션 기록
- 날씨·컨디션·주간 목표 기반 일일 미션 생성
- 러닝 데이터 동기화 시 미션 자동 완료
- 통증 부위 기록 및 조회

### 🏃 러닝 활동

- 러닝 시간, 거리, 평균 페이스, 케이던스 저장
- 평균 심박수 및 최대 심박수 기록
- 러닝 시작·종료 시간 및 위치 정보 관리
- 러닝 당시 날씨 조회
- 리포트 작성 전 러닝·날씨 데이터 미리보기

### 🧴 피부 스캔

- 운동 직후 `AFTER_RUN` 피부 스캔
- 관리 후 `AFTER_CARE` 피부 스캔
- 날짜별 피부 기록 조회
- 홍조, 유분, 피부결, 모공, 잡티, 보습, 색소침착 분석
- 전날 대비 `AFTER_CARE` 피부 점수 비교
- Python 피부 분석 서버와 이미지 기반 분석 연동

### 📊 AI 웰니스 리포트

다음 데이터를 통합해 맞춤형 리포트를 생성합니다.

- 러닝 거리, 시간, 페이스, 케이던스
- 평균 심박수 및 최대 심박수
- 러닝 당시 온도, 습도, 자외선 정보
- 수면 상태 및 몸 상태
- `AFTER_RUN` 피부 점수
- 운동 만족도, 에너지, 땀 설문
- 사용자가 선택한 통증 부위

러닝 강도는 1~10점으로 산출하고 다음과 같이 분류합니다.

| 점수 | 강도 |
|---:|---|
| 1~3 | LOW |
| 4~7 | MODERATE |
| 8~10 | HIGH |

### 💊 맞춤형 웰니스 처방전

- 수분 및 영양 보충 솔루션
- 운동 후 피부 관리 솔루션
- 통증 부위 기반 스트레칭 솔루션
- YouTube Data API를 활용한 회복 영상 추천
- 상체와 하체 통증 그룹에 따른 영상 추천
- 피부·스트레칭 처방전 완료 상태 관리
- 카테고리별 처방전 목록 및 상세 조회

### 🏠 홈 및 마이페이지

- 주간 활동 통계
- 목표 달성률 조회
- 사후 관리 달성률 조회
- 월간 피부 점수 그래프
- 사용자 데이터 기반 AI 웰니스 피드백

---

## 🧠 AI 활용 영역

| 기능 | 활용 데이터 | 결과 |
|---|---|---|
| 목표 추천 | 프로필, 운동 경험, 목표 | 사용자 맞춤 러닝 목표 |
| 일일 미션 | 날씨, 컨디션, 주간 목표 | 오늘의 러닝 미션 |
| 컨디션 분석 | 수면 상태, 몸 상태 | 당일 컨디션 피드백 |
| 러닝 강도 분석 | 심박수, 페이스, 시간, 날씨 | 1~10점 강도 및 코멘트 |
| 수분·영양 처방 | 날씨, 땀, 칼로리, 운동 강도 | 보충 솔루션 |
| 피부 처방 | 피부 점수, 날씨, 땀 | 피부 관리 솔루션 |
| 스트레칭 처방 | 통증 부위, 운동 강도 | 회복 방법 및 영상 추천 |

> AI가 생성한 결과는 의료 진단이나 전문적인 치료를 대체하지 않습니다.

---

## 🏗 시스템 아키텍처

![RunCovery 시스템 아키텍처](<img width="1741" height="1039" alt="Image" src="https://github.com/user-attachments/assets/184f36db-80b7-410e-a86a-81896d1034d7" />)

```text
React Native 앱
        │
        │ HTTP :80
        │ X-Public-Id
        ▼
Nginx :80
        │ Reverse Proxy
        ▼
Spring Boot :8080
   ├── MariaDB :3306
   ├── FastAPI 피부 분석 :8000
   │     └── Python · OpenCV · MediaPipe
   ├── OpenAI API
   ├── OpenWeather API
   └── YouTube Data API
```

### 서버 구성

가비아 클라우드 서버 한 대에서 다음 서비스를 실행합니다.

| 서비스 | 포트 | 역할 |
|---|---:|---|
| Nginx | 80 | 외부 API 요청 수신 및 Reverse Proxy |
| Spring Boot | 8080 | RunCovery 백엔드 API |
| MariaDB | 3306 | 서비스 데이터 저장 |
| FastAPI | 8000 | 이미지 기반 피부 상태 분석 |

- 운영체제: Ubuntu 22.04
- 서버 사양: 2vCore / 4GB
- Spring Boot와 FastAPI는 systemd 서비스로 관리
- MariaDB와 FastAPI는 동일 서버 내부에서 통신
- HTTPS는 추후 적용 예정

---

## 🔍 피부 스캔 처리 흐름

```text
React Native 카메라 촬영
        ↓
multipart/form-data 이미지 전송
        ↓
Nginx
        ↓
Spring Boot
        ↓
FastAPI /scan
        ↓
OpenCV · MediaPipe 피부 분석
        ↓
condition_scores 반환
        ↓
Spring Boot에서 피부 기록 저장
        ↓
React Native 앱에 결과 반환
```

피부 분석 서버는 다음 점수를 반환합니다.

```json
{
  "redness": 61,
  "oiliness": 76,
  "texture": 89,
  "pores": 100,
  "blemishes": 79,
  "hydration": 64,
  "pigment": 85
}
```

피부 분석 서버는 별도 저장소에서 관리합니다.

- [runcovery-skin-analysis](https://github.com/runcovery/runcovery-skin-analysis)

---

## 🛠 기술 스택

### Backend

| 기술 | 용도 |
|---|---|
| Java 21 | 백엔드 개발 언어 |
| Spring Boot 4.1.0 | 백엔드 애플리케이션 |
| Spring MVC | REST API |
| Spring Data JPA | 데이터 접근 |
| Spring Validation | 요청값 검증 |
| Spring AI | OpenAI 연동 |
| Lombok | 반복 코드 최소화 |
| Gradle | 빌드 및 의존성 관리 |

### Database

| 기술 | 용도 |
|---|---|
| MariaDB | 서비스 데이터 저장 |
| utf8mb4 | 한글·이모지 데이터 저장 |

### AI 및 외부 API

| 기술 | 용도 |
|---|---|
| OpenAI API | 목표·미션·컨디션·웰니스 리포트 생성 |
| OpenWeather API | 날씨 및 과거 러닝 당시 기상 정보 |
| YouTube Data API | 통증 부위별 회복 영상 검색 |

### 피부 분석

| 기술 | 용도 |
|---|---|
| FastAPI | 피부 분석 REST API |
| Python | 피부 분석 서버 |
| OpenCV | 이미지 전처리 및 분석 |
| MediaPipe | 얼굴 영역 감지 |

### Infrastructure

| 기술 | 용도 |
|---|---|
| 가비아 클라우드 | 운영 서버 |
| Ubuntu 22.04 | 서버 운영체제 |
| Nginx | Reverse Proxy |
| systemd | Spring Boot·FastAPI 프로세스 관리 |
| GitHub Actions | CI/CD |

### API 문서

| 기술 | 용도 |
|---|---|
| springdoc-openapi | OpenAPI 문서 생성 |
| Swagger UI | API 테스트 및 명세 확인 |

---

## 🔐 사용자 식별

RunCovery는 별도의 로그인 기능 대신 사용자별 UUID인 `publicId`를 사용합니다.

클라이언트는 API 요청 시 다음 헤더를 전달합니다.

```http
X-Public-Id: {사용자 UUID}
```

Spring Boot는 전달받은 UUID를 내부 사용자 PK로 변환하며, 모든 조회·저장·수정은 해당 사용자 소유 데이터만 대상으로 처리합니다.

> README, 로그, 테스트 문서에 실제 운영 UUID나 사용자 개인정보를 작성하지 않습니다.

---

## 📖 API 문서

서버 실행 후 Swagger UI에서 API를 확인할 수 있습니다.

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

배포 환경에서는 Nginx를 통해 접근합니다.

```text
http://{배포 서버 주소}/swagger-ui/index.html
```

### API 요청 공통 헤더

```http
X-Public-Id: {사용자 UUID}
```

### 피부 스캔 요청 형식

```text
Content-Type: multipart/form-data
파일 키: file
타입: AFTER_RUN 또는 AFTER_CARE
```

---

## ⚙️ 로컬 실행 방법

### 1. 요구 환경

- Java 21
- MariaDB
- Git
- Python 피부 분석 서버

### 2. 저장소 복제

```bash
git clone https://github.com/runcovery/runcovery-be.git
cd runcovery-be
```

### 3. 환경변수 설정

다음 값은 Git에 올리지 않고 로컬 또는 배포 환경에서 관리합니다.

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD

SPRING_AI_OPENAI_API_KEY
OPENWEATHER_API_KEY
YOUTUBE_API_KEY

WELLNESS_SKIN_SCAN_URL
WELLNESS_SKIN_SCAN_TIMEOUT
```

피부 분석 서버의 로컬 주소 예시:

```text
WELLNESS_SKIN_SCAN_URL=http://127.0.0.1:8000/scan
```

> API 키, DB 비밀번호, 사용자 UUID는 절대 Git에 commit하지 않습니다.

### 4. 테스트 및 빌드

Windows:

```powershell
.\gradlew.bat clean test
.\gradlew.bat bootJar
```

Linux/macOS:

```bash
./gradlew clean test
./gradlew bootJar
```

### 5. 애플리케이션 실행

Windows:

```powershell
.\gradlew.bat bootRun
```

Linux/macOS:

```bash
./gradlew bootRun
```

---

## 🔁 CI/CD

RunCovery 백엔드는 GitHub Actions 기반 CI/CD를 사용합니다.

### CI

```text
develop push 또는 Pull Request
        ↓
GitHub Actions
        ↓
MariaDB 테스트 환경 구성
        ↓
Gradle clean test
        ↓
Spring Boot JAR 빌드
```

### CD

```text
main 브랜치 push
        ↓
GitHub Actions
        ↓
테스트 및 빌드
        ↓
SSH로 가비아 서버 접속
        ↓
최신 코드 또는 JAR 반영
        ↓
systemd 서비스 재시작
```

### 브랜치 전략

| 브랜치 | 용도 |
|---|---|
| `main` | 운영 배포 브랜치 |
| `develop` | 기능 통합 및 CI 브랜치 |
| `feature/*` | 기능 개발 브랜치 |

기능 개발 흐름:

```text
feature/*
→ develop Pull Request
→ 코드 리뷰 및 CI 확인
→ develop 병합
→ main 병합
→ 운영 서버 자동 배포
```

---

## 🗂 프로젝트 구조

```text
src/main/java/com/likelion14/runcovery
├── common          # 공통 응답, 예외 처리, 외부 API 공통 기능
├── condition       # 오늘의 컨디션
├── activity        # 러닝 활동 기록
├── wellness        # 피부 스캔, 리포트, 처방전
├── user            # 사용자 프로필 및 UUID 식별
└── ...             # 목표, 미션, 홈, 마이페이지 도메인

src/main/resources
├── application.properties
└── data.sql
```

`wellness` 도메인은 다음과 같이 구성됩니다.

```text
wellness
├── controller      # 피부·리포트·처방전 API
├── service         # 비즈니스 로직 및 외부 API 연동
├── repository      # JPA Repository
├── entity          # 피부 기록·리포트·처방전 Entity
├── dto             # Request·Response DTO
├── client          # 날씨·피부 분석·YouTube 클라이언트
├── exception       # 웰니스 도메인 예외
└── config          # WebClient 및 관련 설정
```

---

## 🛡 보안 및 개인정보

- 피부 이미지는 분석 목적에 필요한 범위에서만 처리
- 이미지 원본의 장기 저장 최소화
- 실제 사용자 UUID를 README나 로그에 노출하지 않음
- API 키와 DB 비밀번호는 환경변수로 관리
- MariaDB와 FastAPI 포트는 외부 노출 제한 권장
- 운영 환경에서는 HTTPS 적용 권장
- 피부 분석 결과는 의료 진단이 아닌 웰니스 참고 정보로 제공

---

## 📈 확장 계획

- HTTPS 및 도메인 적용
- DB와 피부 분석 서버 분리
- 이미지 저장 정책 및 보관 기간 관리
- 모니터링 및 장애 알림 도입
- 스트레칭 영상 추천 정확도 개선
- 러닝·컨디션·피부 데이터를 활용한 개인화 고도화
- AAC 수분·피부·스트레칭 제품 및 프로그램 연계
- 오프라인 러닝 스테이션 피부 분석 기능 제공

---

## 💼 비즈니스 확장 방향

### AAC 제품·프로그램 연계

웰니스 처방전에서 사용자에게 필요한 수분, 피부, 스트레칭 관련 제품과 프로그램을 추천하고 실제 구매 또는 서비스 이용으로 연결합니다.

### 오프라인 러닝 스테이션

러닝크루 거점, 팝업 스토어, 행사장 등에 피부 분석 기술을 제공해 러닝 후 개인 맞춤 웰니스 경험을 제공합니다.

---

## 👥 Team

| 역할 | 담당 |
|---|---|
| Backend | RunCovery Backend Team |
| Frontend | RunCovery Frontend Team |
| AI·피부 분석 | RunCovery AI Team |
| Design | RunCovery Design Team |

---

<div align="center">

### Run Better. Recover Smarter.

러닝이 끝난 순간부터, 진짜 회복이 시작됩니다.

</div>
