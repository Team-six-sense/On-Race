# 마이페이지 백엔드 대응 정리

## 목적

- 이 문서는 프론트 요청사항을 백엔드 구현 관점으로 재해석한 작업 기준서이다.
- 초점은 "프론트가 뭘 원한다"가 아니라, 현재 On-Race 백엔드에서 무엇을 유지하고 무엇을 수정해야 하는지 정리하는 데 있다.
- 특히 신청내역 화면은 현재 DTO와 화면 요구사항 사이 간극이 크므로, 기존 `/mypage/entries` 확장을 중심으로 대응한다.

## 현재 상황 요약

- `/mypage` 는 overview 용 aggregate API로 이미 존재한다.
- `/mypage/account`, `/mypage/address`, `/mypage/entries`, `/mypage/waiting-entries`, `/mypage/orders` 는 이미 분리 API로 존재한다.
- 따라서 "마이페이지를 하나의 통합 API에서 분리한다"는 과제는 이미 상당 부분 해결되어 있다.
- 실제 문제는 신청내역 계약이 화면 요구사항을 충분히 표현하지 못한다는 점이다.

## 프론트 요청을 백엔드 관점으로 해석한 결론

### 유지할 것

- `GET /mypage`
  - 메인 overview 전용으로 유지
- `GET /mypage/account`
  - 현재 계약 유지
- `GET /mypage/address`
  - 기본 배송지 요약 전용으로 유지
- `GET /mypage/orders`
  - 기존 목록 API 유지
- 배송지 목록/CRUD
  - `/mypage/address` 가 아니라 기존 `/addresses` 계약 유지

### 수정할 것

- `GET /mypage/entries`
  - 기존 endpoint를 확장해서 신청내역 화면 요구사항을 직접 만족시키도록 변경
  - 신규 endpoint를 추가하기보다 기존 endpoint 호환 확장을 우선

### 정책 확인이 필요한 것

- `/mypage/entries` 와 `/mypage/waiting-entries` 의 역할 분리
- 결제 완료된 신청 항목을 신청내역에 계속 남길지 여부
- `PAID` 주문과 신청 row의 귀속 기준

## 핵심 문제

현재 `/mypage/entries` 는 프론트가 신청내역 화면을 바로 렌더링하기에 정보가 부족하다.

부족한 점은 아래와 같다.

- 상태가 `statusDisplayValue` 같은 한글 표시문구 중심이라 프론트 분기 기준으로 쓰기 어렵다.
- row 단위에서 `LOTTERY` / `FIRST_COME` 를 바로 쓰기 위한 구조가 약하다.
- 이벤트 카드에 필요한 메타 정보가 flat 하거나 누락되어 있다.
- 모집 상태, 원천 상태, 최종 표시 상태가 분리되어 있지 않다.
- 결제 필요/주문 연결 상태를 표현할 수 있는 `orderId`, `deadlineAt`, `statusDescription` 같은 필드가 없다.

즉, 신청내역 화면은 현재 "문자열 중심 API"이고, 프론트가 원하는 것은 "코드 중심 화면 API"이다.

## 백엔드 대응 방향

### 1. `/mypage/entries` 를 호환 확장한다

- 기존 response envelope 는 유지한다.
  - `filter`
  - `counts`
  - `page`
  - `size`
  - `totalCount`
  - `hasNext`
  - `emptyState`
  - `items`
- 기존 필드는 제거하지 않는다.
- `items` 내부에 구조화 필드를 추가한다.

이 방식이면:

- 기존 소비자와의 호환성을 최대한 유지할 수 있고
- 프론트 신규 화면도 같은 endpoint에서 필요한 값을 받을 수 있다.

### 2. 코드와 라벨을 함께 제공한다

표시 문자열만 주지 않고 아래 3단계를 분리한다.

- 이벤트 진행 상태
- 원천 신청 상태
- 최종 화면 표시 상태

권장 구조:

- `recruitmentStatusCode`, `recruitmentStatusLabel`
- `rawStatusCode`, `rawStatusLabel`
- `displayStatusCode`, `displayStatusLabel`
- `statusDescription`

### 3. 이벤트/선택/일정 정보를 row에 묶어서 내려준다

신청내역 화면은 여러 API를 조합하기보다 row 하나에 필요한 화면 정보를 한 번에 받는 쪽이 적절하다.

권장 하위 구조:

- `event`
- `selection`
- `schedule`
- `status`
- `action`

## `/mypage/entries` 확장안

### Query Parameter

기존 계약 유지

| 필드 | 설명 | 값 |
| --- | --- | --- |
| `filter` | 신청 방식 필터 | `ALL`, `LOTTERY`, `FIRST_COME` |
| `page` | 페이지 번호 | `0-based` |
| `size` | 페이지 크기 | 기존 유지 |

선택 확장 가능

| 필드 | 설명 | 비고 |
| --- | --- | --- |
| `includeCounts` | 상단 카운트 포함 여부 | 기본 `true` |

### items 확장 필드

기존 item에 아래 필드를 추가하는 방향을 권장한다.

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `orderId` | `Long \| null` | 연결 주문 ID |
| `event` | `Object` | 이벤트 기본 정보 |
| `selection` | `Object` | 선택 코스/페이스 정보 |
| `schedule` | `Object` | 신청/모집/결과/마감 일정 |
| `status` | `Object` | 코드/라벨/보조문구 |
| `action` | `Object \| null` | 버튼/이동 정보 |

### event

| 필드 | 설명 |
| --- | --- |
| `id` | 이벤트 ID |
| `title` | 이벤트명 |
| `typeCode` | `EventType` code |
| `typeLabel` | `EventType.description` |
| `appTypeCode` | `LOTTERY`, `FIRST_COME` |
| `appTypeLabel` | 현재 description 사용 |
| `regionCode` | `EventRegion` code |
| `regionLabel` | `EventRegion.description` |
| `venue` | 장소 |
| `eventAt` | 행사 시각 |
| `thumbnailUrl` | 대표 썸네일 |

### selection

| 필드 | 설명 |
| --- | --- |
| `courseId` | 코스 ID |
| `courseName` | 코스명 |
| `distanceMeter` | 거리(원천 존재 시) |
| `distanceLabel` | 예: `10km` |
| `paceId` | 페이스 ID |
| `paceLabel` | 예: `5'30" ~ 6'30"/km` |
| `price` | 선택 기준 가격 |

### schedule

| 필드 | 설명 |
| --- | --- |
| `appliedAt` | 신청/응모/대기 등록 시각 |
| `applicationStartAt` | 모집 시작 |
| `applicationEndAt` | 모집 종료 |
| `resultAt` | 결과 발표 시각 |
| `ticketOpenAt` | 선착 오픈 시각 |
| `deadlineAt` | 결제/행동 마감 시각 |

### status

| 필드 | 설명 |
| --- | --- |
| `recruitmentStatusCode` | 이벤트 진행 상태 code |
| `recruitmentStatusLabel` | 이벤트 진행 상태 label |
| `rawStatusCode` | 원천 신청 상태 code |
| `rawStatusLabel` | 원천 신청 상태 label |
| `displayStatusCode` | 최종 화면 상태 code |
| `displayStatusLabel` | 최종 화면 상태 label |
| `statusDescription` | 보조 문구 |

### action

| 필드 | 설명 |
| --- | --- |
| `actionCode` | 액션 code |
| `actionLabel` | 버튼 라벨 |
| `enabled` | 버튼 활성 여부 |
| `targetType` | 이동 대상 타입 |
| `targetId` | 이동 대상 ID |

## 코드 체계

새로운 code를 별도로 invent 하기보다 현재 프로젝트 enum을 재사용한다.

### appTypeCode

- `LOTTERY`
- `FIRST_COME`

### recruitmentStatusCode

현재 `EventStatus` 사용

- `READY`
- `IN_PROGRESS`
- `CLOSING_SOON`
- `END`
- `DRAW_COMPLETED`

### rawStatusCode

현재 `EntryStatus` 사용

- `PRE_SAVED`
- `RESERVED`
- `APPLIED`
- `WON`
- `LOST`

### displayStatusCode

현재 `ApplyDisplayStatus` 사용

- `PRE_ENTRY_SAVED`
- `WAITING_TO_APPLY`
- `AVAILABLE_TO_APPLY`
- `ENTRY_CLOSED`
- `ENTRY_UNAVAILABLE`
- `RESERVED`
- `LOTTERY_APPLIED`
- `RESULT_PENDING`
- `RESULT_CHECK_REQUIRED`
- `ENTRY_APPLIED`
- `WON`
- `LOST`

### actionCode

현재 `ApplyActionType` 사용

- `NONE`
- `EDIT`
- `APPLY`
- `CHECKOUT`

## waiting-entries 처리 방안

### 현재 문제

- 문서상으로는 `entries` 와 `waitingEntries` 가 분리되어 있다.
- 그러나 실제 화면 요구는 신청내역 한 화면 안에서 `신청 대기`, `신청 가능`, `신청 완료`, `당첨`, `결제하기` 등을 함께 보여주려는 방향에 가깝다.

### 백엔드 권장안

- 단기:
  - `/mypage/waiting-entries` 는 유지
  - `/mypage/entries` 확장을 우선 진행
- 중기:
  - 정책 확정 후 역할을 둘 중 하나로 정리

정리 가능한 방향은 두 가지다.

- A안
  - `/mypage/entries` 는 화면 전용 통합 목록 성격으로 확장
  - `/mypage/waiting-entries` 는 별도 화면 또는 legacy 용도로 유지
- B안
  - `/mypage/waiting-entries` 는 `FIRST_COME + PRE_SAVED/RESERVED` 전용으로 축소
  - 화면은 점진적으로 `/mypage/entries` 중심으로 이동

## orders 처리 방안

- `/mypage/orders` 는 기존 계약 유지
- 필터는 현재처럼 `tab=ALL|PENDING|COMPLETED|CANCELLED`
- 프론트가 신청내역 row에서 주문 상세 이동을 하려면 `orderId` 또는 `orderNumber` 연결 정보가 필요하다.

추가로 정책 확정이 필요한 부분:

- `PAID` 주문이 붙은 신청 row를 `/mypage/entries` 에 남길지
- 남긴다면 최종 상태를 `displayStatusCode` 상에서 어떻게 표현할지
- 제거한다면 신청내역과 주문내역 사이의 사용자 경험을 어떻게 연결할지

## 구현 작업 목록

### 작업 1. `/mypage/entries` 확장 DTO 설계

- `MyPageApplicationHistoryItemDto` 확장
- 하위 구조 `event`, `selection`, `schedule`, `status`, `action` 추가
- 기존 필드 유지 여부 결정

이유:

- 현재 DTO는 화면에 필요한 메타 정보와 code 구조가 부족하다.

### 작업 2. 신청내역 상태 계산 구조 확장

- `ApplyDisplayStatusResolver` 결과를 code/label 구조로 노출
- 이벤트 상태, entry 원천 상태, display 상태를 함께 응답에 포함

이유:

- 프론트는 더 이상 표시 문자열로 분기하면 안 되고, code 기반 분기가 필요하다.

### 작업 3. 신청내역 row projection 확장

- event title/appType/region/venue/eventAt/thumbnail
- selection course/pace/price
- schedule appliedAt/applicationStartAt/applicationEndAt/resultAt/deadlineAt
- orderId 연결

이유:

- 프론트가 이벤트 상세 API를 추가 호출해 조립하지 않도록 row 단위 응답으로 제공해야 한다.

### 작업 4. `/mypage/waiting-entries` 정책 정리

- 유지/축소/legacy 여부를 결정
- 결정 내용에 맞춰 문서/테스트 정리

이유:

- 현재 화면 요구와 기존 분리형 계약이 충돌한다.

### 작업 5. 계약 테스트 수정

- `MyPageApiContractTest`
- `MyPageControllerTest`

이유:

- 이번 변경은 응답 계약 변경이므로 테스트 고정이 필수다.

### 작업 6. 문서 정리

- `docs/mypage-contract-reconfirmation.md`
- `docs/mypage-implementation-status-mapping.md`

이유:

- 현재도 문서와 구현 사이에 해석 차이가 있어, 코드만 바꾸면 다시 혼선이 생긴다.

## 우선순위

1. `/mypage/entries` 확장 범위 확정
2. `waiting-entries` 역할 정책 확정
3. DTO/서비스/테스트 수정
4. 문서 반영

## 백엔드 입장에서 지금 바로 확인받아야 할 질문

1. `PAID` 주문이 연결된 신청 row를 `/mypage/entries` 에 계속 남길 것인가
2. `/mypage/waiting-entries` 를 장기적으로 유지할 것인가
3. `displayStatusCode` 는 현재 `ApplyDisplayStatus` 를 그대로 노출할지, 외부 공개용 별도 enum을 둘지
4. 신청 row 이동 기준으로 `orderId` 만 필요할지, `orderNumber` 도 함께 필요할지

## 최종 정리

백엔드가 해야 할 일은 "마이페이지를 통합 API에서 분리"하는 것이 아니다.
그 부분은 이미 상당 부분 되어 있다.

실제 해야 할 일은:

- overview 는 overview 로 유지하고
- 신청내역 화면 요구사항을 만족하도록 기존 `/mypage/entries` 를 확장하고
- `waiting-entries` 와 `orders` 의 경계를 정책적으로 정리하고
- 프론트가 문자열이 아니라 code 기반으로 안전하게 렌더링할 수 있는 계약으로 바꾸는 것이다.
