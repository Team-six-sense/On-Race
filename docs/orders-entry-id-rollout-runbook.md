# orders.entry_id 운영 반영 Runbook

- 작성일: 2026-03-28
- 목적: `orders.entry_id`를 2단계로 안전하게 운영 반영하고, backfill 동안 조회 정합성을 유지한다.
- 범위: `main` 애플리케이션 배포, 운영 DB 수동 DDL/DML, null 모니터링, phase 2 마무리

## 1. 이번 배포에 포함된 코드

- `Order.entryId`는 phase 1 배포를 위해 nullable 컬럼으로 열어둔다.
- `OrderService.checkout()`는 계속 `entryId`를 채워 저장한다.
- `ApplicationHistoryService`는 `orders.entry_id`를 우선 사용하고, `entry_id IS NULL`인 legacy row는 `user_id + event_id` fallback으로 읽는다.
- `OrderEntryIdMonitor`가 앱 시작 시점과 주기적으로 `entry_id IS NULL` 잔존 건수를 로그로 남긴다.

즉 phase 1 동안은:

```text
새 주문: entry_id dual-write
기존 주문: backfill 전까지 legacy fallback read
운영 모니터링: SQL + 애플리케이션 warn log
```

## 2. 반영 순서

### Phase 1

1. 운영 DB에 [01_phase1_add_entry_id_nullable.sql](/Users/wusu/dev/On-Race/backend/scripts/orders-entry-id/01_phase1_add_entry_id_nullable.sql) 적용
2. nullable + dual-write + fallback read 코드 배포
3. 앱 기동 후 로그에서 `orders.entry_id monitor` 메시지 확인

### Backfill

1. [02_backfill_entry_id.sql](/Users/wusu/dev/On-Race/backend/scripts/orders-entry-id/02_backfill_entry_id.sql) 실행
2. [03_monitor_entry_id_nulls.sql](/Users/wusu/dev/On-Race/backend/scripts/orders-entry-id/03_monitor_entry_id_nulls.sql)로 남은 null 확인
3. `null_entry_id_count = 0`이 될 때까지 반복
4. `e.id IS NULL` 샘플이 남으면 데이터 정합성 이슈로 분류하고 수동 보정

### Phase 2

1. `null_entry_id_count = 0` 확인
2. 애플리케이션 로그에서도 `null_count=0` 확인
3. [04_phase2_enforce_not_null_and_add_index.sql](/Users/wusu/dev/On-Race/backend/scripts/orders-entry-id/04_phase2_enforce_not_null_and_add_index.sql) 적용

## 3. 모니터링 기준

### SQL

- 기본 count: `null_entry_id_count`
- 상태별 분포: `order_status` group by
- 보정 불가 샘플: `entry`를 찾지 못한 row

### Application log

- logger message: `orders.entry_id monitor`
- startup 1회 + 기본 5분 주기
- 설정
  - `MAIN_ORDER_ENTRY_ID_MONITOR_ENABLED`
  - `MAIN_ORDER_ENTRY_ID_MONITOR_FIXED_DELAY_MS`
  - `MAIN_ORDER_ENTRY_ID_MONITOR_SAMPLE_SIZE`

## 4. 완료 기준

- 새로 생성되는 주문에 `entry_id` 누락이 없다.
- backfill 이후 `orders.entry_id IS NULL` row가 0건이다.
- `/mypage/entries`와 overview 집계에서 결제 완료 주문 연결이 누락되지 않는다.
- phase 2 적용 후 `orders.entry_id`가 `NOT NULL`이고 `idx_orders_user_id_order_status_entry_id` 인덱스가 존재한다.

## 5. 의도적으로 이번 범위에서 제외한 것

- `orders.entry_id -> entry.id` foreign key 추가
- 자동 DB migration 도구 도입
- 과거 데이터의 business-level 수동 정정 로직

foreign key와 자동 migration은 별도 작업으로 분리하는 편이 운영 위험이 낮다.
