# Backend API QA Status

기준일: 2026-03-24

실행한 검증 명령:

```bash
./gradlew :auth:test --tests '*AccountServiceTest' --tests '*AuthServiceLoginTest' --tests '*AuthServiceLogoutWithdrawTest' \
  :main:test --tests '*AddressApiControllerTest' --tests '*AddressServiceTest' \
  --tests '*MyPageControllerTest' --tests '*MyPageApiContractTest' --tests '*OrderServiceTest'
```

판정 기준:

- `[x]` 자동 테스트 또는 명확한 코드 검증으로 확인됨
- `[ ]` 아직 자동 검증되지 않았거나, HTTP 계약 기준으로 미확인

## 이상 항목 요약

- [ ] `/address/{id}`의 `PATCH` 의미가 `/addresses/{id}`와 다르다.
  - canonical `/addresses/{id}`는 주소 수정
  - compat `/address/{id}`는 기본 배송지 지정
  - 같은 HTTP method/path 패턴인데 의미가 달라서 프론트가 혼동할 수 있다.
- [ ] `auth` 컨트롤러 레이어는 서비스 테스트만 있고 HTTP 계약 테스트가 없다.
  - `/account/me`, `/signup`, `/login`, `/password/reset*`, `/email/*`, `/sms/*`, `/oauth/*`
- [ ] `event`, `entry`, `media` 컨트롤러에 대한 자동 테스트가 없다.

## 공통

- [x] `mypage`/`address` 계열 성공 응답이 `ApiResponse` envelope를 유지한다.
- [x] `mypage`/`address` 계열에서 잘못된 입력 시 공통 에러 형식을 유지한다.
- [x] `mypage`/`address` 계열에서 `X-User-Id` 누락 시 4xx를 반환한다.
- [ ] `auth` 전반에서 `Authorization` 누락/validation HTTP 계약이 일관적인지는 컨트롤러 테스트로 미검증이다.

## Auth

### 회원가입/로그인

- [x] 로그인 성공 시 access/refresh token 발급 로직이 동작한다.
- [x] 로그인 실패 시 존재하지 않는 사용자/비밀번호 불일치가 구분된다.
- [x] refresh token 재발급 로직이 동작한다.
- [x] 로그아웃 시 블랙리스트 등록 + refresh token 삭제 로직이 동작한다.
- [x] 회원탈퇴 시 비밀번호 확인/비활성화/토큰 무효화 로직이 동작한다.
- [ ] `GET /check-email` HTTP 계약은 미검증이다.
- [ ] `POST /signup` HTTP 계약은 미검증이다.
- [ ] `POST /login` HTTP 계약은 미검증이다.
- [ ] `POST /token/refresh` HTTP 계약은 미검증이다.
- [ ] `POST /logout` HTTP 계약은 미검증이다.
- [ ] `POST /find-email` HTTP 계약은 미검증이다.
- [ ] `DELETE /account` HTTP 계약은 미검증이다.

### 이메일/SMS 인증

- [ ] `POST /email/send-code` HTTP 계약은 미검증이다.
- [ ] `POST /email/verify-code` HTTP 계약은 미검증이다.
- [ ] `POST /sms/send` HTTP 계약은 미검증이다.
- [ ] `POST /sms/send-for-find` HTTP 계약은 미검증이다.
- [ ] `POST /sms/verify` HTTP 계약은 미검증이다.

### 계정관리

- [x] `GET /account/me` 응답 모델에 `name`, `email`, `phone`, `canChangePassword`, `verificationStatus`, `marketingConsent`가 포함된다.
- [x] `LOCAL` 계정은 `canChangePassword=true`다.
- [x] OAuth 계정은 `canChangePassword=false`다.
- [x] `marketingConsent` 변경 로직이 반영된다.
- [x] `verificationStatus` 변경 로직이 반영된다.
- [x] `verificationStatus` 기본값은 `NOT_STARTED`다.
- [ ] `PATCH /account/me` HTTP 계약은 미검증이다.
- [ ] `PATCH /account/me/marketing-consent` HTTP 계약은 미검증이다.
- [ ] `PATCH /account/me/verification-status` HTTP 계약은 미검증이다.
- [ ] `POST /account/password/change-request` HTTP 계약은 미검증이다.

### 비밀번호 재설정

- [ ] `/password/reset-request`, `/password/reset-verify`, `/password/reset` HTTP 계약은 미검증이다.

## MyPage

### 계정 화면

- [x] `GET /mypage/account`가 `name`, `email`, `phone`, `canChangePassword`, `verificationStatus`, `marketingConsent`를 반환한다.
- [x] `GET /mypage/account`는 `auth` 원천 상태를 읽고 mock 값을 만들지 않는다.
- [x] `GET /mypage/account` 호출 전에 member 검증이 적용된다.

### 신청내역 화면

- [x] `GET /mypage/entries`가 기본값으로 `filter=ALL`, `page=0`, `size=20`으로 동작한다.
- [x] `GET /mypage/entries?filter=ALL`이 전체 목록을 반환한다.
- [x] `GET /mypage/entries?filter=LOTTERY`가 추첨형만 반환한다.
- [x] `GET /mypage/entries?filter=FIRST_COME`가 선착순만 반환한다.
- [x] `GET /mypage/entries` 응답에 `empty`가 포함된다.
- [x] `GET /mypage/entries` 응답에 `pagination.page`, `size`, `totalCount`, `hasNext`가 포함된다.
- [x] `GET /mypage/entries` 각 item에 `displayStatus`, `actionLabel`, `deepLink`, `thumbnailUrl`가 포함된다.
- [x] `deepLink`가 프론트가 바로 사용할 수 있는 값으로 내려온다.
- [x] `thumbnailUrl`이 실제 값으로 내려온다.
- [x] checkout/payment 액션이 숨겨져 있다.
- [x] `PAID` 주문 존재 여부 때문에 신청내역이 사라지지 않는다.
- [x] 잘못된 filter 값이 400으로 거절된다.

### MyPage 주소/주문 보조 조회

- [x] `GET /mypage/address`가 기본 배송지 요약만 반환한다.
- [x] `/mypage/address`는 주소 목록 API가 아니라는 점이 문서화되어 있다.
- [x] `GET /mypage/orders`와 `GET /mypage/orders/{orderNumber}`는 컨트롤러 레벨 응답 shape가 유지된다.

## Address

- [x] `GET /addresses`가 사용자 주소 목록을 반환한다.
- [x] `GET /addresses/default`가 기본 배송지를 반환한다.
- [x] `GET /addresses/{id}`가 단건 조회를 반환한다.
- [x] `POST /addresses`가 주소를 생성한다.
- [x] `PUT /addresses/{id}`가 주소를 수정한다.
- [x] `PATCH /addresses/{id}`가 주소를 수정한다.
- [x] `PATCH /addresses/{id}/default`가 기본 배송지를 변경한다.
- [x] `DELETE /addresses/{id}`가 주소를 삭제한다.
- [x] `/api/account/addresses` 호환 경로가 동작한다.
- [x] `/address` 호환 경로가 동작한다.

주소 정책:

- [x] 최대 10개 제한이 동작한다.
- [x] 기본 배송지는 1개만 유지된다.
- [x] 기본 배송지를 삭제하면 자동 승격된다.
- [x] 라벨 정책이 검증된다.
- [x] 전화번호 형식 검증이 동작한다.

주의:

- [ ] `/address/{id}`의 `PATCH`는 수정이 아니라 기본 배송지 지정이다. canonical 경로와 의미가 달라서 주의가 필요하다.

## Event

- [ ] `GET /events` HTTP 계약은 자동 검증되지 않았다.
- [ ] `GET /events/{eventId}` HTTP 계약은 자동 검증되지 않았다.
- [ ] `GET /events/{eventId}/info` HTTP 계약은 자동 검증되지 않았다.
- [ ] `GET /events/{eventId}/sales-info` HTTP 계약은 자동 검증되지 않았다.

## Entry

- [ ] `GET /events/{eventId}/entries/overview` HTTP 계약은 자동 검증되지 않았다.
- [ ] `POST /events/{eventId}/entries/pre-save` HTTP 계약은 자동 검증되지 않았다.
- [ ] `DELETE /events/{eventId}/entries/pre-save` HTTP 계약은 자동 검증되지 않았다.
- [ ] `GET /events/{eventId}/entries/rate` HTTP 계약은 자동 검증되지 않았다.
- [ ] `POST /events/{eventId}/entries/apply` HTTP 계약은 자동 검증되지 않았다.
- [x] `POST /events/{eventId}/entries/confirm`는 신규 화면이 의존하지 말아야 하는 임시 API로 문서화되어 있다.

## Order

- [x] checkout-info 서비스 로직은 기본/선택 배송지 반영을 처리한다.
- [x] checkout 서비스 로직은 배송지 스냅샷 반영을 처리한다.
- [x] 잘못된 배송지 id에서 예외가 발생한다.
- [ ] `/orders` 컨트롤러 HTTP 계약은 자동 검증되지 않았다.
- [ ] `/orders/{orderNumber}` 컨트롤러 HTTP 계약은 자동 검증되지 않았다.
- [ ] `/orders/checkout-info` 컨트롤러 HTTP 계약은 자동 검증되지 않았다.
- [ ] `/orders/checkout` 컨트롤러 HTTP 계약은 자동 검증되지 않았다.

## Media

- [ ] `/media/presign-upload` HTTP 계약은 자동 검증되지 않았다.
- [ ] `/media/confirm` HTTP 계약은 자동 검증되지 않았다.

## 문서/연동 정합성

- [x] 프론트 연동 문서에서 계정 화면은 `/mypage/account` 기준으로 정리되어 있다.
- [x] 프론트 연동 문서에서 신청내역 화면은 `/mypage/entries` 기준으로 정리되어 있다.
- [x] 프론트 연동 문서에서 주소 목록/CRUD는 `/addresses` 기준으로 정리되어 있다.
- [x] `/mypage/address`는 기본 배송지 요약으로만 문서화되어 있다.
- [x] 내부 전용 `/internal/members/**`는 프론트 비사용 경로로 분리되어 있다.
- [x] `/events/{eventId}/stock/init`는 프론트 비사용 경로로 분리되어 있다.

