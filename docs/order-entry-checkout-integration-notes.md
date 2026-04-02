# 주문-신청 연동 주의사항 문서

- 작성일: 2026-03-28
- 상태: P0, MVP 반영 후 메모
- 범위: `checkout` 진입 게이트, `Order.entryId` 저장, 결제 완료 확정 API, 현재 남아 있는 주의사항

## 1. 이번에 반영한 내용

### checkout 진입 게이트

- `OrderService.checkout()`는 주문 생성 전에 `OrderEntryContract.resolveCheckoutEligibility()`를 호출한다.
- `canCheckout=false`면 주문 생성을 중단한다.
- `failureCode`는 `BusinessErrorCode.code`와 매핑해서 `BusinessException`으로 변환한다.
- `failureCode`가 비어 있으면 기본값으로 `ENTRY_CANNOT_APPLY`를 사용한다.
- `failureCode`가 알 수 없는 코드면 `COMMON_SYSTEM_ERROR`로 처리한다.

### order-entry 직접 연결

- `Order` 엔티티에 `entryId` 필드를 추가했다.
- `checkout`에서 `eligibility.entryId()`를 받아 주문에 저장한다.
- `entryId == null`이면 주문을 만들지 않고 `COMMON_SYSTEM_ERROR`로 실패시킨다.

### 결제 완료 확정 API

- `POST /orders/{orderNumber}/confirm`를 추가했다.
- `OrderService.confirmPayment()`가 주문을 조회해 `markPaid()`를 호출한다.
- 상태 전이 규칙은 아래와 같다.
  - `PENDING -> PAID`: 정상 전이
  - `PAID -> PAID`: 그대로 성공 처리
  - `CANCELLED`, `EXPIRED`, `FAILED`: `ORDER_CANNOT_CONFIRM` 예외
- 즉, MVP 기준 idempotency는 "이미 `PAID`면 다시 성공 응답"으로 처리한다.

### 임시 컴파일 복구

- `EntryService`의 `ReservationConfirmedEvent` import와 발행 코드는 현재 주석 처리했다.
- 이유: 클래스가 repo 안에 없어 컴파일이 막히고 있었기 때문이다.
- 이 조치는 P0 unblock용 임시 처리다.

## 2. `entryId`는 언제 DB에 저장되는가

정답은 `checkout` 시점이다.

흐름은 아래와 같다.

1. `POST /orders/checkout` 진입
2. `event/course/pace` 조회
3. `resolveCheckoutEligibility(userId, eventId, paceId)` 호출
4. `eligibility.entryId()` 확보
5. `Order.builder(...).entryId(...)`로 주문 객체 생성
6. `orderRepository.save(order)` 호출
7. 트랜잭션 커밋 시 `orders.entry_id` 컬럼에 반영

즉:

- `entryId 저장 시점`: 주문 생성 시점
- `PAID 전환 시점`: `POST /orders/{orderNumber}/confirm`
- `entry 후속 완료 처리 시점`: 아직 미구현

주의:

- `save()`가 호출되어도 트랜잭션이 롤백되면 DB에는 남지 않는다.
- 따라서 "DB에 저장된다"는 표현은 `checkout` 트랜잭션이 정상 커밋되는 경우를 뜻한다.

## 3. 왜 `entryId`를 결제 완료가 아니라 checkout에 저장하는가

- 어떤 신청을 대상으로 만든 주문인지 먼저 고정해야 한다.
- 이후 결제 성공, 실패, 만료 처리에서 같은 주문이 어떤 entry를 갱신해야 하는지 바로 찾을 수 있다.
- MyPage에서도 order와 entry를 직접 연결할 수 있다.
- 결제 완료 시점까지 기다리면 연결 책임이 분산되고 후속 처리 분기가 더 복잡해진다.

## 4. 지금 코드 기준 주의사항

### DB 스키마

- 현재 main 모듈은 `ddl-auto: update`라 로컬에서는 컬럼이 자동으로 붙을 수 있다.
- 운영/공유 DB에서는 `orders.entry_id` 컬럼 추가를 명시적으로 관리해야 한다.
- 기존 `orders` 데이터가 있으면 backfill 또는 nullable 전략을 먼저 결정해야 한다.
- 지금 엔티티는 `entryId`를 `nullable = false`로 가정한다.

### entry contract

- 결제 가능 상태에서는 `resolveCheckoutEligibility()`가 반드시 `entryId`를 내려줘야 한다.
- `canCheckout=true`인데 `entryId=null`이면 order는 시스템 오류로 실패한다.
- 즉, entry 쪽 계약은 이제 "판단만"이 아니라 "연결 가능한 식별자 제공"까지 포함한다.

### payment 완료 후속 처리

- `PENDING -> PAID`로 바꾸는 mock confirm API는 이제 있다.
- 현재 기준 결제 완료 확정 진입점은 `POST /orders/{orderNumber}/confirm`이다.
- 이 API는 실제 PG 연동이 아니라 MVP/mock 완료 API다.
- 아직 order가 결제 완료 후 entry 상태를 바꾸는 호출도 없다.
- 따라서 지금 단계는 "주문 생성 전 체크", "주문-신청 연결 저장", "주문 PAID 전이"까지 완료된 상태다.
- 아직 빠진 것은 "결제 완료 후 entry 후속 처리"다.

### confirm API 사용 시 주의사항

- 같은 주문번호를 여러 번 confirm해도 이미 `PAID`면 문제 없이 성공해야 한다.
- 반대로 `CANCELLED`, `EXPIRED`, `FAILED` 주문을 confirm하면 안 된다.
- 이 규칙을 위해 `ORDER_CANNOT_CONFIRM` 에러 코드를 추가했다.
- 아직 `confirmedAt` 같은 결제 완료 시각은 저장하지 않는다.
- 아직 이 confirm 안에서 PG 서명 검증, amount 검증, transaction id 검증은 하지 않는다.

### lottery 완료 처리

- 선착은 현재 `confirmReservation(userId, paceId)` 계약이 있다.
- 추첨 당첨 결제 완료는 `entryId` 기준 후속 처리 계약이 아직 부족하다.
- 정석으로 가려면 entry contract에 `completePayment(entryId)`가 추가되어야 한다.

### 임시 주석 처리 복구 필요

- `ReservationConfirmedEvent`는 현재 주석 처리 상태다.
- 이벤트 구조를 복구할지, 동기 호출로 완전히 정리할지 방향을 정하고 다시 연결해야 한다.
- 이 임시 상태를 그대로 두면 "예약 확정 후 비동기 후속 처리"에 대한 설계 의도가 코드에 반영되지 않는다.

## 5. 다음 작업 우선순위

1. 결제 완료 시 order에서 entry 후속 처리 호출
2. MyPage 신청내역이 `entryId + order 상태`를 기준으로 더 직접적으로 상태 계산하도록 보정
3. `ReservationConfirmedEvent` 구조 복구 또는 제거 방향 확정
4. 실제 PG/webhook 연동 시 confirm API를 외부 결제 검증과 연결

## 6. 이번 변경 검증 메모

- `./gradlew :main:compileTestJava`
- `./gradlew :main:test --tests com.kt.onrace.domain.order.OrderServiceTest`

위 두 검증은 현재 통과했다.
