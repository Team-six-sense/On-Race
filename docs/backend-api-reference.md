# Backend API Reference

기준일: 2026-03-24

현재 코드베이스의 `auth` / `main` 백엔드에 존재하는 API를 컨트롤러 기준으로 정리한 문서다.
실제 응답은 대부분 `ApiResponse<T>` envelope로 반환된다.

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": null,
  "data": {}
}
```

## 공통 규칙

- 로그인 사용자 식별은 주로 `X-User-Id` 헤더를 사용한다.
- `X-User-Id`는 외부 클라이언트가 임의로 넣는 값이 아니라, 게이트웨이/인증 필터가 내부 신뢰 구간에서 주입하는 값으로 간주한다.
- 서비스 간 내부 호출에는 `X-Gateway-Token`이 필요할 수 있다.
- 로그아웃/회원탈퇴는 `Authorization: Bearer <token>` 헤더를 함께 사용한다.
- 일부 응답은 `CursorResponse<T>`를 `ApiResponse` 안에 담아 반환한다.

## Auth API

### 인증/회원

| Method | Path | Header | Request | Response | 설명 |
| --- | --- | --- | --- | --- | --- |
| `GET` | `/check-email` | - | query: `email` | `Boolean` | 이메일 중복 확인 |
| `POST` | `/signup` | - | `SignupRequest` | `SignupResponse` | 이메일 회원가입 |
| `POST` | `/login` | - | `LoginRequest` | `LoginResponse` | 이메일 로그인 |
| `POST` | `/token/refresh` | - | `TokenRefreshRequest` | `TokenRefreshResponse` | Access Token 재발급 |
| `POST` | `/logout` | `X-User-Id`, `Authorization` | - | `Void` | 로그아웃 |
| `POST` | `/find-email` | - | `FindEmailRequest` | `FindEmailResponse` | SMS 인증 후 이메일 조회 |
| `DELETE` | `/account` | `X-User-Id`, `Authorization` | `WithdrawRequest` | `Void` | 회원탈퇴 |

주요 요청 바디:

- `SignupRequest`
  - `email`
  - `name`
  - `password`
  - `phoneNumber`
  - `termAgreements`
- `LoginRequest`
  - `email`
  - `password`
- `TokenRefreshRequest`
  - `refreshToken`
- `FindEmailRequest`
  - `phoneNumber`
- `WithdrawRequest`
  - `password`

### 계정관리

| Method | Path | Header | Request | Response | 설명 |
| --- | --- | --- | --- | --- | --- |
| `GET` | `/account/me` | `X-User-Id` | - | `AccountMeResponse` | 내 계정 정보 조회 |
| `PATCH` | `/account/me` | `X-User-Id` | `UpdateNameRequest` | `Void` | 이름 변경 |
| `PATCH` | `/account/me/marketing-consent` | `X-User-Id` | `UpdateMarketingConsentRequest` | `Void` | 마케팅 수신 동의 변경 |
| `PATCH` | `/account/me/verification-status` | `X-User-Id` | `UpdateVerificationStatusRequest` | `Void` | 본인인증 상태 변경 |
| `POST` | `/account/password/change-request` | `X-User-Id` | `PasswordChangeRequest` | `Void` | 비밀번호 재설정 링크 발송 |

`AccountMeResponse` 주요 필드:

- `id`
- `email`
- `name`
- `phone`
- `canChangePassword`
- `verificationStatus`
- `marketingConsent`
- `authProvider`
- `status`

계정 정책 메모:

- `canChangePassword`는 현재 `LOCAL` 계정만 `true`
- `verificationStatus`는 `NOT_STARTED | PENDING | COMPLETED`
- `marketingConsent`는 boolean

### 이메일 인증

| Method | Path | Header | Request | Response | 설명 |
| --- | --- | --- | --- | --- | --- |
| `POST` | `/email/send-code` | - | `EmailSendCodeRequest` | `Void` | 이메일 인증코드 발송 |
| `POST` | `/email/verify-code` | - | `EmailVerifyCodeRequest` | `Void` | 이메일 인증코드 검증 |

요청 필드:

- `EmailSendCodeRequest`
  - `email`
- `EmailVerifyCodeRequest`
  - `email`
  - `code`

### SMS 인증

| Method | Path | Header | Request | Response | 설명 |
| --- | --- | --- | --- | --- | --- |
| `POST` | `/sms/send` | - | `SmsSendRequest` | `Void` | SMS 인증코드 발송 |
| `POST` | `/sms/send-for-find` | - | `SmsSendRequest` | `Void` | 아이디 찾기용 SMS 발송 |
| `POST` | `/sms/verify` | - | `SmsVerifyRequest` | `Void` | SMS 인증코드 검증 |

요청 필드:

- `SmsSendRequest`
  - `phoneNumber`
- `SmsVerifyRequest`
  - `phoneNumber`
  - `code`

### 소셜 로그인

| Method | Path | Header | Request | Response | 설명 |
| --- | --- | --- | --- | --- | --- |
| `POST` | `/oauth/{provider}` | - | `OAuthLoginRequest` | `LoginResponse` | 소셜 로그인 |

요청 필드:

- path: `provider` (`kakao`, `naver`)
- body:
  - `providerId`
  - `email`
  - `name`

### 비밀번호 재설정

| Method | Path | Header | Request | Response | 설명 |
| --- | --- | --- | --- | --- | --- |
| `POST` | `/password/reset-request` | - | `PasswordResetRequest` | `Void` | 비밀번호 재설정 링크 발송 |
| `GET` | `/password/reset-verify` | - | query: `token` | `Void` | 재설정 토큰 검증 |
| `POST` | `/password/reset` | - | `PasswordResetConfirmRequest` | `Void` | 비밀번호 재설정 완료 |

요청 필드:

- `PasswordResetRequest`
  - `email`
- `PasswordResetConfirmRequest`
  - `token`
  - `newPassword`

## Main API

### 이벤트

| Method | Path | Header | Request | Response | 설명 |
| --- | --- | --- | --- | --- | --- |
| `GET` | `/events` | - | `EventSearchRequest` query | `CursorResponse<EventListResponse>` | 이벤트 목록 |
| `GET` | `/events/{eventId}` | - | path: `eventId` | `EventDetailResponse` | 이벤트 상세 |
| `GET` | `/events/{eventId}/info` | - | path: `eventId` | `EventInfoResponse` | 이벤트 요약 정보 |
| `GET` | `/events/{eventId}/sales-info` | - | path: `eventId` | `EventSalesInfoResponse` | 판매/응모 관련 정보 |
| `POST` | `/events/{eventId}/stock/init` | - | path: `eventId` | `Void` | 재고 초기화 |

`EventSearchRequest` 주요 query:

- `type`
- `appType`
- `status`
- `minDistance`
- `maxDistance`
- `eventStartDate`
- `eventEndDate`
- `region`
- `keyword`
- `cursor`
- `size`

### 신청

| Method | Path | Header | Request | Response | 설명 |
| --- | --- | --- | --- | --- | --- |
| `GET` | `/events/{eventId}/entries/overview` | optional `X-User-Id` | path: `eventId` | `EntryOverviewResponse` | 신청 화면 개요 |
| `POST` | `/events/{eventId}/entries/pre-save` | `X-User-Id` | `EntryCoursePaceRequest` | `EntryPreSaveResponse` | 신청 임시 저장 |
| `GET` | `/events/{eventId}/entries/rate` | - | query: `courseId`, `paceId` | `EntryRateResponse` | 경쟁률 조회 |
| `DELETE` | `/events/{eventId}/entries/pre-save` | `X-User-Id` | path: `eventId` | `Long` | 임시 저장 삭제 |
| `POST` | `/events/{eventId}/entries/apply` | `X-User-Id` | `EntryCoursePaceRequest` | `EntryApplyResponse` | 신청 완료 |
| `POST` | `/events/{eventId}/entries/confirm` | `X-User-Id` | query: `paceId` | `Void` | 예약 확정 임시 API |

`EntryCoursePaceRequest`:

- `courseId`
- `paceId`

주의:

- `/events/{eventId}/entries/confirm`는 코드 주석상 테스트용 임시 API다.

### 주문

| Method | Path | Header | Request | Response | 설명 |
| --- | --- | --- | --- | --- | --- |
| `GET` | `/orders` | `X-User-Id` | query: `tab` | `OrderListResponseDto` | 주문 목록 |
| `GET` | `/orders/{orderNumber}` | `X-User-Id` | path: `orderNumber` | `OrderDetailResponseDto` | 주문 상세 |
| `POST` | `/orders/checkout-info` | `X-User-Id` | `CheckoutPrepareRequestDto` | `CheckoutPrepareResponseDto` | 결제 준비 정보 |
| `POST` | `/orders/checkout` | `X-User-Id` | `CheckoutRequestDto` | `CheckoutResponseDto` | 결제 진행 |

`CheckoutPrepareRequestDto`:

- `eventId`
- `eventCourseId`
- `eventPaceId`
- `addressId`

`CheckoutRequestDto`:

- `prepareToken`
- `eventId`
- `eventCourseId`
- `eventPaceId`
- `selectedPackageIds`
- `expectedFinalAmount`
- `addressId`
- `recipientName`
- `recipientPhone`
- `zipCode`
- `address`
- `detailAddress`
- `deliveryMemo`

현재 범위 메모:

- 주문/결제는 코드상 API는 존재하지만, 현재 MyPage reduced scope의 핵심 범위는 아니다.

### 배송지

canonical 경로:

- `/addresses`
- `/api/account/addresses`

호환 경로:

- `/address`

#### canonical API

| Method | Path | Header | Request | Response | 설명 |
| --- | --- | --- | --- | --- | --- |
| `GET` | `/addresses` | `X-User-Id` | - | `List<AddressDto.Response>` | 주소 목록 |
| `GET` | `/addresses/default` | `X-User-Id` | - | `AddressDto.DefaultResponse` | 기본 배송지 조회 |
| `GET` | `/addresses/{id}` | `X-User-Id` | path: `id` | `AddressDto.Response` | 배송지 단건 |
| `POST` | `/addresses` | `X-User-Id` | `AddressDto.SaveRequest` | `AddressDto.Response` | 배송지 추가 |
| `PUT` | `/addresses/{id}` | `X-User-Id` | `AddressDto.SaveRequest` | `AddressDto.Response` | 배송지 전체 수정 |
| `DELETE` | `/addresses/{id}` | `X-User-Id` | path: `id` | `Void` | 배송지 삭제 |
| `PATCH` | `/addresses/{id}/default` | `X-User-Id` | path: `id` | `Void` | 기본 배송지 지정 |

#### compatibility API

| Method | Path | Header | Request | Response | 설명 |
| --- | --- | --- | --- | --- | --- |
| `GET` | `/address` | `X-User-Id` | - | `List<AddressDto.Response>` | 구 경로 호환 목록 |
| `GET` | `/address/default` | `X-User-Id` | - | `AddressDto.DefaultResponse` | 구 경로 호환 기본 배송지 |
| `GET` | `/address/{id}` | `X-User-Id` | path: `id` | `AddressDto.Response` | 구 경로 호환 단건 |
| `POST` | `/address` | `X-User-Id` | `AddressDto.SaveRequest` | `AddressDto.Response` | 구 경로 호환 생성 |
| `PUT` | `/address/{id}` | `X-User-Id` | `AddressDto.SaveRequest` | `AddressDto.Response` | 구 경로 호환 수정 |
| `DELETE` | `/address/{id}` | `X-User-Id` | path: `id` | `Void` | 구 경로 호환 삭제 |
| `PATCH` | `/address/{id}` | `X-User-Id` | path: `id` | `Void` | 구 경로 호환 기본 배송지 지정 |

`AddressDto.SaveRequest`:

- `receiverName`
- `phone`
- `zipcode`
- `address1`
- `address2`
- `memo`
- `isDefault`
- `label`

주소 정책 메모:

- 최대 10개
- 기본 배송지 1개 유지
- 기본 삭제 시 자동 승격
- canonical endpoint는 `/addresses`

### 마이페이지

| Method | Path | Header | Request | Response | 설명 |
| --- | --- | --- | --- | --- | --- |
| `GET` | `/mypage` | `X-User-Id` | - | `MyPageOverviewResponseDto` | 마이페이지 개요 |
| `GET` | `/mypage/account` | `X-User-Id` | - | `MyPageAccountResponse` | 계정 화면용 집계 응답 |
| `GET` | `/mypage/entries` | `X-User-Id` | query: `filter`, `page`, `size` | `MyPageApplicationHistoryResponseDto` | 신청내역 reduced-scope 계약 |
| `GET` | `/mypage/waiting-entries` | `X-User-Id` | query: `page`, `size` | `MyPageEntryListResponseDto` | 대기 신청내역 |
| `GET` | `/mypage/orders` | `X-User-Id` | query: `tab`, `page`, `size` | `MyPageOrderListResponseDto` | 마이페이지 주문 목록 |
| `GET` | `/mypage/orders/{orderNumber}` | `X-User-Id` | path: `orderNumber` | `MyPageOrderDetailResponseDto` | 마이페이지 주문 상세 |
| `GET` | `/mypage/address` | `X-User-Id` | - | `MyPageAddressResponseDto` | 기본 배송지 요약 |

`GET /mypage/account` 주요 필드:

- `name`
- `email`
- `phone`
- `canChangePassword`
- `verificationStatus`
- `marketingConsent`

`GET /mypage/entries` query:

- `filter=ALL|LOTTERY|FIRST_COME`
- `page`
- `size`

현재 범위 메모:

- `/mypage/account`는 `main`이 집계하고 원천 데이터는 `auth`에서 읽는다.
- `/mypage/entries`는 현재 reduced scope의 기준 API다.
- `/mypage/address`는 주소 목록이 아니라 기본 배송지 요약이다.
- 주소 목록/CRUD는 `/addresses`를 사용한다.

### 미디어

| Method | Path | Header | Request | Response | 설명 |
| --- | --- | --- | --- | --- | --- |
| `POST` | `/media/presign-upload` | `X-User-Id` | `PresignUploadRequestDto` | `PresignUploadResponseDto` | 업로드용 presigned URL 발급 |
| `POST` | `/media/confirm` | `X-User-Id` | `ConfirmUploadRequestDto` | `ConfirmUploadResponseDto` | 업로드 완료 확정 |

요청 필드:

- `PresignUploadRequestDto`
  - `filename`
  - `contentType`
- `ConfirmUploadRequestDto`
  - `mediaId`

### 내부 멤버 동기화

| Method | Path | Header | Request | Response | 설명 |
| --- | --- | --- | --- | --- | --- |
| `POST` | `/internal/members/{userId}/sync-create` | 내부 호출 | path: `userId` | `Void` | auth -> main 회원 생성 동기화 |
| `POST` | `/internal/members/{userId}/sync-delete` | 내부 호출 | path: `userId` | `Void` | auth -> main 회원 삭제 동기화 |

## 현재 운영 관점 메모

- MyPage 현재 핵심 화면은 `GET /mypage/account` + `GET /mypage/entries` + `/addresses` 조합이다.
- 신청내역 화면에서는 주문/결제 액션을 일부러 축소한 reduced scope 계약을 사용한다.
- `auth`는 계정 원천 데이터, `main`은 MyPage 집계 책임을 가진다.
- `/address`는 호환 경로이고, 신규 연결은 `/addresses`를 우선한다.
