# k6 부하 테스트 가이드

On-Race 마라톤 이벤트 티켓팅 플랫폼 부하 테스트 문서입니다.
**데이터 셋업부터 Redis 초기화, 대기열 활성화까지 모두 자동화**되어 있으므로, k6 명령어 한 줄로 테스트를 실행할 수 있습니다.

---

## 목차

1. [테스트 시나리오 개요](#1-테스트-시나리오-개요)
2. [아키텍처 & 테스트 흐름](#2-아키텍처--테스트-흐름)
3. [사전 준비](#3-사전-준비)
4. [실행 방법](#4-실행-방법)
5. [환경변수 레퍼런스](#5-환경변수-레퍼런스)
6. [시나리오별 실행 예시](#6-시나리오별-실행-예시)
7. [모니터링 (Prometheus + Grafana)](#7-모니터링-prometheus--grafana)
8. [결과 검증](#8-결과-검증)
9. [커스텀 메트릭 레퍼런스](#9-커스텀-메트릭-레퍼런스)
10. [트러블슈팅](#10-트러블슈팅)

---

## 1. 테스트 시나리오 개요

| # | 시나리오 | 스크립트 | 설명 |
|---|---------|---------|------|
| 0 | 스모크 테스트 | `00-smoke-test.js` | 환경 검증 (5 VU, 전체 이벤트 생성) |
| 1 | 응모신청 | `01-lottery.js` | 로그인 → 응모 (재고 무제한, 전원 성공) |
| 2 | 선착순 대기열X | `02-first-come-no-queue.js` | 로그인 → 선착순 → 결제/이탈 → Wave2 재선점 |
| 3 | 선착순 대기열O | `03-first-come-with-queue.js` | 로그인 → 대기열 → 선착순 → 결제/이탈 → Wave2 재선점 |

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
일괄 로그인 (http.batch)
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

### 2.3 페이스 배분

```
전체 VU의 70% (HOT_PACE_RATIO) → 인기 페이스 (라운드로빈)
전체 VU의 30%                   → 나머지 페이스 (라운드로빈)
```

### 2.4 인기 페이스 재고 배분

```
총 재고 = totalStock (직접 지정 or VU_COUNT ÷ 경쟁률)

인기 페이스 재고 = 총재고 × HOT_STOCK_RATIO ÷ HOT_PACE_COUNT
나머지 재고      = (총재고 - 인기 총재고) ÷ (15 - HOT_PACE_COUNT)
```

---

## 3. 사전 준비

### 3.1 k6 설치

```bash
# Windows
winget install grafana.k6

# macOS
brew install k6

# 확인
k6 version
```

### 3.2 Gateway 설정 변경

`gateway/src/main/resources/application.yml`에서:

1. **auth-route**: `RequestRateLimiter` 주석 처리 (IP 기반 rate limit 제거)
2. **queue-route**: 주석 해제, `BotDetectionFilter` 주석 유지
3. **entry-apply-first-come-route**: 주석 해제, `BotDetectionFilter` 주석 유지

### 3.3 서비스 기동

```bash
# Docker 전체 빌드 & 실행 (서비스 + 모니터링 스택)
docker compose build --no-cache && docker compose up -d

# 상태 확인
docker compose ps
```

> **Docker 빌드 시 `--no-cache` 필수**: 소스 코드 변경이 Gradle 캐시 레이어에 가려질 수 있습니다.

---

## 4. 실행 방법

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
1. 00-smoke-test.js     ← 환경 검증 (5 VU)
2. 01-lottery.js        ← 소규모 50~100 VU 검증
3. 01-lottery.js        ← 목표 규모 (4,500 VU)
4. 02-first-come-no-queue.js
5. 03-first-come-with-queue.js
```

> **시나리오 간 별도 초기화 불필요**: 각 시나리오의 setup()이 이전 데이터를 자동 정리합니다.

---

## 5. 환경변수 레퍼런스

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
| `RETRY_WAIT_SEC` | `18` | 재시도 대기 시간 (TTL 15초 + 여유 3초) |

### 대기열 설정 (시나리오 3)

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `QUEUE_POLL_SEC` | `2` | 대기열 폴링 간격(초) |
| `QUEUE_MAX_POLL` | `300` | 최대 폴링 횟수 |

### 로그인 설정

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `LOGIN_TIMEOUT` | `120s` | 로그인 HTTP 타임아웃 |
| `LOGIN_BATCH_SIZE` | `50` | 배치 로그인 단위 |

### 기타

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `HOT_PACE_RATIO` | `0.7` | VU의 70%가 인기 페이스 대상 |

---

## 6. 시나리오별 실행 예시

### 6.1 스모크 테스트

```bash
k6 run backend/k6/scenarios/00-smoke-test.js
```

### 6.2 시나리오 1: 응모신청 (01-lottery)

> 재고 제한 없음. 전원 신청 성공이 기대됩니다.

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

### 6.3 시나리오 2: 선착순 대기열X (02-first-come-no-queue)

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

### 6.4 시나리오 3: 선착순 대기열O (03-first-come-with-queue)

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

### 6.5 고급 옵션 조합 예시

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

## 7. 모니터링 (Prometheus + Grafana)

### Prometheus 연동 실행

```bash
K6_PROMETHEUS_RW_SERVER_URL=http://localhost:9090/api/v1/write \
k6 run -o experimental-prometheus-rw \
  -e VU_COUNT=4500 -e RAMP_UP_SEC=60 \
  backend/k6/scenarios/01-lottery.js
```

### Grafana 대시보드

- Grafana: `http://localhost:3000` (admin / admin)
- k6 기본 대시보드 import: Dashboard ID `18030`
- Loki 로그 확인: Explore → Loki → `{container="main"}` 등

---

## 8. 결과 검증

### 8.1 기대 결과

#### 시나리오 1: 응모신청

| 항목 | 기대값 (VU=4,500) |
|------|-------------------|
| 응모 성공 | 4,500건 (전원 200) |
| DB entry | 4,500건 (status=APPLIED) |
| p95 응답시간 | < 3초 |

#### 시나리오 2: 선착순 대기열X

| 항목 | 기대값 (VU=4,500 / 재고=1,500) |
|------|-------------------------------|
| Wave 1 선점 성공 | ~1,500건 |
| Wave 1 결제확정 | ~1,050건 (70%) |
| Wave 1 결제이탈 | ~450건 (30%) |
| Wave 2 재선점+확정 | ~450건 |
| **오버셀링** | **0건** |
| p95 응답시간 | < 3초 |

#### 시나리오 3: 선착순 대기열O

| 항목 | 기대값 (VU=4,500 / 재고=1,500) |
|------|-------------------------------|
| PASS 수신 | 전원 |
| Wave 1 선점 성공 | ~1,500건 |
| Wave 1 결제확정 | ~1,050건 (70%) |
| Wave 1 결제이탈 | ~450건 (30%) |
| Wave 2 재선점+확정 | ~450건 |
| **오버셀링** | **0건** |
| queue_wait_time p95 | < 5분 |

### 8.2 검증 쿼리 (동적 ID)

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

## 9. 커스텀 메트릭 레퍼런스

### 시나리오 1: 응모신청

| 메트릭 | 타입 | 설명 |
|--------|------|------|
| `lottery_apply_success` | Counter | 응모 성공 수 |
| `lottery_apply_fail` | Counter | 응모 실패 수 |

### 시나리오 2: 선착순 대기열X

| 메트릭 | 타입 | 설명 |
|--------|------|------|
| `firstcome_reserve_success` | Counter | 선점 성공 수 |
| `firstcome_payment_confirmed` | Counter | Wave 1 결제확정 수 |
| `firstcome_payment_dropout` | Counter | Wave 1 결제이탈 수 |
| `firstcome_wave2_confirmed` | Counter | Wave 2 재선점+확정 수 |
| `firstcome_sold_out` | Counter | 최종 매진 수 |
| `firstcome_already_reserved` | Counter | 중복 신청(409) 수 |
| `firstcome_unexpected_error` | Counter | 예상 외 에러 수 |

### 시나리오 3: 선착순 대기열O

| 메트릭 | 타입 | 설명 |
|--------|------|------|
| `queue_wait_time` | Trend | 대기열 대기 시간(ms) |
| `queue_pass_count` | Counter | PASS 수신 수 |
| `queue_timeout_count` | Counter | 대기열 타임아웃 수 |
| `queue_reserve_success` | Counter | 선점 성공 수 |
| `queue_payment_confirmed` | Counter | Wave 1 결제확정 수 |
| `queue_payment_dropout` | Counter | Wave 1 결제이탈 수 |
| `queue_wave2_confirmed` | Counter | Wave 2 재선점+확정 수 |
| `queue_sold_out` | Counter | 최종 매진 수 |
| `queue_already_reserved` | Counter | 중복 신청(409) 수 |
| `queue_unexpected_error` | Counter | 예상 외 에러 수 |

---

## 10. 트러블슈팅

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

---

## 프로젝트 구조

```
k6/
├── scenarios/
│   ├── 00-smoke-test.js              # 스모크 테스트 (5 VU)
│   ├── 01-lottery.js                 # 응모신청
│   ├── 02-first-come-no-queue.js     # 선착순 대기열X (2웨이브)
│   └── 03-first-come-with-queue.js   # 선착순 대기열O (2웨이브)
├── lib/
│   ├── config.js        # 환경변수 & 기본값
│   ├── setup.js         # Setup API 호출 (데이터 자동 셋업)
│   ├── auth.js          # 로그인 & 토큰 관리 (배치 로그인)
│   ├── distribution.js  # VU → 페이스 배분 (70/30 HOT 배분)
│   ├── retry.js         # HTTP 재시도 래퍼
│   └── log.js           # 로깅 유틸
└── README.md            # 이 문서
```
