# MyPage API 명세서

- 작성일: 2026-04-01
- 기준: 현재 백엔드 구현 기준
- 범위: `GET /mypage/*` 읽기 API

## 공통

- Base Path: `/mypage`
- 인증: 모든 API는 `X-User-Id` 헤더가 필요합니다.
- 전송방식: 현재 마이페이지 API는 전부 `GET` 조회 API이며 `Request Body`를 사용하지 않습니다.
- 응답 포맷: 모든 API는 공통 `ApiResponse<T>` 래퍼를 사용합니다.
- 날짜/시간 형식: 응답의 날짜 필드는 `LocalDateTime` 직렬화 값이며, 예시는 `yyyy-MM-dd'T'HH:mm:ss` 형식을 사용합니다. timezone offset은 포함되지 않습니다.
- 배열 필드 정책:
  - 목록이 없을 때는 `null` 대신 빈 배열 `[]`를 반환합니다.
  - 현재 코드 기준 빈 배열 보장이 있는 대표 필드: `addressList`, `items`, `packages`
- nullable 필드 정책:
  - 선택 정보가 없거나 원천 데이터가 없는 경우 객체 필드 또는 날짜 필드는 `null`이 될 수 있습니다.
  - 주소가 없는 사용자는 `hasAddress=false`, `defaultAddress=null` 으로 표현합니다.
  - 문서 각 섹션의 `비고`에서 nullable 여부를 별도로 표기합니다.

### 공통 Headers

| 변수명 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `X-User-Id` | Long | O | 로그인 사용자 ID |

### 공통 성공 응답 형식

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": null,
  "data": {},
  "timestamp": "2026-03-31T12:00:00"
}
```

### 공통 에러 응답 형식

```json
{
  "success": false,
  "code": "ERROR_CODE",
  "message": "에러 메시지",
  "timestamp": "2026-03-31T12:00:00"
}
```

### 전송방식 요약

| API | Method | Header | Path | Query | Body |
| --- | --- | --- | --- | --- | --- |
| `/mypage/account` | `GET` | `X-User-Id` | 없음 | 없음 | 없음 |
| `/mypage` | `GET` | `X-User-Id` | 없음 | 없음 | 없음 |
| `/mypage/entries` | `GET` | `X-User-Id` | 없음 | `filter` | 없음 |
| `/mypage/waiting-entries` | `GET` | `X-User-Id` | 없음 | `page`, `size` | 없음 |
| `/mypage/orders` | `GET` | `X-User-Id` | 없음 | `tab` | 없음 |
| `/mypage/orders/{orderNumber}` | `GET` | `X-User-Id` | `orderNumber` | 없음 | 없음 |
| `/mypage/address` | `GET` | `X-User-Id` | 없음 | 없음 | 없음 |

---

## 1. 회원정보

- `GET /mypage/account`
- 회원 기본정보와 배송지 목록을 조회합니다.

### Path Variables

- 없음

### Query Parameters

- 없음

### Request Body

- 없음

### Response Body

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": null,
  "data": {
    "id": 1,
    "name": "김유저",
    "phoneNumber": "01012345678",
    "email": "user@email.com",
    "isPassAuth": false,
    "addressList": [
      {
        "id": 1,
        "label": "우리집",
        "receiverName": "홍길동",
        "phoneNumber": "01012345678",
        "zipcode": "12345",
        "address1": "서울특별시 강남구 테헤란로 123",
        "address2": "좋은아파트 102동 304호",
        "memo": "문앞",
        "isDefault": true
      }
    ]
  },
  "timestamp": "2026-03-31T12:00:00"
}
```

### 필드(data)

| 필드 | 타입 | 설명 | 비고 |
| --- | --- | --- | --- |
| `id` | Long | 사용자 ID |  |
| `name` | String | 사용자 이름 |  |
| `phoneNumber` | String | 사용자 연락처 |  |
| `email` | String | 사용자 이메일 |  |
| `isPassAuth` | Boolean | 본인인증 완료 여부 | auth `verificationStatus=VERIFIED`면 `true` |
| `addressList` | Array | 배송지 목록 | 없으면 빈 배열 `[]` |

### 필드(addressList)

| 필드 | 타입 | 설명 | 비고 |
| --- | --- | --- | --- |
| `id` | Long | 배송지 ID |  |
| `label` | String | 배송지 별칭 |  |
| `receiverName` | String | 받는 사람 |  |
| `phoneNumber` | String | 받는 사람 연락처 |  |
| `zipcode` | String | 우편번호 |  |
| `address1` | String | 기본 주소 |  |
| `address2` | String | 상세 주소 | 비어 있을 수 있음 |
| `memo` | String | 배송 메모 | 비어 있을 수 있음 |
| `isDefault` | Boolean | 기본 배송지 여부 |  |

---

## 2. 마이페이지 개요

- `GET /mypage`
- 메인 화면 요약 정보를 한 번에 조회합니다.

### Path Variables

- 없음

### Query Parameters

- 없음

### Request Body

- 없음

### Response Body

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": null,
  "data": {
    "entries": {
      "page": 0,
      "size": 3,
      "totalCount": 1,
      "hasNext": false,
      "items": []
    },
    "waitingEntries": {
      "page": 0,
      "size": 3,
      "totalCount": 1,
      "hasNext": false,
      "items": []
    },
    "orders": {
      "page": 0,
      "size": 3,
      "totalCount": 1,
      "hasNext": false,
      "items": []
    },
    "address": {
      "hasAddress": true,
      "defaultAddress": {
        "receiverName": "홍길동",
        "label": "집",
        "address": "서울시 강남구 101동",
        "phone": "01012345678"
      }
    }
  },
  "timestamp": "2026-03-31T12:00:00"
}
```

### 필드(data)

| 필드 | 타입 | 설명 | 비고 |
| --- | --- | --- | --- |
| `entries` | Object | 신청내역 요약 | 기본 조회 크기 `size=3` |
| `waitingEntries` | Object | 대기 신청내역 요약 | 기본 조회 크기 `size=3` |
| `orders` | Object | 주문내역 요약 | 기본 조회 크기 `size=3` |
| `address` | Object | 기본 배송지 요약 |  |

### 비고

- overview 내부의 목록형 응답은 공통 요약 정책을 따르며 첫 페이지(`page=0`) 3건만 반환합니다.
- `entries.items`, `waitingEntries.items`, `orders.items` 는 데이터가 없으면 빈 배열 `[]`를 반환합니다.
- `address.defaultAddress` 는 주소가 없는 사용자의 경우 `null` 이며, 이때 `address.hasAddress=false` 입니다.

---

## 3. 신청내역

- `GET /mypage/entries`
- 사용자의 신청내역 전체 목록을 조회합니다.

### Path Variables

- 없음

### Query Parameters

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
| --- | --- | --- | --- | --- |
| `filter` | String | X | `ALL` | `ALL`, `LOTTERY`, `FIRST_COME` |

### Request Body

- 없음

### Response Body

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": null,
  "data": [
    {
      "id": 1,
      "eventId": 1,
      "title": "서울 마라톤 2026",
      "appType": "LOTTERY",
      "status": "IN_PROGRESS",
      "entryStatus": "응모 완료",
      "date": "2026-02-26T00:00:00",
      "eventAt": "2026-03-15T09:00:00",
      "appStartAt": "2026-02-01T00:00:00",
      "appEndAt": "2026-02-28T23:59:59",
      "resultAt": "2026-02-28T23:59:59",
      "venue": "서울특별시 송파구 올림픽로",
      "course": "10km",
      "pace": "5’30’’ ~ 6’30’’/km"
    }
  ],
  "timestamp": "2026-03-31T12:00:00"
}
```

### 필드(data item)

| 필드 | 타입 | 설명 | 비고 |
| --- | --- | --- | --- |
| `id` | Long | 신청 내역 ID | 엔트리 ID |
| `eventId` | Long | 이벤트 ID |  |
| `title` | String | 이벤트명 |  |
| `appType` | String | 이벤트 신청 유형 | `LOTTERY`, `FIRST_COME` |
| `status` | String | 이벤트 상태 | `IN_PROGRESS`, `CLOSING_SOON`, `READY`, `END`, `DRAW_COMPLETED` |
| `entryStatus` | String | 화면 노출용 신청 상태 | 아래 상태 매핑 참고 |
| `date` | String | 신청 일시 | entry `createdAt` |
| `eventAt` | String | 이벤트 일시 |  |
| `appStartAt` | String | 신청 시작 일시 |  |
| `appEndAt` | String | 신청 종료 일시 |  |
| `resultAt` | String | 결과 발표 일시 | `LOTTERY`는 `event.lotteryAnnouncedAt`, `FIRST_COME`는 항상 `null` |
| `venue` | String | 장소 |  |
| `course` | String | 신청 코스 | 항상 존재 |
| `pace` | String | 신청 페이스 | 항상 존재 |

### `resultAt` 정책

| appType | 값 |
| --- | --- |
| `LOTTERY` | 이벤트의 결과 발표일 `event.lotteryAnnouncedAt` 반환 |
| `FIRST_COME` | 항상 `null` |

- 현재 구현은 이벤트 생성/수정 단계에서 `LOTTERY`의 `lotteryAnnouncedAt` 필수 입력을 강제하지 않습니다.
- 따라서 `LOTTERY`라도 이벤트 원천 데이터에 발표일이 설정되지 않았다면 `resultAt`은 `null`일 수 있습니다.

### `entryStatus` 매핑

| 내부 표시 상태 | 공개 값 |
| --- | --- |
| `PRE_ENTRY_SAVED` | `신청 대기` |
| `WAITING_TO_APPLY` | `신청 대기` |
| `AVAILABLE_TO_APPLY` | `신청 가능` |
| `ENTRY_CLOSED` | `신청 불가` |
| `ENTRY_UNAVAILABLE` | `신청 불가` |
| `RESERVED` | `신청 완료` |
| `ENTRY_APPLIED` | `신청 완료` |
| `LOTTERY_APPLIED` | `응모 완료` |
| `RESULT_PENDING` | `발표 대기` |
| `RESULT_CHECK_REQUIRED` | `발표 대기` |
| `WON` | `당첨` |
| `LOST` | `미당첨` |

### 에러 케이스

| 상황 | HTTP Status | code |
| --- | --- | --- |
| `X-User-Id` 누락 | `400` | 스프링 헤더 검증 오류 |
| 존재하지 않는 회원 | `404` | `MBR_001` |
| 잘못된 `filter` 값 | `400` | `CMN_002` |

---

## 4. 대기 신청내역

- `GET /mypage/waiting-entries`
- 신청 대기 상태 목록을 조회합니다.

### Path Variables

- 없음

### Query Parameters

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
| --- | --- | --- | --- | --- |
| `page` | int | X | `0` | 페이지 번호, 0-based |
| `size` | int | X | `20` | 페이지 크기, `1 <= size <= 100` |

### Request Body

- 없음

### Response Body

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": null,
  "data": {
    "page": 0,
    "size": 20,
    "totalCount": 1,
    "hasNext": false,
    "items": [
      {
        "entryId": 1,
        "eventId": 1,
        "status": "신청 대기",
        "actionType": "EDIT",
        "actionLabel": "사전정보 수정",
        "actionEnabled": true,
        "thumbnailUrl": null,
        "title": "서울 마라톤 2026",
        "courseName": "10km",
        "paceName": "5’30’’ ~ 6’30’’/km",
        "price": 25000,
        "appliedAt": "2026-02-26T00:00:00",
        "resultAt": null
      }
    ]
  },
  "timestamp": "2026-03-31T12:00:00"
}
```

### 필드(data)

| 필드 | 타입 | 설명 | 비고 |
| --- | --- | --- | --- |
| `page` | int | 페이지 번호 | 0-based |
| `size` | int | 페이지 크기 |  |
| `totalCount` | long | 전체 건수 |  |
| `hasNext` | boolean | 다음 페이지 존재 여부 | `(page + 1) * size < totalCount` |
| `items` | Array | 대기 신청내역 목록 | 없으면 빈 배열 `[]` |

### 필드(items item)

| 필드 | 타입 | 설명 | 비고 |
| --- | --- | --- | --- |
| `entryId` | Long | 엔트리 ID |  |
| `eventId` | Long | 이벤트 ID |  |
| `status` | String | 상태 텍스트 | 화면 노출용 값 |
| `actionType` | String | 액션 타입 | `NONE`, `EDIT`, `APPLY`, `CHECKOUT` |
| `actionLabel` | String | 액션 라벨 | 없으면 `null` |
| `actionEnabled` | Boolean | 액션 가능 여부 |  |
| `thumbnailUrl` | String | 썸네일 URL | 현재는 `null` 가능 |
| `title` | String | 이벤트명 |  |
| `courseName` | String | 코스명 | 항상 존재 |
| `paceName` | String | 페이스명 | 항상 존재 |
| `price` | Long | 금액 | 코스 가격 |
| `appliedAt` | String | 신청 일시 |  |
| `resultAt` | String | 결과 일시 | `LOTTERY`는 `event.lotteryAnnouncedAt`, `FIRST_COME`는 항상 `null` |

### 에러 케이스

| 상황 | HTTP Status | code |
| --- | --- | --- |
| `X-User-Id` 누락 | `400` | 스프링 헤더 검증 오류 |
| 존재하지 않는 회원 | `404` | `MBR_001` |
| `page < 0` 또는 `size <= 0` 또는 `size > 100` | `400` | `CMN_002` |

---

## 5. 결제내역

- `GET /mypage/orders`
- 사용자의 결제내역 전체 목록을 조회합니다.

### Path Variables

- 없음

### Query Parameters

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
| --- | --- | --- | --- | --- |
| `tab` | String | X | `ALL` | `ALL`, `PENDING`, `COMPLETED`, `CANCELLED` |

### Request Body

- 없음

### Response Body

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": null,
  "data": [
    {
      "id": "ORD20260215001",
      "eventId": 1,
      "title": "서울 마라톤 2026",
      "appType": "LOTTERY",
      "status": "END",
      "orderStatus": "입금대기",
      "date": "2026-02-26T00:00:00",
      "eventAt": "2026-03-15T09:00:00",
      "venue": "서울특별시 송파구 올림픽로",
      "course": "10km",
      "pace": "5’30’’ ~ 6’30’’/km",
      "price": 51000
    }
  ],
  "timestamp": "2026-03-31T12:00:00"
}
```

### 필드(data item)

| 필드 | 타입 | 설명 | 비고 |
| --- | --- | --- | --- |
| `id` | String | 주문 번호 |  |
| `eventId` | Long | 이벤트 ID | 없으면 `null` |
| `title` | String | 이벤트명 | 없으면 `null` |
| `appType` | String | 이벤트 신청 유형 | `LOTTERY`, `FIRST_COME`, 없으면 `null` |
| `status` | String | 이벤트 상태 | `IN_PROGRESS`, `CLOSING_SOON`, `READY`, `END`, `DRAW_COMPLETED`, 없으면 `null` |
| `orderStatus` | String | 화면 노출용 결제 상태 | 아래 상태 매핑 참고 |
| `date` | String | 주문 일시 | order `createdAt` |
| `eventAt` | String | 이벤트 일시 | 없으면 `null` |
| `venue` | String | 장소 | 없으면 `null` |
| `course` | String | 신청 코스 | 없으면 `null` |
| `pace` | String | 신청 페이스 | 없으면 `null` |
| `price` | Long | 결제 금액 | `finalAmount` |

### `orderStatus` 매핑

| 원천 상태 | 공개 값 |
| --- | --- |
| `PENDING` | `입금대기` |
| `PAID` | `결제완료` |
| `CANCELLED` | `결제취소` |
| `EXPIRED` | `결제취소` |
| `FAILED` | `결제취소` |

### 비고

- 현재 구현에는 `상품준비중`, `배송중`, `배송완료`, `구매확정`, `교환접수`, `교환완료`, `환불접수`, `환불완료` 상태가 없습니다.
- `tab=CANCELLED` 조회 조건은 현재 원천 `OrderStatus.CANCELLED`만 포함합니다.
- 따라서 전체 목록에서는 `EXPIRED`, `FAILED`가 `결제취소`로 표시되더라도 `CANCELLED` 탭에는 포함되지 않을 수 있습니다.

### 에러 케이스

| 상황 | HTTP Status | code |
| --- | --- | --- |
| `X-User-Id` 누락 | `400` | 스프링 헤더 검증 오류 |
| 존재하지 않는 회원 | `404` | `MBR_001` |
| 잘못된 `tab` 값 | `400` | `ORD_002` |

---

## 6. 주문 상세

- `GET /mypage/orders/{orderNumber}`
- 주문 상세 정보를 조회합니다.

### Path Variables

| 변수명 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `orderNumber` | String | O | 주문 번호 |

### Query Parameters

- 없음

### Request Body

- 없음

### Response Body

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": null,
  "data": {
    "eventId": 1,
    "orderNumber": "ORD-20260317-0001",
    "status": "결제 대기",
    "actionType": "DETAIL",
    "actionLabel": "주문 상세보기",
    "actionEnabled": true,
    "orderedAt": "2026-03-17T10:00:00",
    "paymentDeadlineAt": null,
    "eventTitle": "서울 마라톤 대회 2026",
    "thumbnailUrl": null,
    "courseName": "풀코스",
    "paceName": "6:00/km",
    "itemTotalAmount": 35000,
    "shippingFee": 3000,
    "discountAmount": 0,
    "finalAmount": 38000,
    "recipientName": "홍길동",
    "addressLabel": "집",
    "recipientPhone": "01012345678",
    "zipCode": "12345",
    "address": "서울시 강남구",
    "detailAddress": "101동",
    "deliveryMemo": "문앞",
    "paymentMethod": null,
    "hasShipmentInfo": false,
    "shipmentStatus": null,
    "trackingNumber": null,
    "canCancel": true,
    "canRefund": false,
    "canExchange": false,
    "packages": [
      {
        "eventPackageId": 1,
        "name": "기록칩",
        "price": 5000
      }
    ]
  },
  "timestamp": "2026-03-31T12:00:00"
}
```

### 필드(data)

| 필드 | 타입 | 설명 | 비고 |
| --- | --- | --- | --- |
| `eventId` | Long | 이벤트 ID | 없으면 `null` |
| `orderNumber` | String | 주문 번호 |  |
| `status` | String | 주문 상태 텍스트 | 화면 노출용 값 |
| `actionType` | String | 액션 타입 |  |
| `actionLabel` | String | 액션 라벨 |  |
| `actionEnabled` | Boolean | 액션 가능 여부 |  |
| `orderedAt` | String | 주문 일시 |  |
| `paymentDeadlineAt` | String | 결제 마감 일시 | 현재 구현상 `null` |
| `eventTitle` | String | 이벤트명 | 없으면 `null` |
| `thumbnailUrl` | String | 썸네일 URL | 현재는 `null` 가능 |
| `courseName` | String | 코스명 | 없으면 `null` |
| `paceName` | String | 페이스명 | 없으면 `null` |
| `itemTotalAmount` | Long | 상품 금액 |  |
| `shippingFee` | Long | 배송비 |  |
| `discountAmount` | Long | 할인 금액 |  |
| `finalAmount` | Long | 최종 결제 금액 |  |
| `recipientName` | String | 수령인 |  |
| `addressLabel` | String | 배송지 별칭 |  |
| `recipientPhone` | String | 수령인 연락처 |  |
| `zipCode` | String | 우편번호 |  |
| `address` | String | 기본 주소 |  |
| `detailAddress` | String | 상세 주소 |  |
| `deliveryMemo` | String | 배송 메모 |  |
| `paymentMethod` | String | 결제수단 | 현재 구현상 `null` |
| `hasShipmentInfo` | Boolean | 배송 정보 존재 여부 | 현재 구현상 `false` |
| `shipmentStatus` | String | 배송 상태 | 현재 구현상 `null` |
| `trackingNumber` | String | 운송장 번호 | 현재 구현상 `null` |
| `canCancel` | Boolean | 취소 가능 여부 |  |
| `canRefund` | Boolean | 환불 가능 여부 |  |
| `canExchange` | Boolean | 교환 가능 여부 |  |
| `packages` | Array | 패키지 목록 | 없으면 빈 배열 `[]` |

### 필드(packages)

| 필드 | 타입 | 설명 | 비고 |
| --- | --- | --- | --- |
| `eventPackageId` | Long | 패키지 ID |  |
| `name` | String | 패키지명 |  |
| `price` | Long | 패키지 금액 |  |

### 에러 케이스

| 상황 | HTTP Status | code |
| --- | --- | --- |
| `X-User-Id` 누락 | `400` | 스프링 헤더 검증 오류 |
| 존재하지 않는 회원 | `404` | `MBR_001` |
| 존재하지 않는 주문 | `404` | `ORD_001` |

---

## 7. 기본 배송지

- `GET /mypage/address`
- 기본 배송지 요약 정보를 조회합니다.

### Path Variables

- 없음

### Query Parameters

- 없음

### Request Body

- 없음

### Response Body

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": null,
  "data": {
    "hasAddress": true,
    "defaultAddress": {
      "receiverName": "홍길동",
      "label": "집",
      "address": "서울시 강남구 101동",
      "phone": "01012345678"
    }
  },
  "timestamp": "2026-03-31T12:00:00"
}
```

### 필드(data)

| 필드 | 타입 | 설명 | 비고 |
| --- | --- | --- | --- |
| `hasAddress` | Boolean | 기본 배송지 존재 여부 |  |
| `defaultAddress` | Object | 기본 배송지 요약 정보 | 주소가 없으면 `null` |

### 필드(defaultAddress)

| 필드 | 타입 | 설명 | 비고 |
| --- | --- | --- | --- |
| `receiverName` | String | 수령인 이름 |  |
| `label` | String | 배송지 별칭 |  |
| `address` | String | 기본주소 + 상세주소 조합 문자열 | `address1`과 `address2`를 합쳐 생성 |
| `phone` | String | 연락처 |  |

### 비고

- 현재 정책은 주소 없는 사용자를 허용합니다.
- 기본 배송지가 없으면 `hasAddress=false`, `defaultAddress=null` 을 반환합니다.
- 동일한 정책이 `GET /mypage` overview 의 `address.defaultAddress` 에도 적용됩니다.

### 에러 케이스

| 상황 | HTTP Status | code |
| --- | --- | --- |
| `X-User-Id` 누락 | `400` | 스프링 헤더 검증 오류 |
| 존재하지 않는 회원 | `404` | `MBR_001` |

---

## 공통 보완 메모

- 이 문서는 현재 구현 기준 문서입니다. 향후 응답 필드나 상태 매핑이 바뀌면 DTO와 함께 갱신해야 합니다.
- `mypage`는 조회 전용 문서이며, 생성/수정/삭제 API는 포함하지 않습니다.
- 현재 구현상 `GET /mypage/*`는 모두 `Request Body`를 사용하지 않습니다.
