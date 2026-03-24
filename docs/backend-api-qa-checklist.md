# Backend API QA Checklist

기준일: 2026-03-24

백엔드 API QA용 체크리스트다.
화면/플로우 기준으로 빠르게 점검할 수 있게 정리했다.

## 공통

- [ ] 모든 성공 응답이 `ApiResponse.success(...)` envelope로 내려온다.
- [ ] 실패 응답이 공통 에러 형식을 유지한다.
- [ ] 로그인 필요 API에서 `X-User-Id` 누락 시 적절한 4xx를 반환한다.
- [ ] `Authorization`이 필요한 API에서 토큰 누락 시 적절한 4xx를 반환한다.
- [ ] 잘못된 path/query/body 값에 대해 validation 에러가 일관되게 내려온다.

## Auth

### 회원가입/로그인

- [ ] `GET /check-email`이 중복 이메일일 때 `true`, 신규 이메일일 때 `false`를 반환한다.
- [ ] `POST /signup`이 정상 회원가입을 처리한다.
- [ ] `POST /signup`에서 잘못된 이메일 형식이 거절된다.
- [ ] `POST /signup`에서 약관 정보 누락이 거절된다.
- [ ] `POST /login`이 정상 로그인 시 access/refresh token을 반환한다.
- [ ] `POST /login`에서 잘못된 비밀번호가 거절된다.
- [ ] `POST /token/refresh`가 refresh token으로 새 access token을 발급한다.
- [ ] `POST /logout`이 access token을 무효화한다.
- [ ] `DELETE /account`가 비밀번호 확인 후 탈퇴 처리된다.

### 이메일/SMS 인증

- [ ] `POST /email/send-code`가 정상 응답을 반환한다.
- [ ] `POST /email/verify-code`가 올바른 코드일 때 성공한다.
- [ ] `POST /sms/send`가 정상 응답을 반환한다.
- [ ] `POST /sms/send-for-find`가 정상 응답을 반환한다.
- [ ] `POST /sms/verify`가 올바른 코드일 때 성공한다.

### 계정관리

- [ ] `GET /account/me`가 `name`, `email`, `phone`, `canChangePassword`, `verificationStatus`, `marketingConsent`를 반환한다.
- [ ] `GET /account/me`에서 `LOCAL` 계정은 `canChangePassword=true`다.
- [ ] `GET /account/me`에서 OAuth 계정은 `canChangePassword=false`다.
- [ ] `PATCH /account/me`로 이름 변경이 반영된다.
- [ ] `PATCH /account/me/marketing-consent`로 마케팅 동의 변경이 반영된다.
- [ ] `PATCH /account/me/verification-status`로 본인인증 상태 변경이 반영된다.
- [ ] `verificationStatus`는 `NOT_STARTED`, `PENDING`, `COMPLETED`만 허용한다.
- [ ] `POST /account/password/change-request`가 LOCAL 계정에서만 동작한다.

### 비밀번호 재설정

- [ ] `POST /password/reset-request`가 가입된 이메일 기준으로 동작한다.
- [ ] `GET /password/reset-verify`가 유효한 토큰을 검증한다.
- [ ] `POST /password/reset`가 새 비밀번호를 저장한다.

## MyPage

### 계정 화면

- [ ] `GET /mypage/account`가 `name`, `email`, `phone`, `canChangePassword`, `verificationStatus`, `marketingConsent`를 반환한다.
- [ ] `GET /mypage/account`가 auth 원천 상태를 읽고 mock 값을 만들지 않는다.
- [ ] `GET /mypage/account` 호출 전에 member 삭제/미존재 검증이 적용된다.

### 신청내역 화면

- [ ] `GET /mypage/entries`가 기본값으로 `filter=ALL`, `page=0`, `size=20`으로 동작한다.
- [ ] `GET /mypage/entries?filter=ALL`이 전체 목록을 반환한다.
- [ ] `GET /mypage/entries?filter=LOTTERY`가 추첨형만 반환한다.
- [ ] `GET /mypage/entries?filter=FIRST_COME`가 선착순만 반환한다.
- [ ] `GET /mypage/entries` 응답에 `empty`가 포함된다.
- [ ] `GET /mypage/entries` 응답에 `pagination.page`, `size`, `totalCount`, `hasNext`가 포함된다.
- [ ] `GET /mypage/entries` 각 item에 `displayStatus`, `actionLabel`, `deepLink`, `thumbnailUrl`가 포함된다.
- [ ] `deepLink`가 프론트가 바로 사용할 수 있는 값으로 내려온다.
- [ ] `thumbnailUrl`이 null이 아닌 실제 값으로 내려온다.
- [ ] 이번 릴리즈 기준으로 checkout/payment 액션이 숨겨져 있다.
- [ ] `PAID` 주문 존재 여부 때문에 신청내역이 사라지지 않는다.
- [ ] 잘못된 filter 값이 400으로 거절된다.

### MyPage 주소/주문 보조 조회

- [ ] `GET /mypage/address`가 기본 배송지 요약만 반환한다.
- [ ] `GET /mypage/address`가 주소 목록 API처럼 오해되지 않도록 문서화되어 있다.
- [ ] `GET /mypage/orders`와 `GET /mypage/orders/{orderNumber}`는 현재 범위 밖이지만 코드상 정상 응답한다.

## Address

- [ ] `GET /addresses`가 사용자 주소 목록을 반환한다.
- [ ] `GET /addresses/default`가 기본 배송지를 반환한다.
- [ ] `GET /addresses/{id}`가 단건 조회를 반환한다.
- [ ] `POST /addresses`가 주소를 생성한다.
- [ ] `PUT /addresses/{id}`가 주소를 수정한다.
- [ ] `PATCH /addresses/{id}`가 주소를 수정한다.
- [ ] `PATCH /addresses/{id}/default`가 기본 배송지를 변경한다.
- [ ] `DELETE /addresses/{id}`가 주소를 삭제한다.

주소 정책:

- [ ] 최대 10개 제한이 동작한다.
- [ ] 기본 배송지는 1개만 유지된다.
- [ ] 기본 배송지를 삭제하면 다른 주소가 자동 승격된다.
- [ ] 라벨 정책이 검증된다.
- [ ] 전화번호 형식 검증이 동작한다.

호환 경로:

- [ ] `/address` 경로가 기존 프론트 호환용으로 정상 동작한다.
- [ ] 신규 연결은 `/addresses`를 사용하도록 문서화되어 있다.

## Event

- [ ] `GET /events`가 필터 query를 반영해 목록을 반환한다.
- [ ] `GET /events`가 cursor pagination을 정상 처리한다.
- [ ] `GET /events/{eventId}`가 상세를 반환한다.
- [ ] `GET /events/{eventId}/info`가 요약 정보를 반환한다.
- [ ] `GET /events/{eventId}/sales-info`가 판매/응모 상태를 반환한다.

## Entry

- [ ] `GET /events/{eventId}/entries/overview`가 로그인 전/후 모두 의도대로 동작한다.
- [ ] `POST /events/{eventId}/entries/pre-save`가 course/pace 저장을 처리한다.
- [ ] `DELETE /events/{eventId}/entries/pre-save`가 임시 저장 삭제를 처리한다.
- [ ] `GET /events/{eventId}/entries/rate`가 경쟁률을 반환한다.
- [ ] `POST /events/{eventId}/entries/apply`가 신청 완료를 처리한다.
- [ ] 테스트용 임시 API인 `POST /events/{eventId}/entries/confirm`에 신규 화면이 의존하지 않는다.

## Order

- [ ] `GET /orders`가 사용자 주문 목록을 반환한다.
- [ ] `GET /orders/{orderNumber}`가 사용자 주문 상세를 반환한다.
- [ ] `POST /orders/checkout-info`가 결제 준비 정보를 반환한다.
- [ ] `POST /orders/checkout`가 결제 흐름을 처리한다.
- [ ] 현재 MyPage reduced scope에서 주문/결제 액션이 과도하게 노출되지 않는다.

## Media

- [ ] `POST /media/presign-upload`가 업로드 URL을 발급한다.
- [ ] `POST /media/confirm`이 업로드 완료를 확정한다.

## 문서/연동 정합성

- [ ] 프론트가 계정 화면에서 `/mypage/account`를 사용한다.
- [ ] 프론트가 신청내역 화면에서 `/mypage/entries`를 사용한다.
- [ ] 프론트가 주소 목록/CRUD에서 `/addresses`를 사용한다.
- [ ] `/mypage/address`는 기본 배송지 요약으로만 사용한다.
- [ ] 내부 전용 `/internal/members/**`를 프론트가 호출하지 않는다.
- [ ] `/events/{eventId}/stock/init`를 프론트가 호출하지 않는다.

