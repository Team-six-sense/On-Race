# k6 부하 테스트

On-Race 마라톤 이벤트 티켓팅 플랫폼 부하 테스트 스크립트.

## 테스트 시나리오

| # | 시나리오 | 스크립트 | 설명 |
|---|---------|---------|------|
| 0 | 스모크 테스트 | `00-smoke-test.js` | 환경 검증 (5 VU) |
| 1 | 응모신청 | `01-lottery.js` | 로그인 → 응모 (전원 성공) |
| 2 | 선착순 대기열X | `02-first-come-no-queue.js` | 로그인 → 선착순 (재고 소진 시 매진) |
| 3 | 선착순 대기열O | `03-first-come-with-queue.js` | 로그인 → 대기열 → 폴링 → 선착순 |

## 테스트 조건

### 소규모 (기본)
- **재고**: 15페이스 x 100석 = 1,500석/이벤트 (`setup-load-test-events.sql`)
- **유저**: 4,500명 (경쟁률 3:1)

### 대규모
- **재고**: 15페이스 x 667석 = ~10,000석/이벤트 (`setup-load-test-events-large.sql`)
- **유저**: 최대 30,000명 (경쟁률 3:1)

### 공통
- **배분**: 70% HOT 페이스 집중, 30% 나머지 14개 분산
- **Gateway 경유**: JWT 인증 + 큐 토큰 검증 활성화, 봇 토큰/Rate Limit 비활성화

## 사전 준비

### 1. k6 설치

```bash
# Windows
winget install grafana.k6

# macOS
brew install k6

# 확인
k6 version
```

### 2. Gateway 설정 변경

`gateway/src/main/resources/application.yml`에서:

1. **auth-route**: `RequestRateLimiter` 주석 처리 (IP 기반 rate limit 제거)
2. **queue-route**: 주석 해제, BotDetectionFilter 주석 유지
3. **entry-apply-first-come-route**: 주석 해제, BotDetectionFilter 주석 유지

### 3. 서비스 기동

```bash
# Docker로 전체 기동
docker-compose up -d --build

# 또는 개별 실행
# Gateway(30000), Auth(31000), Main(32000), Queue(33000), MySQL, Redis
```

### 4. 데이터 셋업

#### 방법 A: API 자동 셋업 (권장)

k6 시나리오 스크립트(00~03)는 `setup()` 단계에서 아래 API를 자동 호출합니다.
별도 SQL 실행이나 Redis 초기화가 **필요 없습니다**.

```bash
# 수동으로 확인하려면:
curl -X POST http://localhost:30000/main/internal/load-test/setup \
  -H 'Content-Type: application/json' \
  -d '{"stockPerPace": 100}'
```

이 API는 유저 3만명 등록(멱등) → 이전 데이터 정리 → 이벤트/코스/페이스/재고/판매정보 생성 → Redis flushDB + 재고 초기화 + 대기열 활성화를 한 번에 수행합니다.
`local` 프로필에서만 활성화됩니다.

#### 방법 B: SQL 수동 실행 (레거시)

API를 사용하지 않고 직접 SQL을 실행하는 경우:

```bash
# 유저 데이터 (30,000명)
mysql -u root -p on-race-main < k6/data/setup-load-test-users.sql

# 이벤트 데이터 (소규모: 100석/페이스)
mysql -u root -p on-race-main < k6/data/setup-load-test-events.sql

# 이벤트 데이터 (대규모: 667석/페이스) — 10K+ VU 테스트 시
# mysql -u root -p on-race-main < k6/data/setup-load-test-events-large.sql
```

### 5. Redis 재고 초기화 (SQL 수동 실행 시만)

API 셋업 사용 시 자동 처리됩니다. SQL 수동 실행 시에만 필요합니다.

```bash
curl -X POST http://localhost:30000/main/events/11/stock/init
curl -X POST http://localhost:30000/main/events/12/stock/init
curl -X POST http://localhost:30000/main/events/13/stock/init
```

### 6. 대기열 활성화 (SQL 수동 실행 + 시나리오 3만)

API 셋업 사용 시 자동 처리됩니다.

```bash
curl -X POST http://localhost:30000/main/events/13/queue/enable
```

## 실행

### Phase 1: 스모크 테스트

```bash
k6 run k6/scenarios/00-smoke-test.js
```

### Phase 2: 소규모 검증 (50~100 VU)

```bash
k6 run -e VU_COUNT=50 k6/scenarios/01-lottery.js
# cleanup 후 →
k6 run -e VU_COUNT=50 k6/scenarios/02-first-come-no-queue.js
# cleanup 후 →
k6 run -e VU_COUNT=50 k6/scenarios/03-first-come-with-queue.js
```

### Phase 3: 기획 목표 (4,500 VU, 1,500석)

```bash
k6 run -e VU_COUNT=4500 -e RAMP_UP_SEC=60 k6/scenarios/01-lottery.js
k6 run -e VU_COUNT=4500 -e RAMP_UP_SEC=60 k6/scenarios/02-first-come-no-queue.js
k6 run -e VU_COUNT=3800 -e RAMP_UP_SEC=60 -e HOLD_SEC=300 k6/scenarios/03-first-come-with-queue.js
```

### Phase 4: 대규모 (10,000~15,000 VU, 10,000석)

```bash
# 대규모 이벤트 데이터로 교체
mysql -u root -p on-race-main < k6/data/setup-load-test-events-large.sql

# .env에서 HIKARI 풀 50, 서비스 재시작 후
k6 run -e VU_COUNT=10000 -e RAMP_UP_SEC=120 -e HOLD_SEC=300 k6/scenarios/02-first-come-no-queue.js
```

### Phase 5: 최대 스케일 (30,000 VU)

```bash
k6 run -e VU_COUNT=30000 -e RAMP_UP_SEC=180 -e HOLD_SEC=600 -e LOGIN_TIMEOUT=180s \
  k6/scenarios/02-first-come-no-queue.js
```

### Prometheus 연동

```bash
K6_PROMETHEUS_RW_SERVER_URL=http://localhost:9090/api/v1/write \
k6 run -o experimental-prometheus-rw -e VU_COUNT=4500 k6/scenarios/01-lottery.js
```

## 환경변수

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `BASE_URL` | `http://localhost:30000` | Gateway 주소 |
| `VU_COUNT` | `100` | 동시 사용자 수 (최대 30,000) |
| `USER_PASSWORD` | `Test1234!@` | 테스트 유저 비밀번호 |
| `LOGIN_TIMEOUT` | `120s` | 로그인 HTTP 타임아웃 |
| `HOT_PACE_RATIO` | `0.7` | HOT 페이스 집중 비율 |
| `RAMP_UP_SEC` | `10` | 램프업 시간(초) |
| `HOLD_SEC` | `120` | 유지 시간(초) |
| `RAMP_DOWN_SEC` | `5` | 램프다운 시간(초) |
| `QUEUE_POLL_SEC` | `2` | 대기열 폴링 간격(초) |
| `QUEUE_MAX_POLL` | `300` | 최대 폴링 횟수 |
| `EVENT_LOTTERY` | `11` | 응모 이벤트 ID |
| `EVENT_FC_NO_QUEUE` | `12` | 선착순(대기열X) 이벤트 ID |
| `EVENT_FC_WITH_QUEUE` | `13` | 선착순(대기열O) 이벤트 ID |
| `HOT_COURSE_ID` | (자동) | HOT 코스 ID (오버라이드) |
| `HOT_PACE_ID` | (자동) | HOT 페이스 ID (오버라이드) |
| `OTHER_PACES` | (자동) | 기타 페이스 JSON (오버라이드) |

## ID 체계

### API 셋업 사용 시 (권장)

`POST /main/internal/load-test/setup` API를 통해 데이터를 셋업하면 **auto_increment로 ID가 동적 할당**됩니다.
k6 시나리오 스크립트(01~03)는 setup 응답의 `eventIds`와 `paceMap`을 사용하므로 고정 ID에 의존하지 않습니다.

### SQL 수동 실행 시 (레거시)

아래 고정 ID는 `k6/data/*.sql` 파일을 직접 실행할 때만 해당됩니다.

| 구분 | ID 범위 | 비고 |
|------|---------|------|
| 유저 | 10001~40000 | k6user00001~k6user30000 |
| 이벤트 11 | 코스 31-33, 페이스 151-165 | 응모, HOT=162 |
| 이벤트 12 | 코스 34-36, 페이스 166-180 | 선착순(대기열X), HOT=177 |
| 이벤트 13 | 코스 37-39, 페이스 181-195 | 선착순(대기열O), HOT=192 |

## 테스트 간 초기화

시나리오 재실행 전 반드시:

```bash
# 1. SQL 초기화 (entry 삭제 + stock 리셋)
mysql -u root -p on-race-main < k6/data/cleanup-between-tests.sql

# 2. Redis 재고 초기화
curl -X POST http://localhost:30000/main/events/11/stock/init
curl -X POST http://localhost:30000/main/events/12/stock/init
curl -X POST http://localhost:30000/main/events/13/stock/init

# 3. 대기열 재활성화 (시나리오 3)
curl -X POST http://localhost:30000/main/events/13/queue/enable
```

## 기대 결과

### 시나리오 1: 응모신청

| 항목 | 기대값 (4,500 VU) |
|------|-------------------|
| 응모 성공 | VU_COUNT건 (전원 200) |
| DB entry | VU_COUNT건 (status=APPLIED) |
| p95 응답시간 | < 3초 |

### 시나리오 2: 선착순 대기열X

| 항목 | 기대값 (4,500 VU / 1,500석) |
|------|----------------------------|
| 선점 성공 (200) | ~1,500건 |
| 매진 응답 (400) | ~3,000건 |
| 오버셀링 | **0건** |
| p95 응답시간 | < 3초 |

### 시나리오 3: 선착순 대기열O

| 항목 | 기대값 (4,500 VU / 1,500석) |
|------|----------------------------|
| PASS 수신 | 전원 |
| 선점 성공 (200) | ~1,500건 |
| 매진 응답 (400) | ~2,300건 |
| 오버셀링 | **0건** |
| queue_wait_time p95 | < 5분 |

## 검증 쿼리

```sql
-- 오버셀링 확인 (entry 수 <= stock 수)
SELECT ep.id AS pace_id, ep.name,
  es.total_stock,
  COUNT(e.id) AS entry_count,
  CASE WHEN COUNT(e.id) > es.total_stock THEN 'OVERSOLD!' ELSE 'OK' END AS status
FROM event_pace ep
JOIN event_stock es ON es.event_pace_id = ep.id
LEFT JOIN entry e ON e.event_pace_id = ep.id AND e.status IN ('RESERVED', 'APPLIED')
WHERE ep.id BETWEEN 151 AND 195
GROUP BY ep.id, ep.name, es.total_stock;

-- 이벤트별 entry 집계
SELECT e.event_id, COUNT(*) AS total,
  SUM(CASE WHEN e.status = 'APPLIED' THEN 1 ELSE 0 END) AS applied,
  SUM(CASE WHEN e.status = 'RESERVED' THEN 1 ELSE 0 END) AS reserved
FROM entry e
WHERE e.user_id BETWEEN 10001 AND 40000
GROUP BY e.event_id;

-- 중복 entry 검증
SELECT user_id, event_id, COUNT(*) AS cnt
FROM entry WHERE user_id BETWEEN 10001 AND 40000
GROUP BY user_id, event_id HAVING cnt > 1;
```

## 커스텀 메트릭

| 시나리오 | 메트릭 | 타입 | 설명 |
|---------|--------|------|------|
| 01 | `lottery_apply_success` | Counter | 응모 성공 수 |
| 01 | `lottery_apply_fail` | Counter | 응모 실패 수 |
| 02 | `firstcome_reserve_success` | Counter | 선점 성공 수 |
| 02 | `firstcome_sold_out` | Counter | 매진 응답 수 |
| 02 | `firstcome_unexpected_error` | Counter | 예상 외 에러 수 |
| 03 | `queue_wait_time` | Trend | 대기열 대기 시간(ms) |
| 03 | `queue_pass_count` | Counter | PASS 수신 수 |
| 03 | `queue_timeout_count` | Counter | 대기열 타임아웃 수 |
| 03 | `queue_reserve_success` | Counter | 선점 성공 수 |
| 03 | `queue_sold_out` | Counter | 매진 응답 수 |
