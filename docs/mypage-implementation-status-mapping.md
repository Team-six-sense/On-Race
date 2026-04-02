# 마이페이지 구현용 상태 매핑표

- 작성일: 2026-03-28
- 대상: MyPage, Order, Entry, Payment 담당자
- 목적: 마이페이지 구현에 바로 사용할 상태 매핑 기준을 정리하고, 현재 코드 기준 확정 영역과 팀 합의가 필요한 영역을 분리한다.

## 1. 한눈에 보는 결론

- `orders.orderStatus`는 이미 결제 상태 진실 소스로 동작한다.
- MyPage 주문내역은 현재 코드만으로 구현 가능하다.
- MyPage 신청내역은 결제 완료 후 처리 기준이 아직 확정되지 않아 정책 합의가 필요하다.
- 특히 `EntryStatus.PAYMENT_COMPLETED`를 쓸지, 아니면 MVP 동안 `OrderStatus.PAID`만으로 숨김/노출을 제어할지가 핵심 결정이다.

## 2. 현재 코드 기준 확정된 사실

### Order

- `checkout` 전에는 `OrderEntryContract.resolveCheckoutEligibility()`로 결제 가능 여부를 확인한다.
- `checkout` 시 `entryId`를 `Order`에 저장한다.
- `POST /orders/{orderNumber}/confirm`으로 `PENDING -> PAID` 전이가 가능하다.
- 별도 `payment` 테이블은 없다. 현재는 `orders.orderStatus`가 결제 상태 역할을 겸한다.

### Entry

- `EntryStatus`는 현재 `PRE_SAVED`, `RESERVED`, `APPLIED`, `WON`, `LOST`만 존재한다.
- `PAYMENT_COMPLETED`는 아직 없다.
- 선착 후속 처리용 `confirmReservation(userId, paceId)`는 있다.
- 추첨 결제 완료용 `completePayment(entryId)`는 아직 없다.

### MyPage

- 주문내역은 `OrderStatus` 기반으로 표시 상태를 계산한다.
- `/mypage/waiting-entries`는 `PRE_SAVED`, `RESERVED`만 조회한다.
- `/mypage/entries`는 현재 `APPLIED`, `WON`, `LOST`를 기본 대상으로 본다.
- 문서 계약상 `entries`는 결제 완료 주문이 이미 존재하는 신청을 제외하는 것이 맞다.
- 하지만 현재 구현은 summary성 조건에서는 `PAID` 주문을 제외하고, application history 계산은 여전히 entry 상태 중심이다.

## 3. 마이페이지 구현에 필요한 최소 결정

아래 5개가 확정돼야 MyPage 구현을 끝까지 밀 수 있다.

1. `EntryStatus.PAYMENT_COMPLETED`를 도입할지
2. `PAYMENT_COMPLETED`를 lottery만 쓸지, first-come도 쓸지
3. 결제 완료된 신청을 `/mypage/entries`에서 숨길지 보여줄지
4. 상태 충돌 시 `entry`와 `order` 중 무엇을 우선할지
5. 취소/만료 이후 신청내역 문구와 노출 위치를 어떻게 할지

## 4. 추천 기본안

MyPage 담당자 관점에서 가장 구현이 단순하고 정책 충돌이 적은 기본안은 아래와 같다.

- 결제 성공 사실은 `OrderStatus.PAID`를 진실 소스로 본다.
- 신청 최종 확정 상태는 장기적으로 `EntryStatus.PAYMENT_COMPLETED`를 둔다.
- 다만 entry 쪽 구현이 늦더라도, MVP에서는 `PAID` 주문 존재 여부로 신청내역 노출을 제어한다.
- 결제 완료된 신청은 신청내역보다 주문내역에서 보여주는 것을 기본안으로 둔다.

즉:

```text
MVP: order 기준으로 숨김/노출 제어
정석: entry + order 둘 다 정합성 있게 유지
```

## 5. 화면별 구현 기준

### 5-1. `/mypage/orders`

이 영역은 현재 코드 기준으로 거의 확정 상태다.

| OrderStatus | 주문 탭 | 표시 문구 | 액션 | 구현 상태 |
| --- | --- | --- | --- | --- |
| `PENDING` | `PENDING` | `결제 대기` | `DETAIL` | 구현됨 |
| `PAID` | `COMPLETED` | `결제 완료` | `DETAIL` | 구현됨 |
| `CANCELLED` | `CANCELLED` | `주문 취소` | `DETAIL` | 구현됨 |
| `EXPIRED` | `ALL` | `주문 만료` | `DETAIL` | 구현됨 |
| `FAILED` | `ALL` | `주문 실패` | `DETAIL` | 구현됨 |

추가 메모:

- `paymentDeadlineAt`는 현재 원천이 없어 `null` 유지
- `paymentMethod`도 현재 원천이 없어 `null` 유지

### 5-2. `/mypage/waiting-entries`

이 영역은 “결제 완료 전 대기/예약 상태”만 보여주는 화면으로 유지하는 것이 맞다.

| EntryStatus | appType | 표시 문구 | 액션 | 노출 여부 | 구현 상태 |
| --- | --- | --- | --- | --- | --- |
| `PRE_SAVED` | `FIRST_COME` | `신청 대기` 또는 `사전정보 저장` | `EDIT` | 노출 | 구현됨 |
| `RESERVED` | `FIRST_COME` | `예약 중` | `CHECKOUT` | 노출 | 구현됨 |
| `APPLIED` | any | 노출 안 함 | - | 비노출 | 구현됨 |
| `WON` | `LOTTERY` | 노출 안 함 | - | 비노출 | 구현됨 |
| `LOST` | `LOTTERY` | 노출 안 함 | - | 비노출 | 구현됨 |

추천 유지 원칙:

- `waiting-entries`는 결제 전 상태 전용으로 유지
- 결제 완료된 신청은 이 목록에 절대 남기지 않음

### 5-3. `/mypage/entries`

이 영역이 현재 가장 중요한 정책 결정 지점이다.

#### 현재 코드 기준

| EntryStatus | 기본 처리 | 비고 |
| --- | --- | --- |
| `APPLIED` | 노출 | lottery 응모 완료 또는 first-come 신청 완료 |
| `WON` | 노출 | 현재 resolver는 `당첨 + 결제하기`로 계산 |
| `LOST` | 노출 | `미당첨` |
| `PRE_SAVED` | 기본 비노출 | waiting 전용 |
| `RESERVED` | 기본 비노출 | waiting 전용, 일부 mixed history에는 포함 여지 있음 |

#### 추천 기본안

| EntryStatus | OrderStatus | 추천 표시 | 액션 | 노출 위치 |
| --- | --- | --- | --- | --- |
| `APPLIED` | 주문 없음 | `응모 완료` 또는 `신청 완료` | `NONE` | `entries` |
| `APPLIED` | `PENDING` | `신청 완료` | `NONE` | `entries` 또는 주문내역 병행 |
| `APPLIED` | `PAID` | 신청내역 비노출 | - | `orders(COMPLETED)` |
| `WON` | 주문 없음 | `당첨` | `CHECKOUT` | `entries` |
| `WON` | `PENDING` | `당첨` | `CHECKOUT` | `entries` |
| `WON` | `PAID` | 신청내역 비노출 | - | `orders(COMPLETED)` |
| `LOST` | 주문 없음 | `미당첨` | `NONE` | `entries` |
| `PAYMENT_COMPLETED` | `PAID` | 신청내역 비노출 | - | `orders(COMPLETED)` |
| `CANCELED` or `CANCELLED` | any | `당첨 취소` | `NONE` | 정책 확정 필요 |

핵심 추천:

- `entries`는 “결제 완료 이전 신청 상태” 위주로 유지
- `PAID` 주문이 붙은 신청은 `entries`에서 제외
- 결제 완료된 건 주문내역에서 보여줌

이 추천은 기존 계약 문서의 아래 원칙과 맞춘 것이다.

- `entries는 결제 완료 주문이 이미 존재하는 신청을 제외한다`

## 6. Lottery / First-Come 분리 구현 기준

### Lottery

| 상태 조합 | 표시 문구 | 액션 | 추천 |
| --- | --- | --- | --- |
| `APPLIED` + 이벤트 진행 중 | `응모 완료` | `NONE` | 유지 |
| `APPLIED` + 이벤트 종료 | `결과 발표 대기` | `NONE` | 유지 |
| `APPLIED` + 추첨 완료 | `결과 확인 필요` | `NONE` | 유지 |
| `WON` + 주문 없음 또는 `PENDING` | `당첨` | `CHECKOUT` | 유지 |
| `WON` + 주문 `PAID` | 신청내역 비노출 또는 `결제 완료` | 정책 선택 필요 |
| `LOST` | `미당첨` | `NONE` | 유지 |

### First-Come

| 상태 조합 | 표시 문구 | 액션 | 추천 |
| --- | --- | --- | --- |
| `PRE_SAVED` + 오픈 전/대기 | `신청 대기` 또는 `사전정보 저장` | `EDIT` | 유지 |
| `PRE_SAVED` + 오픈 중 | `신청 가능` | `APPLY` | 유지 |
| `RESERVED` | `예약 중` | `CHECKOUT` | 유지 |
| `APPLIED` + 주문 없음 또는 `PENDING` | `신청 완료` | `NONE` | 유지 |
| `APPLIED` + 주문 `PAID` | 신청내역 비노출 | - | 추천 |
| `PAYMENT_COMPLETED` + 주문 `PAID` | 신청내역 비노출 | - | 추천 |

## 7. Entry 변경이 늦을 때의 MVP fallback

entry 팀의 `PAYMENT_COMPLETED` 구현이 늦더라도, MyPage는 아래 방식으로 먼저 완성할 수 있다.

### fallback 원칙

- `EntryStatus`만 믿지 않고, `OrderStatus.PAID`를 함께 본다.
- `WON + PAID`, `APPLIED + PAID`는 신청내역에서 숨긴다.
- 완료 사실은 주문내역 `COMPLETED` 탭에서만 보여준다.

### fallback 장점

- entry enum 확장 없이도 MyPage 구현 가능
- 현재 order 쪽 구현만으로도 결제 완료 표시 가능
- UX 기준으로 “결제 완료는 주문내역에서 본다”를 맞출 수 있음

### fallback 한계

- entry 자체 상태 머신은 완성되지 않음
- `entry=WON`, `order=PAID`처럼 도메인 상태가 분리됨
- 장기적으로는 `PAYMENT_COMPLETED`가 있는 편이 더 정합적임

## 8. 구현 시 체크리스트

### MyPage 담당자 즉시 구현 가능

- 주문내역은 현재 `OrderStatus` 기준 그대로 사용
- `entries` 조회 시 `PAID` 주문이 있으면 숨김 처리
- `waiting-entries`는 결제 전 상태만 유지
- `paymentMethod`, `paymentDeadlineAt`는 현재 `null` 허용 유지

### 팀 합의 후 구현할 것

- `PAYMENT_COMPLETED` entry 문구와 노출 위치
- `CANCELED/CANCELLED` 표준 철자
- 취소/만료 시 신청내역 노출 정책
- lottery와 first-come의 완료 상태를 통일할지 여부

## 9. 회의에서 바로 물어볼 질문

1. `PAYMENT_COMPLETED`를 entry에 추가할 것인가
2. 추가한다면 lottery만 쓸 것인가, first-come도 쓸 것인가
3. 결제 완료 신청은 신청내역에서 숨길 것인가, `결제 완료`로 보여줄 것인가
4. `entry`와 `order`가 충돌할 때 MyPage는 어느 쪽을 우선할 것인가
5. `CANCELED`와 `CANCELLED` 중 무엇으로 통일할 것인가

## 10. MyPage 담당자용 최종 권고

현재 시점에서 가장 안전한 선택은 아래다.

```text
주문내역은 order 기준으로 완성한다.
신청내역은 paid order가 붙은 항목을 숨기는 방향으로 먼저 완성한다.
entry의 PAYMENT_COMPLETED는 후속 확장으로 받는다.
```

이 방식이면 팀 합의가 끝나기 전에도 MyPage 구현을 대부분 진행할 수 있고, 이후 entry 확장이 들어와도 큰 구조 변경 없이 보정 가능하다.
