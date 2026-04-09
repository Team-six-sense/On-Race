# k6 부하 테스트 가이드

On-Race 마라톤 이벤트 티켓팅 플랫폼 부하 테스트 문서입니다.
**데이터 셋업부터 Redis 초기화, 대기열 활성화까지 모두 자동화**되어 있으므로, k6 명령어 한 줄로 테스트를 실행할 수 있습니다.

---

## 목차

1. [테스트 시나리오 개요](#1-테스트-시나리오-개요)
2. [아키텍처 & 테스트 흐름](#2-아키텍처--테스트-흐름)
3. [팀별 역할 & 체크리스트](#3-팀별-역할--체크리스트)
4. [사전 준비](#4-사전-준비)
5. [실행 방법](#5-실행-방법)
6. [환경변수 레퍼런스](#6-환경변수-레퍼런스)
7. [시나리오별 실행 예시](#7-시나리오별-실행-예시)
8. [인프라 환경별 설정 가이드](#8-인프라-환경별-설정-가이드)
9. [모니터링 (Prometheus + Grafana)](#9-모니터링-prometheus--grafana)
10. [결과 검증 & 리포트 해석](#10-결과-검증--리포트-해석)
11. [커스텀 메트릭 레퍼런스](#11-커스텀-메트릭-레퍼런스)
12. [트러블슈팅](#12-트러블슈팅)

---

## 1. 테스트 시나리오 개요

| # | 시나리오 | 스크립트 | 설명 |
|---|---------|---------|------|
| 0 | 스모크 테스트 | `00-smoke-test.js` | 환경 검증 (5 VU, 전체 이벤트 생성) |
| 1 | 응모신청 | `01-lottery.js` | 로그인 → 응모 (재고 무제한, 전원 성공) |
| 2 | 선착순 대기열X | `02-first-come-no-queue.js` | 로그인 → 선착순 → 결제/이탈 → Wave2 재선점 |
| 3 | 선착순 대기열O | `03-first-come-with-queue.js` | 로그인 → 대기열 → 선착순 → 결제/이탈 → Wave2 재선점 |
| 4 | 용량(Breakpoint) | `04-breakpoint.js` | 계단형 VU 증가 → 한계점 자동 탐지 (모든 플로우 지원) |

> **시나리오별 단일 이벤트 생성**: 각 시나리오는 자신에 해당하는 이벤트 **1개만** 생성합니다.
> 예: `01-lottery.js`는 응모 이벤트 1개만, `02-first-come-no-queue.js`는 선착순(대기열X) 이벤트 1개만 생성합니다.

---

## 2. 아키텍처 & 테스트 흐름

### 2.1 자동 셋업 (Setup API)

```
k6 setup() 단계
  ↓
POST /main/internal/load-test/setup  (scenarioType 지정)
  ↓
┌─────────────────────────────────────────┐
│ 1. 유저 30,000명 등록 (멱등)             │
│ 2. 이전 테스트 데이터 정리               │
│ 3. 해당 시나리오 이벤트 1개 생성          │
│    → 코스 3개 × 페이스 5개 = 15 페이스    │
│    → 인기 페이스 재고 집중 배분            │
│    → 판매 정보 생성                       │
│ 4. 사전정보 저장 (preSaveRatio 지정 시)   │
│ 5. Redis flushDB + 재고 초기화           │
│ 6. 대기열 활성화 (WITH_QUEUE만)           │
└─────────────────────────────────────────┘
  ↓
일괄 로그인 (http.batch, 50명/배치)
  ↓
VU 실행 시작
```

- **`local` 프로필에서만 활성화**되는 내부 API입니다.
- SQL 파일 실행이나 Redis 수동 초기화가 **필요 없습니다**.

### 2.2 2웨이브 구조 (시나리오 2, 3)

선착순 시나리오는 **결제 이탈 → 재고 재순환**을 검증하는 2웨이브 구조입니다.

```
Wave 1 (VU 1 ~ VU_COUNT)
  ├─ 선점 성공 → 70% 결제확정 / 30% 결제이탈 (TTL 15초 후 재고 반환)
  └─ 매진 → 종료

Wave 2 (VU VU_COUNT+1 ~ 총VU)
  ├─ TTL 만료 대기 (18초)
  └─ 이탈 재고 재선점 → 전원 결제확정
```

- Wave 2 인원은 `EXTRA_VU_COUNT` 또는 `VU_COUNT × PAYMENT_DROPOUT_RATIO`로 자동 계산
- 총 VU = VU_COUNT + EXTRA_VU_COUNT

### 2.3 페이스 배분 (70/30 인터리빙)

```
10-VU 블록 단위 인터리빙:
  블록 내 첫 7명 → 인기 페이스 (라운드로빈)
  블록 내 나머지 3명 → 일반 페이스 (라운드로빈)
```

> Wave2도 동일한 비율로 인기 페이스에 배정되어 HOT 페이스 기아 현상을 방지합니다.

### 2.4 인기 페이스 재고 배분

```
총 재고 = totalStock (직접 지정 or VU_COUNT ÷ 경쟁률)

인기 페이스 재고 = 총재고 × HOT_STOCK_RATIO ÷ HOT_PACE_COUNT
나머지 재고      = (총재고 - 인기 총재고) ÷ (15 - HOT_PACE_COUNT)
```

### 2.5 용량(Breakpoint) 테스트 구조 (시나리오 4)

```
계단형 VU 증가 (Staircase):
  500 → 1000 → 1500 → 2000 → ... → BP_MAX_VUS
  각 단계: ramp 10초 → hold 60초

자동 중단 조건:
  - error_rate > 5% (30초 유예)
  - apply_latency p95 > 5000ms (30초 유예)

VU 토큰 순환:
  30,000명 로그인 풀에서 소수 배수(7919)로 순환
  → 매 반복마다 다른 유저 사용 → 실제 INSERT 쓰기 부하 유지
```

---

## 3. 팀별 역할 & 체크리스트

### 3.1 개발팀 (Backend)

| 단계 | 작업 | 비고 |
|------|------|------|
| **사전** | Gateway rate limiter 주석 처리 | `RequestRateLimiter` 필터 |
| **사전** | Gateway queue/entry route 주석 해제 | `BotDetectionFilter` 주석 유지 |
| **사전** | `MAIN_PROFILES_ACTIVE=local` 확인 | Setup API는 local에서만 활성화 |
| **실행** | 로컬 Docker 환경에서 시나리오 0→1→2→3 순서 실행 | |
| **실행** | 시나리오 4(Breakpoint)로 단일 인스턴스 한계점 탐지 | |
| **검증** | 오버셀링 0건, DB-Redis 일치 확인 | teardown 자동 리포트 |
| **검증** | 동시성 이슈 발견 시 코드 수정 후 재테스트 | |
| **인수** | Breakpoint 결과 (안정 VU, 안정 RPS) 인프라팀에 전달 | Pod 스케일링 기준값 |

### 3.2 인프라팀 (DevOps / SRE)

| 단계 | 작업 | 비고 |
|------|------|------|
| **사전** | EKS/ECS 클러스터 + RDS + ElastiCache 프로비저닝 | t3.micro부터 시작 |
| **사전** | `.env` 인프라 환경용 복사 & 수정 | [섹션 8](#8-인프라-환경별-설정-가이드) 참조 |
| **사전** | Prometheus + Grafana 모니터링 스택 배포 | |
| **1차** | t3.micro 단일 Pod → 시나리오 4(Breakpoint) 실행 | 기준선 측정 |
| **2차** | t3.small → t3.medium 순서로 스펙 업 테스트 | 스펙별 안정 VU 비교 |
| **3차** | 최적 스펙에서 Pod 2→4→8 스케일 아웃 테스트 | 선형 확장성 검증 |
| **4차** | KEDA HPA 설정 후 자동 스케일링 테스트 | CPU/RPS 기반 트리거 |
| **검증** | Pod별 CPU/메모리, DB 커넥션, Redis 커넥션 모니터링 | Grafana 대시보드 |
| **산출** | 스펙별 성능 비교표 + 최적 Pod 수 + HPA 설정값 문서화 | |

---

## 4. 사전 준비

### 4.1 k6 설치

```bash
# Windows
winget install grafana.k6

# macOS
brew install k6

# 확인
k6 version
```

### 4.2 Gateway 설정 변경

`gateway/src/main/resources/application.yml`에서:

1. **auth-route**: `RequestRateLimiter` 주석 처리 (IP 기반 rate limit 제거)
2. **queue-route**: 주석 해제, `BotDetectionFilter` 주석 유지
3. **entry-apply-first-come-route**: 주석 해제, `BotDetectionFilter` 주석 유지

### 4.3 서비스 기동

```bash
# Docker 전체 빌드 & 실행 (서비스 + 모니터링 스택)
docker compose build --no-cache && docker compose up -d

# 상태 확인
docker compose ps
```

> **Docker 빌드 시 `--no-cache` 필수**: 소스 코드 변경이 Gradle 캐시 레이어에 가려질 수 있습니다.

---

## 5. 실행 방법

### 기본 실행

```bash
k6 run backend/k6/scenarios/<시나리오파일>.js
```

### 환경변수 지정

```bash
k6 run -e VU_COUNT=4500 -e RAMP_UP_SEC=60 backend/k6/scenarios/01-lottery.js
```

### 실행 순서 권장

```
1. 00-smoke-test.js         ← 환경 검증 (5 VU)
2. 01-lottery.js             ← 소규모 50~100 VU 검증
3. 01-lottery.js             ← 목표 규모 (4,500 VU)
4. 02-first-come-no-queue.js ← 선착순 대기열X
5. 03-first-come-with-queue.js ← 선착순 대기열O
6. 04-breakpoint.js          ← 한계점 탐지 (선택)
```

> **시나리오 간 별도 초기화 불필요**: 각 시나리오의 setup()이 이전 데이터를 자동 정리합니다.

---

## 6. 환경변수 레퍼런스

모든 값은 `-e` 플래그로 오버라이드 가능합니다.

### 서버 설정

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `BASE_URL` | `http://localhost:30000` | Gateway 주소 |
| `USER_PASSWORD` | `Test1234!@` | 테스트 유저 공통 비밀번호 |

### VU & 부하 패턴

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `VU_COUNT` | `100` | Wave 1 동시 사용자 수 (최대 30,000) |
| `RAMP_UP_SEC` | `10` | 램프업 시간(초) |
| `HOLD_SEC` | `120` | 유지 시간(초) |
| `RAMP_DOWN_SEC` | `5` | 램프다운 시간(초) |
| `SETUP_TIMEOUT_SEC` | `600` | setup 단계 타임아웃(초) |

### 재고 설정

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `TOTAL_STOCK` | *(자동)* | 이벤트당 총 재고. 미지정 시 `VU_COUNT ÷ COMPETITION_RATIO` |
| `COMPETITION_RATIO` | `3` | 경쟁률 (3:1 → VU 4,500 시 재고 1,500) |
| `HOT_PACE_COUNT` | `2` | 인기 페이스 수 (1~3) |
| `HOT_STOCK_RATIO` | `0.4` | 인기 페이스에 배분할 재고 비율 (40%) |

### 인기 페이스 고정 지정

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `HOT_COURSE_INDEX` | *(랜덤)* | 인기 코스 인덱스 (0=풀코스, 1=하프코스, 2=10km) |
| `HOT_PACE_INDEX` | *(랜덤)* | 인기 페이스 인덱스 (0=3분, 1=4분, 2=5분, 3=6분, 4=7분) |

> 두 값을 모두 지정하면 해당 코스-페이스 1개를 HOT으로 **고정**합니다.
> 예: `-e HOT_COURSE_INDEX=2 -e HOT_PACE_INDEX=2` → 10km 코스 / 5분 페이스 고정

### 사전정보 저장

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `PRE_SAVE_RATIO` | *(미지정=건너뜀)* | 사전정보 저장 비율. `0.7` = 70% 유저가 사전정보 저장 |

### 결제 이탈 & 2웨이브 (시나리오 2, 3)

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `PAYMENT_DROPOUT_RATIO` | `0.3` | Wave 1 결제 이탈률 (30%) |
| `EXTRA_VU_COUNT` | *(자동)* | Wave 2 추가 인원. 미지정 시 `VU_COUNT × PAYMENT_DROPOUT_RATIO` |
| `RETRY_MAX_ROUNDS` | `5` | 매진 시 최대 재시도 횟수 |
| `RETRY_WAIT_SEC` | `18` | Wave2 TTL 만료 대기 (TTL 15초 + 여유 3초) |

### 대기열 설정 (시나리오 3)

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `QUEUE_POLL_SEC` | `2` | 대기열 폴링 간격(초) |
| `QUEUE_MAX_POLL` | `300` | 최대 폴링 횟수 |

### 용량(Breakpoint) 테스트 설정 (시나리오 4)

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `BP_SCENARIO_FLOW` | `LOTTERY` | 테스트 플로우 (`LOTTERY` / `FIRST_COME` / `FIRST_COME_QUEUE`) |
| `BP_START_VUS` | `500` | 시작 VU 수 |
| `BP_STEP_VUS` | `500` | 단계별 VU 증분 |
| `BP_MAX_VUS` | `5000` | 최대 VU 수 |
| `BP_STEP_DURATION_SEC` | `60` | 단계 유지 시간(초) |
| `BP_RAMP_SEC` | `10` | 단계 간 ramp 시간(초) |
| `BP_ERROR_THRESHOLD` | `0.05` | 에러율 중단 임계값 (5%) |
| `BP_P95_THRESHOLD_MS` | `5000` | p95 응답시간 중단 임계값 (ms) |
| `BP_LOGIN_POOL` | `30000` | 로그인 유저 풀 크기 |

### 로그인 & 기타

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `LOGIN_TIMEOUT` | `120s` | 로그인 HTTP 타임아웃 |
| `LOGIN_BATCH_SIZE` | `50` | 배치 로그인 단위 |
| `HOT_PACE_RATIO` | `0.7` | VU의 70%가 인기 페이스 대상 |

---

## 7. 시나리오별 실행 예시

### 7.1 스모크 테스트

```bash
k6 run backend/k6/scenarios/00-smoke-test.js
```

### 7.2 시나리오 1: 응모신청 (01-lottery)

> 재고 제한 없음. 전원 응모 성공이 기대됩니다.

```bash
# 소규모 검증 (100명)
k6 run -e VU_COUNT=100 backend/k6/scenarios/01-lottery.js

# 기획 목표 (4,500명, 사전저장 70%)
k6 run -e VU_COUNT=4500 -e RAMP_UP_SEC=60 -e PRE_SAVE_RATIO=0.7 \
  backend/k6/scenarios/01-lottery.js

# 대규모 (12,000명)
k6 run -e VU_COUNT=12000 -e RAMP_UP_SEC=120 -e HOLD_SEC=300 -e PRE_SAVE_RATIO=0.7 \
  backend/k6/scenarios/01-lottery.js

# 최대 스케일 (30,000명)
k6 run -e VU_COUNT=30000 -e RAMP_UP_SEC=180 -e HOLD_SEC=600 -e PRE_SAVE_RATIO=0.7 \
  -e LOGIN_TIMEOUT=180s backend/k6/scenarios/01-lottery.js
```

### 7.3 시나리오 2: 선착순 대기열X (02-first-come-no-queue)

> 2웨이브 구조. 재고 소진 시 매진, 이탈 재고 재순환 검증.

```bash
# 소규모 검증 (100명)
k6 run -e VU_COUNT=100 backend/k6/scenarios/02-first-come-no-queue.js

# 기획 목표 (4,500명, 재고 1,500, 사전저장 70%)
k6 run -e VU_COUNT=4500 -e RAMP_UP_SEC=60 -e PRE_SAVE_RATIO=0.7 \
  backend/k6/scenarios/02-first-come-no-queue.js

# 대규모 (12,000명, 재고 4,000)
k6 run -e VU_COUNT=12000 -e RAMP_UP_SEC=120 -e HOLD_SEC=300 -e PRE_SAVE_RATIO=0.7 \
  backend/k6/scenarios/02-first-come-no-queue.js

# 최대 스케일 (30,000명, 재고 10,000)
k6 run -e VU_COUNT=30000 -e RAMP_UP_SEC=180 -e HOLD_SEC=600 -e PRE_SAVE_RATIO=0.7 \
  -e LOGIN_TIMEOUT=180s backend/k6/scenarios/02-first-come-no-queue.js
```

### 7.4 시나리오 3: 선착순 대기열O (03-first-come-with-queue)

> 2웨이브 구조 + 대기열 경유. PASS 수신 후 선착순 신청.

```bash
# 소규모 검증 (100명)
k6 run -e VU_COUNT=100 backend/k6/scenarios/03-first-come-with-queue.js

# 기획 목표 (4,500명, 재고 1,500, 사전저장 70%)
k6 run -e VU_COUNT=4500 -e RAMP_UP_SEC=60 -e HOLD_SEC=300 -e PRE_SAVE_RATIO=0.7 \
  backend/k6/scenarios/03-first-come-with-queue.js

# 대규모 (12,000명, 재고 4,000)
k6 run -e VU_COUNT=12000 -e RAMP_UP_SEC=120 -e HOLD_SEC=600 -e PRE_SAVE_RATIO=0.7 \
  backend/k6/scenarios/03-first-come-with-queue.js

# 최대 스케일 (30,000명, 재고 10,000)
k6 run -e VU_COUNT=30000 -e RAMP_UP_SEC=180 -e HOLD_SEC=600 -e PRE_SAVE_RATIO=0.7 \
  -e LOGIN_TIMEOUT=180s -e QUEUE_MAX_POLL=600 \
  backend/k6/scenarios/03-first-come-with-queue.js
```

### 7.5 시나리오 4: 용량(Breakpoint) 테스트 (04-breakpoint)

> 계단형 VU 증가로 서버 한계점을 자동 탐지합니다.

```bash
# 응모 플로우 (기본)
k6 run backend/k6/scenarios/04-breakpoint.js

# 선착순 대기열X (재고 충분하게 설정)
k6 run -e BP_SCENARIO_FLOW=FIRST_COME -e TOTAL_STOCK=999999 \
  backend/k6/scenarios/04-breakpoint.js

# 선착순 대기열O
k6 run -e BP_SCENARIO_FLOW=FIRST_COME_QUEUE -e TOTAL_STOCK=999999 \
  backend/k6/scenarios/04-breakpoint.js

# 세밀한 단계 (100 VU 씩 증가, 최대 3,000)
k6 run -e BP_START_VUS=100 -e BP_STEP_VUS=100 -e BP_MAX_VUS=3000 \
  -e BP_STEP_DURATION_SEC=90 backend/k6/scenarios/04-breakpoint.js
```

### 7.6 고급 옵션 조합 예시

```bash
# 인기 페이스 고정 (10km 코스 / 5분 페이스) + 재고 직접 지정
k6 run -e VU_COUNT=4500 -e TOTAL_STOCK=2000 \
  -e HOT_COURSE_INDEX=2 -e HOT_PACE_INDEX=2 -e HOT_PACE_COUNT=1 \
  -e PRE_SAVE_RATIO=0.7 backend/k6/scenarios/01-lottery.js

# 높은 경쟁률 (5:1) + 인기 페이스 3개
k6 run -e VU_COUNT=4500 -e COMPETITION_RATIO=5 \
  -e HOT_PACE_COUNT=3 -e HOT_STOCK_RATIO=0.6 \
  backend/k6/scenarios/02-first-come-no-queue.js

# 이탈률 50% + Wave 2 인원 직접 지정
k6 run -e VU_COUNT=4500 -e PAYMENT_DROPOUT_RATIO=0.5 -e EXTRA_VU_COUNT=1000 \
  backend/k6/scenarios/02-first-come-no-queue.js
```

---

## 8. 인프라 환경별 설정 가이드

### 8.1 AWS 스펙별 .env 설정 추천

인프라팀은 t3.micro부터 시작하여 점진적으로 스펙을 올리며 테스트합니다.

#### t3.micro (2 vCPU, 1GB RAM) — 기준선 측정용

```env
# HikariCP (메모리 제약 → 최소화)
MAIN_HIKARI_MAX_POOL_SIZE=10
MAIN_HIKARI_MIN_IDLE=5
AUTH_HIKARI_MAX_POOL_SIZE=5
AUTH_HIKARI_MIN_IDLE=3
MAIN_HIKARI_CONNECTION_TIMEOUT=3000

# Tomcat (2 vCPU → 스레드 제한)
MAIN_TOMCAT_MAX_THREADS=50
MAIN_TOMCAT_MIN_SPARE_THREADS=10
MAIN_TOMCAT_ACCEPT_COUNT=30
AUTH_TOMCAT_MAX_THREADS=30
AUTH_TOMCAT_MIN_SPARE_THREADS=5
AUTH_TOMCAT_ACCEPT_COUNT=15

# JVM (1GB RAM → 힙 512MB)
MAIN_JAVA_OPTS=-Xms256m -Xmx512m
AUTH_JAVA_OPTS=-Xms128m -Xmx256m
```

```bash
# Breakpoint 실행 (낮은 시작점)
k6 run -e BP_START_VUS=50 -e BP_STEP_VUS=50 -e BP_MAX_VUS=500 \
  -e BP_STEP_DURATION_SEC=60 \
  -e BASE_URL=http://<ALB_ENDPOINT> \
  backend/k6/scenarios/04-breakpoint.js
```

#### t3.small (2 vCPU, 2GB RAM)

```env
MAIN_HIKARI_MAX_POOL_SIZE=15
MAIN_HIKARI_MIN_IDLE=8
AUTH_HIKARI_MAX_POOL_SIZE=10
AUTH_HIKARI_MIN_IDLE=5
MAIN_TOMCAT_MAX_THREADS=100
MAIN_TOMCAT_MIN_SPARE_THREADS=20
AUTH_TOMCAT_MAX_THREADS=60
AUTH_TOMCAT_MIN_SPARE_THREADS=10
MAIN_JAVA_OPTS=-Xms512m -Xmx1024m
AUTH_JAVA_OPTS=-Xms256m -Xmx512m
```

```bash
k6 run -e BP_START_VUS=100 -e BP_STEP_VUS=100 -e BP_MAX_VUS=1000 \
  -e BASE_URL=http://<ALB_ENDPOINT> \
  backend/k6/scenarios/04-breakpoint.js
```

#### t3.medium (2 vCPU, 4GB RAM)

```env
MAIN_HIKARI_MAX_POOL_SIZE=20
MAIN_HIKARI_MIN_IDLE=10
AUTH_HIKARI_MAX_POOL_SIZE=15
AUTH_HIKARI_MIN_IDLE=8
MAIN_TOMCAT_MAX_THREADS=200
MAIN_TOMCAT_MIN_SPARE_THREADS=40
AUTH_TOMCAT_MAX_THREADS=100
AUTH_TOMCAT_MIN_SPARE_THREADS=20
MAIN_JAVA_OPTS=-Xms1024m -Xmx2048m
AUTH_JAVA_OPTS=-Xms512m -Xmx1024m
```

```bash
k6 run -e BP_START_VUS=200 -e BP_STEP_VUS=200 -e BP_MAX_VUS=2000 \
  -e BASE_URL=http://<ALB_ENDPOINT> \
  backend/k6/scenarios/04-breakpoint.js
```

#### t3.large (2 vCPU, 8GB RAM)

```env
MAIN_HIKARI_MAX_POOL_SIZE=30
MAIN_HIKARI_MIN_IDLE=15
AUTH_HIKARI_MAX_POOL_SIZE=20
AUTH_HIKARI_MIN_IDLE=10
MAIN_TOMCAT_MAX_THREADS=300
MAIN_TOMCAT_MIN_SPARE_THREADS=60
AUTH_TOMCAT_MAX_THREADS=150
AUTH_TOMCAT_MIN_SPARE_THREADS=30
MAIN_JAVA_OPTS=-Xms2048m -Xmx4096m
AUTH_JAVA_OPTS=-Xms1024m -Xmx2048m
```

```bash
k6 run -e BP_START_VUS=300 -e BP_STEP_VUS=300 -e BP_MAX_VUS=3000 \
  -e BASE_URL=http://<ALB_ENDPOINT> \
  backend/k6/scenarios/04-breakpoint.js
```

### 8.2 스케일 아웃 테스트 가이드

단일 Pod Breakpoint 결과를 기반으로 스케일 아웃을 테스트합니다.

#### 단계 1: 단일 Pod 기준선 확인

```bash
# 예: t3.medium 단일 Pod에서 안정 VU = 800 확인
k6 run -e BP_SCENARIO_FLOW=FIRST_COME_QUEUE -e TOTAL_STOCK=999999 \
  -e BP_START_VUS=100 -e BP_STEP_VUS=100 -e BP_MAX_VUS=2000 \
  -e BASE_URL=http://<ALB_ENDPOINT> \
  backend/k6/scenarios/04-breakpoint.js
```

#### 단계 2: Pod 수 증가하며 부하 테스트 (시나리오 1~3)

```bash
# Pod 2개 → VU 1,600 (안정 VU × 2)
kubectl scale deployment main --replicas=2
k6 run -e VU_COUNT=1600 -e RAMP_UP_SEC=60 -e PRE_SAVE_RATIO=0.7 \
  -e BASE_URL=http://<ALB_ENDPOINT> \
  backend/k6/scenarios/03-first-come-with-queue.js

# Pod 4개 → VU 3,200
kubectl scale deployment main --replicas=4
k6 run -e VU_COUNT=3200 -e RAMP_UP_SEC=90 -e PRE_SAVE_RATIO=0.7 \
  -e BASE_URL=http://<ALB_ENDPOINT> \
  backend/k6/scenarios/03-first-come-with-queue.js

# Pod 8개 → VU 4,500 (기획 목표)
kubectl scale deployment main --replicas=8
k6 run -e VU_COUNT=4500 -e RAMP_UP_SEC=60 -e HOLD_SEC=300 -e PRE_SAVE_RATIO=0.7 \
  -e BASE_URL=http://<ALB_ENDPOINT> \
  backend/k6/scenarios/03-first-come-with-queue.js
```

#### 단계 3: HPA 자동 스케일링 검증

```bash
# HPA 설정 후 점진적 부하 증가로 자동 스케일링 동작 확인
k6 run -e BP_SCENARIO_FLOW=FIRST_COME_QUEUE -e TOTAL_STOCK=999999 \
  -e BP_START_VUS=500 -e BP_STEP_VUS=500 -e BP_MAX_VUS=10000 \
  -e BP_STEP_DURATION_SEC=120 \
  -e BASE_URL=http://<ALB_ENDPOINT> \
  backend/k6/scenarios/04-breakpoint.js
```

> HPA 테스트 시 `BP_STEP_DURATION_SEC`을 120초 이상으로 설정하여 스케일링 반응 시간을 확보합니다.

### 8.3 RDS 설정 참고

| 인스턴스 | max_connections (자동) | 권장 Pod 수 (Pool 20 기준) |
|----------|----------------------|--------------------------|
| db.t3.micro | ~66 | 2~3 |
| db.t3.small | ~150 | 5~6 |
| db.t3.medium | ~312 | 12~14 |
| db.r5.large | ~1365 | 50+ |

> **공식**: `max_connections ≈ {DBInstanceClassMemory/12582880}` (RDS 자동 계산)
> 모든 Pod의 HikariCP pool 합계가 max_connections의 80%를 넘지 않도록 설정합니다.

### 8.4 ElastiCache(Redis) 설정 참고

| 항목 | 권장값 | 비고 |
|------|--------|------|
| 인스턴스 | cache.t3.micro 이상 | 대기열 + 재고 관리 |
| maxmemory-policy | `noeviction` | 재고 데이터 유실 방지 |
| notify-keyspace-events | `Ex` | TTL 만료 이벤트 수신 필수 |

---

## 9. 모니터링 (Prometheus + Grafana)

### Prometheus 연동 실행

```bash
K6_PROMETHEUS_RW_SERVER_URL=http://localhost:9090/api/v1/write \
k6 run -o experimental-prometheus-rw \
  -e VU_COUNT=4500 -e RAMP_UP_SEC=60 \
  backend/k6/scenarios/01-lottery.js
```

### Grafana 대시보드

- Grafana: `http://localhost:9091` (admin / admin)
- k6 기본 대시보드 import: Dashboard ID `18030`
- Loki 로그 확인: Explore → Loki → `{container="main"}` 등

### docker-compose.yml 모니터링 스택

```yaml
# 이미 포함된 서비스:
# - prometheus (9090): 15초 간격으로 gateway/auth/main/queue actuator 스크래핑
# - grafana (9091): 시각화
# - loki (9092): 로그 수집
# - promtail: Docker 컨테이너 로그 → Loki 전송 (onrace.log.collect=true 라벨 기반)
```

---

## 10. 결과 검증 & 리포트 해석

### 10.1 리포트 지표 설명

테스트 완료 시 자동 출력되는 리포트의 각 항목 의미:

#### 성능 지표

| 항목 | 의미 |
|------|------|
| 총 요청 | 테스트 중 발생한 전체 HTTP 요청 수 (폴링, 재시도 포함) |
| 평균 RPS | 초당 요청 처리량 |
| 에러율 | 서버 에러(5xx) 비율. 비즈니스 에러(409, 400)는 미포함 |
| p50 / p90 / p95 | 신청(apply) 응답시간 백분위수 |

#### 비즈니스 지표 (선착순)

| 항목 | 의미 |
|------|------|
| 선점 성공 | apply 200 응답 (RESERVED 상태). 결제 전 단계 |
| 중복/매진 | 이미 신청(409) 또는 유효성 검증 실패(400) |
| Wave1 결제 확정 | Wave1 유저 중 결제까지 완료 (APPLIED 상태) |
| Wave2 결제 확정 | Wave2 유저가 이탈 재고를 재선점 후 결제 완료 |
| 총 결제 확정 | Wave1 + Wave2 합산. **DB APPLIED 수와 일치해야 함** |
| 결제 이탈 | Wave1에서 선점 후 결제하지 않은 건 (TTL 만료 후 재고 반환) |
| 재선점 | 이탈 재고 중 Wave2가 실제로 재선점한 건. 재선점률 = 재선점 / 이탈 |
| 매진 차단 | 최대 재시도 후에도 매진으로 종료된 VU 수 |
| 비즈니스 차단 | 대기열 중복 진입(409), passToken 만료(429) 등 |
| 서버 에러 | 예상 외 HTTP 에러 (5xx 등) |

#### 비즈니스 지표 (응모)

| 항목 | 의미 |
|------|------|
| 응모 성공 | apply 200 응답 (APPLIED 상태). 재고 무제한이므로 전원 성공 기대 |

### 10.2 기대 결과

#### 시나리오 1: 응모신청

| 항목 | 기대값 (VU=4,500) |
|------|-------------------|
| 응모 성공 | 4,500건 (전원 200) |
| DB entry | 4,500건 (status=APPLIED) |
| p95 응답시간 | < 3초 |

#### 시나리오 2: 선착순 대기열X

| 항목 | 기대값 (VU=4,500 / 재고=1,500) |
|------|-------------------------------|
| Wave1 선점 성공 | ~1,500건 |
| Wave1 결제 확정 | ~1,050건 (70%) |
| 결제 이탈 | ~450건 (30%) |
| Wave2 재선점 | ~450건 (재선점률 ~100%) |
| 총 결제 확정 | ~1,500건 |
| **오버셀링** | **0건** |
| p95 응답시간 | < 3초 |

#### 시나리오 3: 선착순 대기열O

| 항목 | 기대값 (VU=4,500 / 재고=1,500) |
|------|-------------------------------|
| 대기열 PASS | 전원 (5,850명) |
| Wave1 선점 성공 | ~1,500건 |
| Wave1 결제 확정 | ~1,050건 (70%) |
| 결제 이탈 | ~450건 (30%) |
| Wave2 재선점 | ~450건 (재선점률 ~100%) |
| 총 결제 확정 | ~1,500건 |
| **오버셀링** | **0건** |
| queue_wait_time p95 | < 5분 |

> **재선점률이 100% 미만인 경우**: 페이스 배분 불일치 또는 테스트 시간 초과(interrupted iterations)로 인한 정상 현상입니다. 오버셀링 0건이면 동시성 제어는 정상입니다.

### 10.3 재고 검증 리포트 (teardown 자동 출력)

```
========================================
         재고 검증 리포트
========================================
이벤트 ID: 103

[DB]    총 재고: 1491  |  확정: 1481
[Redis] 잔여:   10  |  확정: 1481
[Entry] PRE_SAVED: 3018  |  RESERVED: 453  |  APPLIED: 1481

오버셀링:     없음 (OK)       ← 반드시 OK
DB-Redis 일치: 일치 (OK)       ← 반드시 OK
```

- **오버셀링**: APPLIED + RESERVED > 총 재고이면 `!! 발생 !!` 표시
- **DB-Redis 일치**: DB 확정 수 ≠ Redis 확정 수이면 `!! 불일치 !!` 표시
- **RESERVED**: 이탈자 (결제 미완료). TTL 만료 후 재고는 반환되지만 Entry 상태는 RESERVED 유지

### 10.4 검증 쿼리 (동적 ID)

테스트 후 아래 쿼리로 결과를 검증합니다. Setup API가 동적 ID를 생성하므로, 먼저 이벤트 ID를 확인하세요.

```sql
-- 최근 생성된 테스트 이벤트 확인
SELECT id, title, type, is_queue_active
FROM event
WHERE title LIKE 'k6 부하테스트%'
ORDER BY id DESC
LIMIT 5;
```

```sql
-- 오버셀링 확인 (entry 수 <= stock 수) — event_id를 위 결과로 교체
SELECT ep.id AS pace_id, ep.name,
  es.total_stock,
  COUNT(e.id) AS entry_count,
  CASE WHEN COUNT(e.id) > es.total_stock THEN 'OVERSOLD!' ELSE 'OK' END AS status
FROM event_pace ep
JOIN event_stock es ON es.event_pace_id = ep.id
LEFT JOIN entry e ON e.pace_id = ep.id AND e.status IN ('RESERVED', 'APPLIED')
WHERE ep.event_course_id IN (
  SELECT ec.id FROM event_course ec WHERE ec.event_id = <이벤트ID>
)
GROUP BY ep.id, ep.name, es.total_stock;
```

```sql
-- 이벤트별 entry 상태 집계
SELECT e.event_id, COUNT(*) AS total,
  SUM(CASE WHEN e.status = 'APPLIED' THEN 1 ELSE 0 END) AS applied,
  SUM(CASE WHEN e.status = 'RESERVED' THEN 1 ELSE 0 END) AS reserved,
  SUM(CASE WHEN e.status = 'PRE_SAVED' THEN 1 ELSE 0 END) AS pre_saved
FROM entry e
WHERE e.event_id = <이벤트ID>
GROUP BY e.event_id;
```

```sql
-- 중복 entry 검증 (0건이어야 정상)
SELECT user_id, event_id, COUNT(*) AS cnt
FROM entry
WHERE event_id = <이벤트ID>
GROUP BY user_id, event_id HAVING cnt > 1;
```

---

## 11. 커스텀 메트릭 레퍼런스

> 아래 메트릭은 모두 **k6 클라이언트 사이드 메트릭**입니다. 서버 코드가 아닌 k6 스크립트 내에서 HTTP 응답 코드 기반으로 집계됩니다.
> 서버 사이드 메트릭(JVM, DB 커넥션, 요청 처리시간 등)은 Spring Actuator + Prometheus가 자동 수집하며 `/actuator/prometheus` 엔드포인트에서 확인 가능합니다.
>
> **서버 대응 메트릭**이 있는 항목은 `/actuator/prometheus`에서 교차 검증할 수 있습니다.

### 공통 (모든 시나리오)

| 메트릭 | 타입 | 설명 | 서버 대응 메트릭 |
|--------|------|------|-----------------|
| `apply_ok` | Counter | 신청/선점 성공 수 (HTTP 200) | `entry_apply_total{result="success"}` |
| `apply_dup` | Counter | 중복/매진 수 (HTTP 409, 400) | `entry_apply_total{result="duplicate\|sold_out"}` |
| `apply_latency` | Trend | 신청 응답시간 (ms) | — |
| `error_rate` | Rate | 서버 에러 비율 | — |
| `unexpected_error` | Counter | 예상 외 에러 수 | — |

### 선착순 전용 (시나리오 2, 3)

| 메트릭 | 타입 | 설명 | 서버 대응 메트릭 |
|--------|------|------|-----------------|
| `confirm_ok` | Counter | Wave1 결제 확정 수 | `entry_confirm_total` |
| `payment_dropout` | Counter | Wave1 결제 이탈 수 | `entry_rollback_total` |
| `wave2_ok` | Counter | Wave2 재선점+결제 확정 수 | `entry_confirm_total` (Wave 구분 없음) |
| `sold_out` | Counter | 최종 매진 수 | `stock_reserve_total{result="sold_out"}` |

### 대기열 전용 (시나리오 3, 4)

| 메트릭 | 타입 | 설명 | 서버 대응 메트릭 |
|--------|------|------|-----------------|
| `queue_wait_time` | Trend | 대기열 대기 시간 (ms) | — |
| `queue_pass` | Counter | PASS 수신 수 | `queue_pass_total` |
| `queue_timeout` | Counter | 대기열 타임아웃 수 | — |
| `blocked` | Counter | 비즈니스 차단 수 (409, 429) | — |

### HTTP 태그별 응답시간 필터

| 태그 | 시나리오 | 설명 |
|------|---------|------|
| `stock_check` | 1, 2, 3 | 재고 조회 |
| `firstcome_apply` | 2 | 선착순 신청 |
| `queue_enter` | 3 | 대기열 진입 |
| `queue_poll` | 3 | 대기열 폴링 |
| `queue_apply` | 3 | 대기열 경유 선착순 신청 |
| `confirm_reservation` | 2, 3 | 결제 확정 |
| `bp_apply` | 4 | Breakpoint 신청 |
| `bp_confirm` | 4 | Breakpoint 결제 확정 |
| `bp_queue_enter` | 4 | Breakpoint 대기열 진입 |
| `bp_queue_poll` | 4 | Breakpoint 대기열 폴링 |

---

## 12. 트러블슈팅

### Setup API 500 에러

- **서버 미기동**: `docker compose ps`로 main 서비스 상태 확인. 기동 직후 3~5초 대기 필요.
- **프로필 확인**: Setup API는 `@Profile("local")`에서만 활성화됩니다.
- **로그 확인**: `docker compose logs main --tail=100`

### 로그인 대량 실패

- Gateway의 `RequestRateLimiter` 주석 처리 확인
- `LOGIN_BATCH_SIZE` 줄이기 (기본 50)
- `LOGIN_TIMEOUT` 늘리기 (기본 120s)

### Docker 빌드 후 코드 변경 미반영

```bash
# 캐시 무시 재빌드
docker compose build --no-cache && docker compose up -d
```

### 대규모 테스트 시 타임아웃

- `SETUP_TIMEOUT_SEC` 늘리기 (기본 600초)
- `RAMP_UP_SEC` 늘리기 (30,000 VU 시 180초 권장)
- `HOLD_SEC` 늘리기 (대기열 시나리오는 충분한 시간 필요)

### k6 setup 단계에서 메모리 부족

- VU 30,000 기준 토큰 저장 메모리 약 150MB
- k6 실행 머신의 여유 메모리 확인

### AWS 환경에서 Connection Refused

- Security Group에서 Gateway 포트 (30000) 인바운드 허용 확인
- ALB Target Group 헬스체크 경로: `/actuator/health`
- k6 실행 머신 → ALB 네트워크 경로 확인

### Pod 스케일 아웃 시 DB 커넥션 고갈

- 모든 Pod의 HikariCP pool 합계 < RDS max_connections 확인
- 공식: `Pod 수 × MAIN_HIKARI_MAX_POOL_SIZE < max_connections × 0.8`
- t3.micro RDS는 max_connections ~66이므로 Pod 3개 × Pool 20 = 60으로 제한

---

## 프로젝트 구조

```
k6/
├── scenarios/
│   ├── 00-smoke-test.js              # 스모크 테스트 (5 VU)
│   ├── 01-lottery.js                 # 응모신청
│   ├── 02-first-come-no-queue.js     # 선착순 대기열X (2웨이브)
│   ├── 03-first-come-with-queue.js   # 선착순 대기열O (2웨이브)
│   └── 04-breakpoint.js             # 용량(Breakpoint) 테스트
├── lib/
│   ├── config.js        # 환경변수 & 기본값
│   ├── setup.js         # Setup API 호출 (데이터 자동 셋업)
│   ├── auth.js          # 로그인 & 토큰 관리 (배치 로그인)
│   ├── distribution.js  # VU → 페이스 배분 (70/30 인터리빙)
│   ├── retry.js         # HTTP 재시도 래퍼 (5xx만, 지수 백오프)
│   ├── log.js           # 로깅 유틸
│   ├── breakpoint.js    # 계단형 스테이지 생성
│   └── report.js        # 통합 리포트 템플릿
└── README.md            # 이 문서
```
