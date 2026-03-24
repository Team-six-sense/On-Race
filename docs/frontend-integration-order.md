# Frontend Integration Order

기준일: 2026-03-24

프론트 개발자가 현재 백엔드 기준으로 막힘 없이 바로 붙일 수 있는 순서만 정리한 문서다.

## 1. 공통 세팅

- 모든 로그인 필요 API에는 `X-User-Id`가 들어온다고 가정한다.
- 응답은 모두 `ApiResponse<T>` 형태이므로 `data`를 기준으로 화면에 바인딩한다.
- 신규 주소 연결은 `/addresses`를 사용한다.
- `/mypage/address`는 기본 배송지 요약이고, 주소 목록 API가 아니다.

## 2. 신청내역 화면 먼저 연결

가장 먼저 붙일 API:

- `GET /mypage/entries`

기본 호출 예:

```http
GET /mypage/entries?filter=ALL&page=0&size=20
```

먼저 붙여야 하는 이유:

- 지금 기준으로 가장 안정적으로 닫힌 화면 계약이다.
- 프론트가 별도 비즈니스 계산 없이 바로 그릴 수 있다.

우선 사용 필드:

- `data.filter`
- `data.empty`
- `data.pagination.page`
- `data.pagination.size`
- `data.pagination.totalCount`
- `data.pagination.hasNext`
- `data.items[].eventId`
- `data.items[].eventName`
- `data.items[].applicationType`
- `data.items[].displayStatus`
- `data.items[].actionLabel`
- `data.items[].deepLink`
- `data.items[].thumbnailUrl`
- `data.items[].courseName`
- `data.items[].paceName`
- `data.items[].price`
- `data.items[].appliedAt`

필터 연결:

- 전체 탭: `filter=ALL`
- 추첨 탭: `filter=LOTTERY`
- 선착순 탭: `filter=FIRST_COME`

주의:

- `deepLink`는 프론트가 조합하지 말고 응답값 그대로 사용한다.
- `thumbnailUrl`은 별도 이미지 조회 API를 다시 부르지 않는다.
- 이번 릴리즈에서 결제/주문 CTA는 적극 사용하지 않는다.

## 3. 계정 화면 연결

다음으로 붙일 API:

- `GET /mypage/account`

기본 호출 예:

```http
GET /mypage/account
```

우선 사용 필드:

- `data.name`
- `data.email`
- `data.phone`
- `data.canChangePassword`
- `data.verificationStatus`
- `data.marketingConsent`

UI 연결 규칙:

- 이름/이메일/전화번호는 그대로 출력
- 비밀번호 변경 버튼은 `canChangePassword === true`일 때만 활성
- 본인인증 상태는 `verificationStatus` 값으로만 분기
- 마케팅 토글 초기값은 `marketingConsent`

상태값:

- `NOT_STARTED`
- `PENDING`
- `COMPLETED`

## 4. 마케팅 토글 연결

사용 API:

- `PATCH /account/me/marketing-consent`

요청 예:

```json
{
  "marketingConsent": true
}
```

권장 순서:

1. 화면 진입 시 `GET /mypage/account`
2. 토글 변경 시 `PATCH /account/me/marketing-consent`
3. 성공 후 `GET /mypage/account` 재조회 또는 로컬 상태 갱신

## 5. 본인인증 상태 버튼 연결

사용 API:

- `PATCH /account/me/verification-status`

요청 예:

```json
{
  "verificationStatus": "PENDING"
}
```

현재 단계 메모:

- 외부 본인인증 연동이 아니라 내부 상태 변경용이다.
- 화면에서는 MVP용 상태 전환 버튼으로만 사용한다.

권장 UI 분기:

- `NOT_STARTED` → 인증하기
- `PENDING` → 진행중 표시
- `COMPLETED` → 다시 인증하기 또는 완료 표시

## 6. 배송지 목록 연결

사용 API:

- `GET /addresses`

기본 호출 예:

```http
GET /addresses
```

우선 사용 필드:

- `data[].id`
- `data[].label`
- `data[].receiverName`
- `data[].phone`
- `data[].zipcode`
- `data[].address1`
- `data[].address2`
- `data[].memo`
- `data[].isDefault`

주의:

- 주소 목록은 `/mypage/address`가 아니라 `/addresses`를 사용한다.
- `/mypage/address`는 계정 화면 상단의 기본 배송지 요약 카드에만 쓸 수 있다.

## 7. 기본 배송지 요약 카드 연결

사용 API:

- `GET /mypage/address`

기본 호출 예:

```http
GET /mypage/address
```

응답 사용:

- `data.hasAddress`
- `data.defaultAddress`

사용 목적:

- 계정 화면 상단 기본 배송지 요약
- 전체 주소 목록 대체용으로 사용하면 안 된다.

## 8. 배송지 추가/수정/삭제 연결

사용 API:

- `POST /addresses`
- `PUT /addresses/{id}`
- `DELETE /addresses/{id}`
- `PATCH /addresses/{id}/default`

생성/수정 요청 필드:

- `receiverName`
- `phone`
- `zipcode`
- `address1`
- `address2`
- `memo`
- `isDefault`
- `label`

권장 순서:

1. 목록 조회 `GET /addresses`
2. 신규 생성 `POST /addresses`
3. 수정 `PUT /addresses/{id}`
4. 기본 배송지 변경 `PATCH /addresses/{id}/default`
5. 삭제 `DELETE /addresses/{id}`
6. 성공 후 목록 재조회

## 9. 비밀번호 변경 버튼 연결

사용 API:

- `POST /account/password/change-request`

요청 예:

```json
{
  "currentPassword": "현재비밀번호"
}
```

주의:

- `canChangePassword === true`일 때만 버튼 노출/활성
- OAuth 계정은 비활성 처리

## 10. 지금 붙이지 말아야 하는 것

- `/mypage/orders`
- `/mypage/orders/{orderNumber}`
- `/orders/checkout-info`
- `/orders/checkout`
- `/events/{eventId}/entries/confirm`
- `/internal/members/**`

현재 범위에서는 위 API를 MyPage 핵심 화면에 연결하지 않는다.

## 권장 작업 순서 요약

1. 신청내역: `GET /mypage/entries`
2. 계정: `GET /mypage/account`
3. 마케팅 토글: `PATCH /account/me/marketing-consent`
4. 본인인증 상태: `PATCH /account/me/verification-status`
5. 배송지 목록: `GET /addresses`
6. 배송지 CRUD: `POST/PUT/DELETE/PATCH /addresses/**`
7. 기본 배송지 요약: `GET /mypage/address`
8. 비밀번호 변경 요청: `POST /account/password/change-request`

