# 마이페이지 계약 재확정 문서

- 작성일: 2026-03-25
- 상태: 재구현 전 기준 계약 문서 (정책 반영, aggregate 반영)
- 목적: 마이페이지 재구현 전에 각 도메인에서 무엇을 받아야 하는지 먼저 못 박아서 DTO 재작업과 책임 혼선을 막는다.
- 범위: 계정관리, 기본 배송지, 신청내역, 주문내역의 읽기 계약과 현재 MVP 화면 표시 계약
- 비범위: 본인인증 실제 처리, 전화번호 변경/재인증, 실제 결제/취소/환불/교환 처리

## 왜 이 문서를 먼저 고정하는가

- 계정관리 화면은 이름, 전화번호, 비밀번호 변경 가능 여부, 본인인증 상태, 마케팅 수신 여부, 기본 배송지를 함께 다룬다.
- 배송지는 별도 정책과 CRUD가 이미 정리된 재사용 영역이다.
- 신청내역과 주문내역은 원천 데이터와 화면 표시용 상태 계산이 분리되어야 한다.
- 예전 구현이 유실된 상황에서 다시 시작하려면 코드보다 계약을 먼저 고정해야 같은 혼선을 반복하지 않는다.

## 기준과 우선순위

- 정책 문서와 현재 구현을 함께 보되, 1차 계약은 현재 채택한 공개 API와 계약 테스트를 기준으로 고정한다.
- 정책과 기존 초안이 충돌할 때는 이번 재구현에서 실제 채택한 결정을 문서와 테스트에 같이 반영한다.
- 이 문서에서 고정하는 현재 1차 기준은 아래와 같다.
  - `GET /mypage/account`는 auth 원천값과 main/address 기본 배송지를 조합하는 공식 aggregate API다.
  - `GET /mypage/account` 공개 응답은 `accountType`, `authProvider`, `email`, `name`, `phone`, `canChangePassword`, `verificationStatus`, `marketingConsent`, `address`를 포함한다.
  - `accountType` 공개 계약은 `EMAIL`, `SNS`다.
  - `verificationStatus` 공개 계약은 `PENDING`, `COMPLETED`, `FAILED`다.
  - 신청내역은 `/mypage/entries`와 `/mypage/waiting-entries`의 분리형을 유지한다.
  - `entries`는 결제 완료 주문이 이미 존재하는 신청을 제외한다.
  - 주문 목록 탭은 `ALL`, `PENDING`, `COMPLETED`, `CANCELLED`를 유지한다.
  - `GET /mypage` overview는 현재 MVP 범위에 포함한다.
  - 리스트 페이지는 0-based `page`를 사용하고 목록 기본 `size`는 20이다.

## 계약 원칙

1. `/mypage/account`처럼 aggregate DTO를 제공하더라도 필드별 원천 책임은 문서에서 고정한다.
2. `auth`는 계정 원천값을 책임진다.
   - 이름, 전화번호, 마케팅 수신 여부, 본인인증 상태, 비밀번호 변경 가능 여부
3. `main`은 마이페이지 집계값과 화면 표시용 계산값을 책임진다.
   - 기본 배송지, 신청내역 목록, 주문내역 목록, 화면 표시용 상태와 액션
4. 화면 표시용 `status`, `actionType`, `actionLabel`, `actionEnabled`는 프론트가 임의 계산하지 않고 `main/mypage`가 결정한다.
5. fallback은 이 문서에 명시된 경우에만 허용한다. 명시되지 않은 값은 프론트에서 추론하지 않는다.

## 책임 분리 요약

| 구분 | 원천 도메인 | 값 | 책임 |
| --- | --- | --- | --- |
| Auth 원천 | `auth/account` | `email`, `authProvider`, `name`, `phone`, `marketingConsent`, `verificationStatus`, `canChangePassword` | Auth |
| Main 집계 | `main/address` | `hasAddress`, `defaultAddress.*` | Main Address |
| Main aggregate | `main/mypage` + `auth/account` + `main/address` | `/mypage/account` 공개 응답, `accountType`, 공개용 `verificationStatus` 정규화 | Main MyPage |
| Main 집계 + 계산 | `main/entry`, `main/event`, `main/mypage` | 신청내역 목록, 대기 신청내역 목록, 표시 상태/액션 | Main MyPage |
| Main 집계 + 계산 | `main/order`, `main/event`, `main/mypage` | 주문내역 목록, 표시 상태/액션 | Main MyPage |

## 현재 MVP API Surface

- 읽기
  - `GET /account/me`
  - `GET /mypage/account`
  - `GET /mypage`
  - `GET /mypage/address`
  - `GET /mypage/entries`
  - `GET /mypage/waiting-entries`
  - `GET /mypage/orders`
  - `GET /mypage/orders/{orderNumber}`
- 쓰기
  - `PATCH /account/me`
  - `PATCH /account/me/marketing-consent`
  - `POST /account/password/change-request`
  - `GET /addresses`
  - `GET /addresses/default`
  - `POST /addresses`
  - `PUT /addresses/{id}`
  - `DELETE /addresses/{id}`
  - `PATCH /addresses/{id}/default`
- 현재 MVP 비범위
  - `verificationStatus` 변경 API
  - 전화번호 변경 및 재인증 API
  - 신청내역 통합 혼합 목록 API
  - 정책 기준 확장 필드인 `statusLabel`, `detailType`, `eventDeleted`, `timeline`, `applicationPeriod`

## 하위 영역 계약

### 1. account

- 마이페이지 계정 요약 조회: `GET /mypage/account`
- main 내부 원천:
  - auth 원천값: `GET /account/me`
  - 기본 배송지 요약: address 저장소 조회
- 수정 책임:
  - 이름 변경: `PATCH /account/me`
  - 마케팅 수신 변경: `PATCH /account/me/marketing-consent`
  - 비밀번호 변경 요청: `POST /account/password/change-request`

| 필요한 필드 | 원천 도메인 | 현재 존재 여부 | fallback 가능 여부 | 책임 | 비고 |
| --- | --- | --- | --- | --- | --- |
| `accountType` | Main MyPage aggregate 계산값 | 존재 | 불가 | Main MyPage | `authProvider`를 공개용 `EMAIL/SNS`로 정규화 |
| `email` | Auth `User.email` | 존재 | 불가 | Auth Account | 읽기 전용 |
| `authProvider` | Auth `User.authProvider` | 존재 | 불가 | Auth Account | 현재 raw enum `LOCAL`, `KAKAO`, `NAVER` |
| `name` | Auth `User.name` | 존재 | 불가 | Auth Account | 계정관리 화면 표시 및 수정 대상 |
| `phone` | Auth `User.phoneNumber` -> 응답 필드 `phone` | 존재 | `null` 허용 | Auth Account | 현재는 조회만 가능, 변경 계약은 별도 |
| `canChangePassword` | Auth 계산값 | 존재 | `false` 허용 | Auth Account | OAuth 사용자는 `false`일 수 있음 |
| `verificationStatus` | Main MyPage aggregate 계산값 | 존재 | 기본값 `PENDING` | Main MyPage | auth raw 값을 공개용 `PENDING/COMPLETED/FAILED`로 정규화 |
| `marketingConsent` | Auth `User.marketingConsent` | 존재 | 기본값 `false` | Auth Account | 수정 API 존재 |
| `address.hasAddress` | Main Address 집계값 | 존재 | 주소 없으면 `false` | Main Address + MyPage | 계정관리 화면의 기본 배송지 존재 플래그 |
| `address.defaultAddress.*` | Main `Address` | 존재 | 주소 없으면 `null` | Main Address + MyPage | 기본 배송지 요약 구조 재사용 |

#### account 계약 결정

- 계정관리 화면의 계정 원천값은 `auth`를 단일 진실 공급원으로 본다.
- `GET /mypage/account`는 1차 공식 aggregate API로 채택한다.
- `main`은 auth 원천값을 임의로 복제하지 않고, 화면 계약에 필요한 정규화와 기본 배송지 조합만 수행한다.
- `phone`은 현재 응답 필드명이 `phone`이므로, 별도 티켓 없이 `phoneNumber`로 바꾸지 않는다.
- `accountType`은 공개 계약의 기본 필드로 두고 `EMAIL`, `SNS`만 사용한다.
- `authProvider`는 raw 공급자 정보를 보존하는 보조 필드로 유지한다.
- `verificationStatus`는 읽기 전용으로 고정한다. 변경 플로우는 별도 티켓으로 분리한다.
- `verificationStatus`는 공개 계약에서 `PENDING`, `COMPLETED`, `FAILED`를 사용한다.
- auth raw 상태는 아래처럼 매핑한다.
  - `UNVERIFIED -> PENDING`
  - `VERIFIED -> COMPLETED`
  - `FAILED -> FAILED`
- 전화번호 변경 및 재인증 플로우도 별도 티켓으로 분리한다.

### 2. default address

- 마이페이지 조회 원천: `GET /mypage/address`
- 주소 도메인 원천 API:
  - `GET /addresses`
  - `GET /addresses/default`
  - `POST /addresses`
  - `PUT /addresses/{id}`
  - `DELETE /addresses/{id}`
  - `PATCH /addresses/{id}/default`

| 필요한 필드 | 원천 도메인 | 현재 존재 여부 | fallback 가능 여부 | 책임 | 비고 |
| --- | --- | --- | --- | --- | --- |
| `hasAddress` | Main Address 집계값 | 존재 | 주소 없으면 `false` | Main Address + MyPageQuery | 마이페이지 요약/계정관리 공통 플래그 |
| `defaultAddress.addressId` | Main `Address.id` | 존재 | 주소 없으면 `defaultAddress=null` | Main Address | |
| `defaultAddress.label`, `receiverName`, `phone`, `zipcode`, `address1`, `address2`, `memo`, `isDefault` | Main `Address` | 존재 | `address2`, `memo`는 `null` 허용 | Main Address | 화면 표시용 기본 배송지 정보 |

#### default address 계약 결정

- 기본 배송지는 `auth`가 아니라 `main/address`가 책임진다.
- 마이페이지는 읽기 계약으로 `GET /mypage/address`를 사용한다.
- 주소 CRUD와 기본 배송지 변경은 `AddressController` 계약을 그대로 재사용한다.
- 기본 배송지 플래그가 비정상적으로 비어 있어도 활성 주소가 하나 이상 있으면 최신 주소를 기본 배송지로 복구한다.
- 활성 주소가 없으면 `hasAddress=false`, `defaultAddress=null`로 고정한다.

### 3. apply history

- 조회 원천:
  - `GET /mypage/entries`
  - `GET /mypage/waiting-entries`
- 의미:
  - `entries`: 완료/결과 확인 대상 신청 이력
  - `waitingEntries`: 사전정보/예약 상태 신청 이력
- 현재 MVP 목록 계약:
  - `page`는 0-based다.
  - 별도 `sort`, `eventType` 쿼리 파라미터는 없다.
  - item 구조는 현재 flat DTO를 유지한다.
  - `statusLabel`, `detailType`, `eventDeleted`, `selectedOption`, `applicationPeriod`, `ticketOpenAt`, `paymentDeadlineAt` 같은 정책 기준 확장 필드는 1차 범위에 넣지 않는다.

| 필요한 필드 | 원천 도메인 | 현재 존재 여부 | fallback 가능 여부 | 책임 | 비고 |
| --- | --- | --- | --- | --- | --- |
| `page`, `size`, `totalCount`, `hasNext` | Main MyPage 페이징 계산값 | 존재 | 빈 목록이면 `0/false` | Main MyPage | 요약 화면은 기본 size 3, 목록은 기본 size 20 |
| `items[].entryId`, `eventId` | Main Entry / Event | 존재 | 불가 | Main Entry | 식별자 |
| `items[].status`, `actionType`, `actionLabel`, `actionEnabled` | Main MyPage 표시 계산값 | 존재 | 프론트 임의 계산 불가 | Main MyPage | `MyPageDisplayStatusResolver`가 결정 |
| `items[].thumbnailUrl` | Main Event/Media | 부분 존재 | `null` 허용 | Main Event + Media | 현재 백엔드는 항상 `null` |
| `items[].title`, `courseName`, `paceName` | Main Event / Course / Pace | 존재 | `courseName`, `paceName`은 `null` 허용 | Main Event | |
| `items[].price` | Main EventCourse | 존재 | `null` 허용 | Main Event | 현재 코스 가격 사용 |
| `items[].appliedAt` | Main Entry `createdAt` | 존재 | 불가 | Main Entry | |
| `items[].resultAt` | Main Event `lotteryAnnouncedAt` | 존재 | `null` 허용 | Main Event | 추첨형이 아니면 `null` 가능 |

#### apply history 계약 결정

- 신청내역의 표시 상태는 원천 enum을 그대로 내리지 않고, `main/mypage`가 화면 상태로 해석한다.
- 현재 공개 상태값은 enum 정규화가 아니라 한글 상태 문구 문자열이다.
- 현재 공개 action 값은 `NONE`, `EDIT`, `APPLY`, `CHECKOUT`을 사용한다.
- `entries`는 `APPLIED`, `WON`, `LOST`를 대상으로 한다.
- `waitingEntries`는 `PRE_SAVED`, `RESERVED`를 대상으로 한다.
- 결제 완료 주문이 이미 존재하는 신청은 `entries`에서 제외한다.
- `thumbnailUrl`은 필드는 유지하되 현재는 nullable 계약으로 고정한다.
- 정책 문서의 혼합 목록, 결제 완료 유지, 세분화된 공개 status enum은 후속 확장 대상으로 남긴다.

### 4. order history

- 조회 원천: `GET /mypage/orders`
- 현재 목록 필터: `ALL`, `PENDING`, `COMPLETED`, `CANCELLED`
- 현재 MVP 목록 계약:
  - `page`는 0-based다.
  - 기본 `tab`은 `ALL`이다.
  - item 구조는 현재 flat DTO를 유지한다.
  - `statusLabel`, `detailType`, `eventDeleted`, `timeline`, `eventType` 같은 정책 기준 확장 필드는 1차 범위에 넣지 않는다.

| 필요한 필드 | 원천 도메인 | 현재 존재 여부 | fallback 가능 여부 | 책임 | 비고 |
| --- | --- | --- | --- | --- | --- |
| `page`, `size`, `totalCount`, `hasNext` | Main MyPage 페이징 계산값 | 존재 | 빈 목록이면 `0/false` | Main MyPage | |
| `items[].orderNumber` | Main Order | 존재 | 불가 | Main Order | 식별자 |
| `items[].eventId` | Main Event 연관 조회 | 존재 | `null` 허용 | Main Order + Event | 코스-이벤트 연결이 없으면 `null` 가능 |
| `items[].status`, `actionType`, `actionLabel`, `actionEnabled` | Main MyPage 표시 계산값 | 존재 | 프론트 임의 계산 불가 | Main MyPage | 현재는 모두 `DETAIL` 또는 읽기 상태 |
| `items[].thumbnailUrl` | Main Event/Media | 부분 존재 | `null` 허용 | Main Event + Media | 현재 백엔드는 항상 `null` |
| `items[].title`, `courseName`, `paceName` | Main Event / Course / Pace | 존재 | `null` 허용 | Main Order + Event | |
| `items[].finalAmount` | Main Order 스냅샷 | 존재 | 불가 | Main Order | |
| `items[].orderedAt` | Main Order `createdAt` | 존재 | 불가 | Main Order | |
| `items[].paymentDeadlineAt` | Main Order 결제 마감 원천 | 부분 존재 | 현재는 항상 `null` | Main Order | 실제 원천값 부재 |

#### order history 계약 결정

- 주문내역의 표시 상태는 원천 `OrderStatus`를 그대로 쓰지 않고 `main/mypage`가 화면 상태로 해석한다.
- 현재 공개 상태값은 enum 정규화가 아니라 한글 상태 문구 문자열이다.
- 현재 공개 action 값은 `DETAIL`만 사용한다.
- 주문 목록은 Order 스냅샷 금액을 사용한다. 프론트에서 재계산하지 않는다.
- `paymentDeadlineAt` 필드는 유지하되 현재는 nullable 계약으로 고정한다.
- 주문 상세 `GET /mypage/orders/{orderNumber}`는 별도 상세 계약으로 본다. 이 문서는 목록 계약을 우선 고정한다.
- 정책 기준의 `PAYMENT_PENDING`, `PAYMENT_COMPLETED` 탭 정규화와 확장 상태 표준화는 후속 확장 대상으로 남긴다.

## 현재 gap 정리

| 항목 | 현재 상태 | 처리 원칙 |
| --- | --- | --- |
| 계정 원천값과 마이페이지 집계값을 한 API에서 내려주는 통합 계약 | `GET /mypage/account`로 구현됨 | 1차 공식 aggregate API로 유지하고 문서/테스트를 동일 계약으로 고정한다 |
| `verificationStatus` 변경 API | 부재 | 읽기 전용으로 유지하고 별도 티켓으로 분리 |
| 전화번호 변경 API | 부재 | 조회 계약만 먼저 고정하고 변경 플로우는 별도 티켓으로 분리 |
| 신청/주문 `thumbnailUrl` 실제 값 | 부재 | nullable 유지, 프론트 플레이스홀더 사용 가능 |
| 주문 `paymentDeadlineAt` 실제 값 | 부재 | nullable 유지, 프론트가 자체 계산하지 않음 |
| 정책 문서 기준 확장 필드와 현재 MVP DTO 차이 | 존재 | 현재 MVP DTO를 유지하고 확장 필드는 후속 티켓으로 분리 |

## 재구현 전에 못 박는 최종 결정

1. 계정 원천값은 `auth`, 마이페이지 집계값은 `main`이 책임진다.
2. 계정관리 화면은 `GET /mypage/account` aggregate API를 사용한다.
3. 계정관리 화면의 기본 배송지는 `auth` 응답에 섞지 않고 `main/address`에서 읽는다.
4. `accountType`은 공개 계약에서 `EMAIL/SNS`, `verificationStatus`는 `PENDING/COMPLETED/FAILED`로 고정한다.
5. `authProvider`는 raw 공급자 정보 보존용 보조 필드로 유지한다.
6. 신청내역과 주문내역의 표시 상태/버튼은 프론트가 직접 계산하지 않는다.
7. 현재 원천이 없는 값은 임시 문자열이 아니라 `null` 또는 빈 응답으로 고정한다.
8. 읽기 계약과 쓰기 계약을 분리한다.
   - 읽기: 이 문서의 범위
   - 쓰기: 이름 변경, 마케팅 수신 변경, 주소 CRUD/기본 설정
9. 정책 문서보다 현재 공개 표면이 더 MVP에 가까운 경우, 1차 계약은 현재 공개 표면을 따른다.
10. 본인인증 상태 변경, 전화번호 변경, 결제 마감 시각, 썸네일 소스, 정책 기준 확장 필드는 후속 티켓으로 분리한다.

## 후속 확장 후보

- 신청/주문 공개 enum 표준화와 별도 `statusLabel`
- `eventDeleted`, `detailType`, `applicationPeriod`, `timeline`, nested `selectedOption`
- 신청내역 단일 혼합 목록 + `전체/추첨/선착` 필터
- 주문 탭의 `PAYMENT_PENDING`, `PAYMENT_COMPLETED` 표준화
- `thumbnailUrl`, `paymentDeadlineAt` 원천 보강

## 근거 코드

- `backend/auth/src/main/java/com/kt/onrace/auth/controller/AccountController.java`
- `backend/auth/src/main/java/com/kt/onrace/auth/service/AccountService.java`
- `backend/auth/src/main/java/com/kt/onrace/auth/dto/AccountMeResponse.java`
- `backend/main/src/main/java/com/kt/onrace/domain/address/controller/AddressController.java`
- `backend/main/src/main/java/com/kt/onrace/domain/address/service/AddressService.java`
- `backend/main/src/main/java/com/kt/onrace/domain/mypage/controller/MyPageController.java`
- `backend/main/src/main/java/com/kt/onrace/domain/mypage/service/MyPageService.java`
- `backend/main/src/main/java/com/kt/onrace/domain/mypage/service/MyPageQueryService.java`
- `backend/main/src/main/java/com/kt/onrace/domain/mypage/service/ApplicationHistoryService.java`
- `backend/main/src/main/java/com/kt/onrace/domain/mypage/service/OrderHistoryService.java`
- `backend/main/src/main/java/com/kt/onrace/domain/mypage/service/MyPageDisplayStatusResolver.java`
- `backend/main/src/test/java/com/kt/onrace/domain/mypage/controller/MyPageApiContractTest.java`
