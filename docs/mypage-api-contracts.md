# MyPage API Contracts

## Scope

Current MyPage backend keeps responsibilities split as follows:

- `GET /mypage/account`: account screen read-model aggregated in `main`
- `GET /mypage/entries`: reduced-scope application history screen
- `/addresses`: address list and CRUD
- `GET /mypage/address`: default-address summary only

Do not merge these back into one oversized `/mypage` contract for the current release.

## Account

`GET /mypage/account`

- source of truth: `auth`
- aggregation: `main`
- required headers: internal `X-User-Id`

Response fields:

- `name`
- `email`
- `phone`
- `canChangePassword`
- `verificationStatus`
- `marketingConsent`

`verificationStatus` and `marketingConsent` are now read from `auth` persisted state, not from `main` mocks.

Auth internal source APIs:

- `GET /account/me`
- `PATCH /account/me/marketing-consent`
- `PATCH /account/me/verification-status`

## Application History

`GET /mypage/entries`

- server-side filter: `ALL | LOTTERY | FIRST_COME`
- frontend-ready fields include:
  - `displayStatus`
  - `actionLabel`
  - `deepLink`
  - `thumbnailUrl`
  - `empty`
  - `pagination`

Order and payment actions remain hidden in the current reduced scope.

## Address

Address management remains independent from MyPage aggregation.

Primary routes:

- `GET /addresses`
- `POST /addresses`
- `PUT /addresses/{id}`
- `DELETE /addresses/{id}`

`GET /mypage/address` is only for default-address summary and should not be reused as the address-list endpoint.
