# 신청내역 백엔드 구현 가이드

- 작성일: 2026-03-26
- 대상: 백엔드 개발자, 기획자, 비전공자
- 목적: 신청내역 상태 계산 분리와 카운트/필터 동작을 현재 코드 기준으로 한 번에 이해할 수 있게 정리한다.

## 1. 한눈에 보는 결론

### 전문가 설명

- 신청내역의 화면 표시 상태 계산은 `ApplicationHistoryService` 안의 조건문 덩어리에서 분리되어, 전용 정책 계산기인 `ApplyDisplayStatusResolver`가 담당한다.
- 신청내역 카운트는 `GET /mypage/entries` 한 응답에서 `counts`로 같이 내려간다.
- `counts`는 항상 전체 신청내역 기준이다.
- `totalCount`와 `items`는 현재 선택한 필터 기준이다.
- `노출 여부`는 아직 resolver 책임으로 옮기지 않았고, 조회 쿼리 조건이 계속 책임진다.

### 초등학생 설명

- 신청내역을 보여주는 규칙표를 따로 만든 상태다.
- 숫자 세는 상자는 항상 전체를 먼저 세고,
- 지금 보고 있는 탭에 맞는 목록만 따로 꺼내 보여준다.
- 그래서 "전체 몇 개", "추첨 몇 개", "선착 몇 개"는 언제 눌러도 기준이 같다.

### 보조설명

- 옷장에 옷이 10벌 있다고 생각하면,
- 왼쪽 종이에는 "전체 10벌, 파란 옷 3벌, 빨간 옷 7벌"을 적어 두고,
- 실제로 서랍을 열 때는 "파란 옷만 보여줘"처럼 일부만 꺼내는 구조다.

## 2. 왜 상태 계산기를 따로 분리했는가

### 전문가 설명

- 신청내역의 상태는 단순히 `EntryStatus` 하나만 보면 결정되지 않는다.
- 실제 화면 문구는 아래 조합으로 달라진다.
  - 이벤트 방식: `LOTTERY`, `FIRST_COME`
  - 이벤트 상태: `READY`, `IN_PROGRESS`, `CLOSING_SOON`, `END`, `DRAW_COMPLETED`
  - 사용자 진행 상태
  - 결과 상태
  - 같은 도메인 상태라도 화면 종류에 따른 차이
- 예를 들어 `PRE_SAVED`라도
  - 신청내역 목록에서는 `사전정보 저장`
  - 요약/대기 성격 화면에서는 `신청 대기`
  로 해석될 수 있다.
- 이런 정책은 서비스 메서드 내부에 흩어진 `if` 문으로 유지하면 변경 시점마다 버그가 생기기 쉽다.

### 초등학생 설명

- 같은 카드라도 어디에 놓느냐에 따라 이름표가 달라질 수 있다.
- 그래서 "카드 이름표를 붙이는 선생님"을 따로 만든 것이다.
- 이 선생님은 카드 종류와 지금 상황을 보고 맞는 이름표를 붙인다.

### 보조설명

- 축구팀에서 선수 배치표를 코치가 따로 관리하듯이,
- 신청 상태도 "누가 어디에 어떻게 보일지"를 따로 계산하는 담당자가 필요하다고 보면 된다.

## 3. 현재 백엔드 구조

### 전문가 설명

- 신청내역 조회 진입점:
  - `backend/main/src/main/java/com/kt/onrace/domain/mypage/service/ApplicationHistoryService.java`
- 신청 상태 계산기:
  - `backend/main/src/main/java/com/kt/onrace/domain/mypage/service/apply/ApplyDisplayStatusResolver.java`
- 주문 상태 계산기:
  - `backend/main/src/main/java/com/kt/onrace/domain/mypage/service/MyPageDisplayStatusResolver.java`

현재 흐름은 아래와 같다.

1. `ApplicationHistoryService`가 DB에서 신청내역을 조회한다.
2. 각 `Entry`와 `Event`를 resolver 입력 모델로 바꾼다.
3. `ApplyDisplayStatusResolver`가 상태/액션을 계산한다.
4. 계산 결과를 기존 API DTO에 맞는 문자열 형태로 변환한다.

### 초등학생 설명

1. 창고에서 신청 카드들을 꺼낸다.
2. 카드 내용을 읽기 쉬운 메모지로 바꾼다.
3. 규칙 선생님이 "이 카드는 당첨", "이 카드는 신청 가능" 같은 표지를 붙인다.
4. 마지막으로 화면에 맞게 예쁘게 정리해서 내보낸다.

### 보조설명

- 공장에서 원재료를 가져와서,
- 중간 가공을 한 다음,
- 검수 규칙을 거쳐,
- 포장해서 출고하는 흐름과 비슷하다.

## 4. 상태 계산기에서 실제로 쓰는 입력값

### 전문가 설명

현재 신청 전용 resolver 입력은 `ApplyDisplayStatusContext`다.

포함 값:

- `surface`
- `appType`
- `eventStatus`
- `userStatus`
- `resultStatus`

의도는 아래와 같다.

- `surface`
  - 같은 상태라도 화면 문구가 다를 수 있어서 필요하다.
- `appType`
  - 정책 축은 `EventType`이 아니라 `EventAppType`이다.
  - 즉 "마라톤/러닝"이 아니라 "추첨/선착" 기준이다.
- `userStatus`
  - 사용자가 어느 단계에 와 있는지 표현한다.
- `resultStatus`
  - 당첨/미당첨 결과를 따로 해석한다.

### 초등학생 설명

- 규칙 선생님은 5가지를 본다.
- 어디 화면에 보여줄지,
- 추첨인지 선착인지,
- 지금 이벤트가 열려 있는지 끝났는지,
- 사용자가 어디까지 했는지,
- 당첨됐는지 안 됐는지.

### 보조설명

- 병원 접수표를 본다고 생각하면,
- "어느 창구인지", "예약환자인지", "진료 중인지", "검사 결과가 나왔는지"를 함께 보고 안내를 정하는 것과 같다.

## 5. `EntryStatus`를 그대로 쓰지 않는 이유

### 전문가 설명

`EntryStatus`는 저장 모델로는 단순하지만, 정책 표현으로는 부족하다.

예:

- `WON`, `LOST`는 결과 상태다.
- `PRE_SAVED`, `RESERVED`, `APPLIED`는 사용자 진행 상태에 가깝다.

그래서 현재 구현은 아래처럼 의미를 다시 나눈다.

- `userStatus`
  - `PRE_SAVED`
  - `RESERVED`
  - `APPLIED`
- `resultStatus`
  - `NONE`
  - `WON`
  - `LOST`

즉 저장은 그대로 두고, 해석 단계에서 의미를 나눠 쓰는 방식이다.

### 초등학생 설명

- 원래 상자에는 스티커가 다 섞여 있다.
- 우리는 그걸 "지금 어디까지 했는지" 스티커와
- "마지막 결과가 뭐였는지" 스티커로 다시 나눠서 본다.

### 보조설명

- 한 장의 성적표에 출석, 시험 결과, 상벌점이 섞여 있으면 읽기 어렵다.
- 그래서 읽을 때는 "출석 정보", "시험 정보"처럼 나눠서 보는 것과 비슷하다.

## 6. 카운트와 필터는 지금 어떻게 동작하는가

### 전문가 설명

핵심 규칙은 아래 두 줄로 요약된다.

- `counts`는 전체 신청내역 기준
- `totalCount`, `items`는 현재 필터 기준

실제 코드 흐름:

1. `countApplicationHistoryEntries(userId)`로 전체 카운트 집계를 먼저 만든다.
2. 필터에 따라 `pickCount(...)`로 현재 탭의 `totalCount`를 고른다.
3. 목록 조회 쿼리에서만 `appTypeCondition(filter)`를 적용한다.

즉 `filter=LOTTERY`로 요청해도 응답 구조는 아래 의미를 가진다.

- `counts.all`: 전체 개수
- `counts.lottery`: 전체 중 추첨 개수
- `counts.firstCome`: 전체 중 선착 개수
- `totalCount`: 현재 요청 필터가 가리키는 개수
- `items`: 현재 요청 필터가 가리키는 목록

### 초등학생 설명

- 먼저 전체 상자에 카드가 몇 장 있는지 센다.
- 그 다음 "추첨 카드만 보여줘"라고 하면,
- 보여주는 카드만 추첨 카드로 바뀐다.
- 하지만 위쪽 숫자판은 여전히 전체 상자 기준 숫자를 가지고 있다.

### 보조설명

- 도서관에서 책을 찾을 때,
- 안내판에는 "전체 100권, 동화 30권, 과학 20권"이 적혀 있고,
- 네가 동화 코너에 가면 동화책만 보게 되는 구조다.

## 7. 빈 결과는 어떻게 처리되는가

### 전문가 설명

- 현재 탭 기준 `totalCount == 0`이면 빈 응답을 만든다.
- 이때도 `counts`는 같이 내려간다.
- 따라서 어떤 탭은 비어 있어도, 다른 탭의 개수 정보는 유지된다.

예:

- 전체는 5개
- 추첨은 0개
- 선착은 5개

`filter=LOTTERY` 요청 결과:

- `counts = { all: 5, lottery: 0, firstCome: 5 }`
- `totalCount = 0`
- `items = []`
- `emptyState.empty = true`

### 초등학생 설명

- 지금 열어본 서랍은 비어 있을 수 있다.
- 그래도 집 안에 다른 서랍들에 물건이 몇 개 있는지는 알 수 있다.

### 보조설명

- 냉장고 문 하나를 열었더니 우유 칸은 비었어도,
- 냉장고 전체에는 과일과 반찬이 남아 있는 상황과 같다.

## 8. 무엇이 바뀌었고 무엇은 안 바뀌었는가

### 전문가 설명

바뀐 것:

- 신청 상태 계산 로직이 전용 resolver로 분리되었다.
- 내부적으로 enum 기반 상태/액션 모델을 쓰게 되었다.
- 주문 상태 계산기는 신청 계산 로직과 분리되었다.

안 바뀐 것:

- 공개 API 응답의 상태값은 여전히 한글 문자열이다.
- 공개 action 값은 여전히 `NONE`, `EDIT`, `APPLY`, `CHECKOUT`이다.
- 카운트/필터는 기존처럼 `GET /mypage/entries` 하나로 처리한다.
- `visible` 책임은 여전히 쿼리 조건이 가진다.

### 초등학생 설명

- 밖에서 보는 화면 이름표는 그대로다.
- 하지만 안쪽에서 이름표를 붙이는 방법이 더 깔끔해졌다.

### 보조설명

- 건물 외관은 그대로 두고,
- 안쪽 배관과 전기선을 정리한 것과 비슷하다.

## 9. 왜 `visible`을 resolver로 옮기지 않았는가

### 전문가 설명

현재 구현에서는 "보여줄지 말지"를 쿼리에서 결정한다.

이유:

- 목록 개수
- 페이지 계산
- 실제 조회 대상

이 셋이 모두 같은 기준을 써야 하기 때문이다.

만약 resolver가 나중에 `visible=false`를 반환하도록 바꾸면,

- 쿼리 count
- 응답 `totalCount`
- 실제 `items`

가 서로 달라질 위험이 있다.

그래서 이번 단계에서는

- 쿼리: 노출 대상 선정
- resolver: 노출된 대상의 화면 상태 계산

으로 책임을 나눴다.

### 초등학생 설명

- 교실에 들어올 사람을 정하는 선생님과,
- 들어온 아이에게 이름표를 붙이는 선생님을 나눈 것이다.

### 보조설명

- 출입 통제 담당과 자리 배치 담당을 한 사람에게 동시에 맡기면 헷갈릴 수 있다.
- 그래서 문 앞 검사와 안쪽 안내를 분리했다고 생각하면 된다.

## 10. 현재 코드에서 꼭 기억해야 할 의미 차이

### 전문가 설명

- `counts`: 항상 전체 기준 탭 정보
- `totalCount`: 현재 필터 기준 전체 개수
- `items`: 현재 필터 기준 현재 페이지 목록
- `filter`: 현재 요청에 사용한 필터

이 네 값은 이름이 비슷해 보여도 의미가 다르다.

### 초등학생 설명

- 전체 숫자판
- 지금 보는 칸의 숫자
- 지금 꺼내온 카드들
- 지금 어떤 칸을 보고 있는지

이 네 개는 서로 다른 역할이다.

### 보조설명

- 지도, 현재 위치, 길 안내 목록, 선택한 목적지가 각각 다른 것과 같은 원리다.

## 11. 추천하는 다음 점검 포인트

### 전문가 설명

- `filter=LOTTERY` 또는 `filter=FIRST_COME` 요청에서도 `counts`가 전체 기준으로 유지되는지 명시적인 테스트를 추가하면 좋다.
- 향후 정책이 확장되면 아래를 다음 후보로 검토한다.
  - `soldOut`
  - `closingSoon`
  - `helperText`의 공개 API 노출
  - `visible` 책임 이동 여부

### 초등학생 설명

- 지금은 잘 정리돼 있지만,
- 나중에 규칙이 더 많아지면
- 숫자판이 계속 맞는지 다시 검사해야 한다.

### 보조설명

- 건물을 리모델링한 뒤에도
- 비상구 표시와 층별 안내판이 계속 맞는지 주기적으로 확인하는 것과 같다.

## 12. 관련 코드 위치

- 신청내역 조회 서비스
  - `backend/main/src/main/java/com/kt/onrace/domain/mypage/service/ApplicationHistoryService.java`
- 신청내역 상태 계산기
  - `backend/main/src/main/java/com/kt/onrace/domain/mypage/service/apply/ApplyDisplayStatusResolver.java`
- 신청내역 필터 정의
  - `backend/main/src/main/java/com/kt/onrace/domain/mypage/dto/MyPageApplicationHistoryFilter.java`
- 신청내역 목록 응답 DTO
  - `backend/main/src/main/java/com/kt/onrace/domain/mypage/dto/MyPageApplicationHistoryListResponseDto.java`
- 주문 상태 계산기
  - `backend/main/src/main/java/com/kt/onrace/domain/mypage/service/MyPageDisplayStatusResolver.java`

