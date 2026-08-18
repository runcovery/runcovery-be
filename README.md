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

## 🏗 시스템 아키텍처

![RunCovery 시스템 아키텍처]
<img width="1741" height="1039" alt="runcovery_system_architecture" src="https://github.com/user-attachments/assets/e17143ad-ea04-43e3-a0a2-797b24bb2d4a" />



- [runcovery-skin-analysis](https://github.com/runcovery/runcovery-skin-analysis)



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
