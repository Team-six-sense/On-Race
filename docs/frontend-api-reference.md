# Frontend API Reference

기준일: 2026-03-24

프론트엔드가 직접 연동하는 API만 추린 문서다.
내부 전용 API와 운영/초기화용 API는 제외했다.

모든 응답은 기본적으로 `ApiResponse<T>` envelope를 사용한다.

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": null,
  "data": {}
}
```

## 공통 규칙

- 로그인 사용자 식별 헤더는 `X-User-Id`
- 로그아웃/회원탈퇴는 `Authorization: Bearer <token>` 필요
- 신규 주소 연동은 `/addresses` 기준으로 붙인다.
- `/address`는 레거시 호환 경로다.
- MyPage 계정/신청내역/주소는 다시 하나의 giant API로 합치지 않는다.

## 현재 MyPage 화면 기준 권장 API

### 계정 화면

- `GET /mypage/account`
- `GET /mypage/address`
- `GET /addresses`
- `POST /addresses`
- `PUT /addresses/{id}`
- `DELETE /addresses/{id}`
- `PATCH /addresses/{id}/default`
- `PATCH /account/me/marketing-consent`
- `PATCH /account/me/verification-status`
- `POST /account/password/change-request`

### 신청내역 화면

- `GET /mypage/entries`

### 이벤트/신청 화면

- `GET /events`
- `GET /events/{eventId}`
- `GET /events/{eventId}/info`
- `GET /events/{eventId}/sales-info`
- `GET /events/{eventId}/entries/overview`
- `POST /events/{eventId}/entries/pre-save`
- `DELETE /events/{eventId}/entries/pre-save`
- `POST /events/{eventId}/entries/apply`
- `GET /events/{eventId}/entries/rate`

## Auth

### 인증/회원

| Method | Path | Request | 핵심 응답 | 메모 |
| --- | --- | --- | --- | --- |
| `GET` | `/check-email` | query: `email` | `Boolean` | 회원가입 전 중복 확인 |
| `POST` | `/signup` | `email`, `name`, `password`, `phoneNumber`, `termAgreements` | 회원가입 결과 | 이메일 회원가입 |
| `POST` | `/login` | `email`, `password` | access/refresh token | 이메일 로그인 |
| `POST` | `/token/refresh` | `refreshToken` | 새 access token | 토큰 재발급 |
| `POST` | `/logout` | 헤더만 사용 | `Void` | 로그아웃 |
| `POST` | `/find-email` | `phoneNumber` | 마스킹 이메일 | 아이디 찾기 |
| `DELETE` | `/account` | `password` | `Void` | 회원탈퇴 |

### 소셜 로그인

| Method | Path | Request | 핵심 응답 | 메모 |
| --- | --- | --- | --- | --- |
| `POST` | `/oauth/{provider}` | `providerId`, `email`, `name` | access/refresh token | `provider`: `kakao`, `naver` |

### 이메일/SMS 인증

| Method | Path | Request | 메모 |
| --- | --- | --- | --- |
| `POST` | `/email/send-code` | `email` | 이메일 인증코드 발송 |
| `POST` | `/email/verify-code` | `email`, `code` | 이메일 인증코드 검증 |
| `POST` | `/sms/send` | `phoneNumber` | SMS 인증코드 발송 |
| `POST` | `/sms/send-for-find` | `phoneNumber` | 아이디 찾기용 SMS 발송 |
| `POST` | `/sms/verify` | `phoneNumber`, `code` | SMS 인증코드 검증 |

### 계정관리

| Method | Path | Request | 핵심 응답 | 메모 |
| --- | --- | --- | --- | --- |
| `GET` | `/account/me` | - | `name`, `email`, `phone`, `canChangePassword`, `verificationStatus`, `marketingConsent` | auth 원천 계정 데이터 |
| `PATCH` | `/account/me` | `name` | `Void` | 이름 변경 |
| `PATCH` | `/account/me/marketing-consent` | `marketingConsent` | `Void` | 마케팅 수신 동의 변경 |
| `PATCH` | `/account/me/verification-status` | `verificationStatus` | `Void` | MVP용 본인인증 상태 변경 |
| `POST` | `/account/password/change-request` | `currentPassword` | `Void` | 비밀번호 변경 링크 발송 |

`verificationStatus` 값:

- `NOT_STARTED`
- `PENDING`
- `COMPLETED`

## MyPage

### 계정

| Method | Path | Request | 핵심 응답 | 메모 |
| --- | --- | --- | --- | --- |
| `GET` | `/mypage/account` | - | `name`, `email`, `phone`, `canChangePassword`, `verificationStatus`, `marketingConsent` | 프론트 계정 화면 전용 집계 API |

### 신청내역

| Method | Path | Request | 핵심 응답 | 메모 |
| --- | --- | --- | --- | --- |
| `GET` | `/mypage/entries` | query: `filter`, `page`, `size` | `filter`, `empty`, `pagination`, `items[]` | reduced scope 기준 API |

`filter` 값:

- `ALL`
- `LOTTERY`
- `FIRST_COME`

`items[]` 주요 필드:

- `entryId`
- `eventId`
- `eventName`
- `applicationType`
- `displayStatus`
- `actionType`
- `actionLabel`
- `deepLink`
- `thumbnailUrl`
- `courseName`
- `paceName`
- `price`
- `appliedAt`
- `eventAt`
- `applicationStartAt`
- `applicationEndAt`
- `resultAnnouncedAt`

주의:

- 이번 릴리즈에서 신청내역 화면은 주문/결제 액션을 적극적으로 사용하지 않는다.
- `deepLink`는 백엔드가 내려준 값을 그대로 사용한다.

### 그 외 MyPage 조회

| Method | Path | Request | 메모 |
| --- | --- | --- | --- |
| `GET` | `/mypage` | - | overview 집계 |
| `GET` | `/mypage/waiting-entries` | query: `page`, `size` | 레거시/보조 조회 |
| `GET` | `/mypage/orders` | query: `tab`, `page`, `size` | 코드상 존재, 현재 reduced scope 핵심 아님 |
| `GET` | `/mypage/orders/{orderNumber}` | - | 코드상 존재, 현재 reduced scope 핵심 아님 |
| `GET` | `/mypage/address` | - | 기본 배송지 요약만 반환 |

## Address

canonical 경로:

- `/addresses`

legacy compatibility 경로:

- `/address`
- `/api/account/addresses`

프론트 신규 연결은 `/addresses` 기준으로 붙인다.

| Method | Path | Request | 핵심 응답 | 메모 |
| --- | --- | --- | --- | --- |
| `GET` | `/addresses` | - | 주소 배열 | 주소 목록 |
| `GET` | `/addresses/default` | - | 기본 배송지 | 기본 배송지 조회 |
| `GET` | `/addresses/{id}` | - | 주소 단건 | 상세 조회 |
| `POST` | `/addresses` | `receiverName`, `phone`, `zipcode`, `address1`, `address2`, `memo`, `isDefault`, `label` | 생성 주소 | 배송지 추가 |
| `PUT` | `/addresses/{id}` | 위와 동일 | 수정 주소 | 전체 수정 |
| `PATCH` | `/addresses/{id}` | 위와 동일 | 수정 주소 | 부분 수정 용도 |
| `PATCH` | `/addresses/{id}/default` | - | `Void` | 기본 배송지 지정 |
| `DELETE` | `/addresses/{id}` | - | `Void` | 배송지 삭제 |

주소 응답 주요 필드:

- `id`
- `label`
- `receiverName`
- `phone`
- `zipcode`
- `address1`
- `address2`
- `memo`
- `isDefault`

별칭 필드도 같이 내려간다:

- `addressId`
- `roadAddress`
- `detailAddress`
- `phoneNumber`

## Event

| Method | Path | Request | 핵심 응답 | 메모 |
| --- | --- | --- | --- | --- |
| `GET` | `/events` | query: `type`, `appType`, `status`, `minDistance`, `maxDistance`, `eventStartDate`, `eventEndDate`, `region`, `keyword`, `cursor`, `size` | cursor 기반 이벤트 목록 | 이벤트 리스트 |
| `GET` | `/events/{eventId}` | - | 이벤트 상세 | 이벤트 상세 페이지 |
| `GET` | `/events/{eventId}/info` | - | 이벤트 기본 정보 | 카드/요약 |
| `GET` | `/events/{eventId}/sales-info` | - | 판매/응모 상태 | 응모/결제 정보 |

## Entry

| Method | Path | Request | 핵심 응답 | 메모 |
| --- | --- | --- | --- | --- |
| `GET` | `/events/{eventId}/entries/overview` | optional `X-User-Id` | 신청 화면 개요 | 로그인 전/후 일부 공용 |
| `POST` | `/events/{eventId}/entries/pre-save` | `courseId`, `paceId` | 임시 저장 결과 | 로그인 필요 |
| `DELETE` | `/events/{eventId}/entries/pre-save` | - | 삭제된 entry id | 로그인 필요 |
| `POST` | `/events/{eventId}/entries/apply` | `courseId`, `paceId` | 신청 완료 결과 | 로그인 필요 |
| `GET` | `/events/{eventId}/entries/rate` | query: `courseId`, `paceId` | 경쟁률 | 공용 |

프론트 신규 연동 비권장:

- `POST /events/{eventId}/entries/confirm`
  - 코드상 테스트용 임시 API

## Order

주문/결제 API는 프론트 호출 가능하지만, 현재 MyPage reduced scope 핵심 범위는 아니다.

| Method | Path | Request | 메모 |
| --- | --- | --- | --- |
| `GET` | `/orders` | query: `tab` | 주문 목록 |
| `GET` | `/orders/{orderNumber}` | - | 주문 상세 |
| `POST` | `/orders/checkout-info` | `eventId`, `eventCourseId`, `eventPaceId`, `addressId` | 결제 준비 정보 |
| `POST` | `/orders/checkout` | checkout body 전체 | 결제 진행 |

## Media

| Method | Path | Request | 메모 |
| --- | --- | --- | --- |
| `POST` | `/media/presign-upload` | `filename`, `contentType` | 업로드 URL 발급 |
| `POST` | `/media/confirm` | `mediaId` | 업로드 완료 확정 |

## 프론트에서 쓰지 말아야 하는 경로

- `/internal/members/**`
- `/events/{eventId}/stock/init`
- `POST /events/{eventId}/entries/confirm` 신규 의존

