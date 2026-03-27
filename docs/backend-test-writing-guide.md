# 백엔드 테스트 작성 가이드

- 작성일: 2026-03-26
- 대상: 백엔드 개발자, 기획자, 비전공자
- 목적: 이 프로젝트에서 어떤 테스트를 언제, 어떤 방식으로 작성해야 하는지 실제 코드 예시로 정리한다.

## 1. 한눈에 보는 결론

### 전문가 설명

- 이 프로젝트의 테스트는 크게 6종류로 나눠서 보면 된다.
- 순수 로직이면 Spring 없이 테스트한다.
- 외부 의존성이 있는 서비스는 Mockito로 단위 테스트한다.
- 컨트롤러는 `@WebMvcTest`로 HTTP 입출력만 검증한다.
- JPA 동작과 실제 저장소 조합은 `@DataJpaTest`로 검증한다.
- API 계약과 전체 조립 결과는 `@SpringBootTest`로 검증한다.
- 테스트는 "무엇을 검증하는지"가 가장 중요하고, "어디까지 실제를 올릴지"는 그 다음이다.

### 초등학생 설명

- 시험도 종류가 다르다.
- 계산 시험, 말하기 시험, 실험 시험이 따로 있듯이,
- 코드도 확인하려는 대상에 따라 다른 시험지를 써야 한다.

### 보조설명

- 전구가 안 켜질 때를 생각하면,
- 전구만 검사할 수도 있고,
- 스위치까지 같이 볼 수도 있고,
- 집 전체 전기선을 같이 볼 수도 있다.
- 테스트도 똑같이 범위를 정해서 검사해야 한다.

## 2. 이 프로젝트에서 쓰는 테스트 종류

## 2-1. 순수 단위 테스트

### 전문가 설명

- 대상: 정책 계산기, 변환 로직, 조건 분기
- 특징:
  - Spring을 띄우지 않는다.
  - 생성자로 객체를 직접 만든다.
  - 실행 속도가 가장 빠르다.
- 우리 프로젝트 예시:
  - `backend/main/src/test/java/com/kt/onrace/domain/mypage/service/apply/ApplyDisplayStatusResolverTest.java`
  - `backend/main/src/test/java/com/kt/onrace/domain/mypage/dto/MyPageAddressDtoTest.java`

이 유형은 입력과 출력이 명확할 때 가장 좋다.

예시 패턴:

```java
private final ApplyDisplayStatusResolver resolver = new ApplyDisplayStatusResolver();

@ParameterizedTest(name = "{0}")
@MethodSource("policyCases")
void resolveFollowsPolicyTable(...) {
    ApplyDisplayDecision result = resolver.resolve(context);

    assertThat(result.displayStatus()).isEqualTo(expectedStatus);
    assertThat(result.actionType()).isEqualTo(expectedActionType);
}
```

### 초등학생 설명

- 계산 문제를 풀 때는 교실 전체를 만들 필요가 없다.
- 문제 하나와 답안지만 있으면 된다.

### 보조설명

- 신청 상태 계산기처럼 "입력을 넣으면 결과가 바로 나오는" 코드는
- 실험실 장비 없이 계산기만 두고 검사하는 게 가장 효율적이다.

## 2-2. Mockito 기반 서비스 단위 테스트

### 전문가 설명

- 대상: 서비스가 다른 서비스나 외부 클라이언트를 조합하는 로직
- 특징:
  - `@ExtendWith(MockitoExtension.class)` 사용
  - 협력 객체는 `@Mock`으로 대체
  - 핵심은 "조립 결과가 맞는지" 검증하는 것
- 우리 프로젝트 예시:
  - `backend/main/src/test/java/com/kt/onrace/domain/mypage/service/MyPageAccountQueryServiceTest.java`

이 테스트는 실제 네트워크 호출이나 DB 조회 없이, 의존 객체의 반환값만 고정해서 서비스 조합 결과를 본다.

```java
@ExtendWith(MockitoExtension.class)
class MyPageAccountQueryServiceTest {

    @Mock
    private AuthAccountClient authAccountClient;

    @Mock
    private AddressService addressService;
}
```

### 초등학생 설명

- 친구들이 아직 안 왔어도 연극 연습은 할 수 있다.
- 친구 역할을 대신하는 인형을 두고 연습하는 것과 같다.

### 보조설명

- 외부 시스템이 항상 준비되어 있지 않기 때문에,
- 서비스 테스트에서는 "가짜 협력자"를 세워 놓고
- 내 서비스가 그 정보들을 잘 조립하는지만 본다.

## 2-3. DTO/값 객체 테스트

### 전문가 설명

- 대상: trim, format, join 같은 작은 변환 로직
- 특징:
  - 가장 작고 단순한 테스트
  - 실패 원인을 찾기 쉽다.
- 우리 프로젝트 예시:
  - `backend/main/src/test/java/com/kt/onrace/domain/mypage/dto/MyPageAddressDtoTest.java`

이 테스트는 주소 문자열 조합 규칙처럼, 작은 규칙이지만 화면 품질에 영향을 주는 부분을 안전하게 고정하는 데 좋다.

### 초등학생 설명

- 이름표를 붙일 때 띄어쓰기 하나가 틀리면 이상해진다.
- 이런 작은 규칙도 따로 확인해 보는 것이다.

### 보조설명

- 작다고 테스트를 빼면 안 된다.
- 작은 포맷 규칙이 화면 전체 문구를 망가뜨리는 경우가 많다.

## 2-4. 컨트롤러 슬라이스 테스트 (`@WebMvcTest`)

### 전문가 설명

- 대상: 요청 파라미터, 헤더, validation, HTTP 상태코드, 응답 JSON 구조
- 특징:
  - 웹 레이어만 올린다.
  - 서비스는 `@MockBean`으로 대체한다.
  - `MockMvc`로 요청을 보내고 `jsonPath`로 응답을 검증한다.
- 우리 프로젝트 예시:
  - `backend/main/src/test/java/com/kt/onrace/domain/mypage/controller/MyPageControllerTest.java`
  - `backend/main/src/test/java/com/kt/onrace/domain/address/controller/AddressApiControllerTest.java`

`MyPageControllerTest`는 `/mypage/entries` 응답의 구조를 확인하는 좋은 예시다.
`AddressApiControllerTest`는 `@ParameterizedTest`로 `/addresses`와 `/address` 두 경로를 한 테스트로 검증하는 좋은 예시다.

```java
@WebMvcTest(controllers = MyPageController.class)
class MyPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MyPageService myPageService;
}
```

### 초등학생 설명

- 가게 문 앞에서 손님을 받는 사람만 검사하는 시험이다.
- 주방까지 다 보지는 않고,
- 주문을 잘 받고, 말을 잘 돌려주는지만 보는 것이다.

### 보조설명

- 컨트롤러 테스트는 "서비스 로직이 맞는가"보다
- "HTTP 계약이 맞는가"를 보는 데 집중해야 한다.
- JSON 필드명, 상태코드, 필수 헤더 누락 처리를 여기서 잡는다.

## 2-5. JPA/저장소 결합 테스트 (`@DataJpaTest`)

### 전문가 설명

- 대상: 엔티티 저장, 조회, 정렬, 기본값 보정, 트랜잭션 안에서의 서비스 로직
- 특징:
  - JPA 관련 빈을 중심으로 테스트한다.
  - 실제 DB 동작을 가까운 수준으로 검증한다.
  - 서비스를 `@Import`해서 함께 올릴 수 있다.
- 우리 프로젝트 예시:
  - `backend/main/src/test/java/com/kt/onrace/domain/address/AddressServiceTest.java`

이 테스트는 "첫 배송지는 자동 기본배송지", "기본배송지 우선 정렬", "최대 10개 제한"처럼 저장소 상태가 중요한 규칙을 검증한다.

```java
@DataJpaTest
@Import({AddressService.class, JpaAuditingConfig.class, QueryDslConfig.class})
class AddressServiceTest {
}
```

### 초등학생 설명

- 장난감 상자에 정말로 물건을 넣었다 빼 보면서 확인하는 시험이다.
- 말로만 "들어갈 거야"가 아니라 진짜 넣어 본다.

### 보조설명

- 정렬, 기본값, 중복 검사, 저장 후 조회처럼
- 메모리 안 계산만으로는 확인이 부족한 규칙은
- DB 가까운 테스트로 보는 게 맞다.

## 2-6. 통합/계약 테스트 (`@SpringBootTest`)

### 전문가 설명

- 대상: 실제 API 응답 형태, 여러 계층이 합쳐진 최종 결과, 빈 상태와 실제 데이터 상태
- 특징:
  - 스프링 컨텍스트를 넓게 올린다.
  - 속도는 느리지만 회귀 방지 효과가 크다.
  - 시드 데이터를 만들어 실제 응답 구조를 검증한다.
- 우리 프로젝트 예시:
  - `backend/main/src/test/java/com/kt/onrace/domain/mypage/controller/MyPageApiContractTest.java`

이 테스트는 `/mypage`, `/mypage/entries`, `/mypage/orders`, `/mypage/address`를 실제로 호출해
응답 shape, 빈 결과, 필터별 결과, 액션 타입까지 한 번에 검증한다.

### 초등학생 설명

- 교실, 복도, 운동장까지 다 열고 학교가 진짜 잘 돌아가는지 보는 큰 시험이다.

### 보조설명

- 이 테스트는 비용이 크다.
- 그래서 모든 규칙을 여기에 몰아넣지 말고,
- 핵심 계약과 실제 연결 결과만 남겨야 유지가 쉽다.

## 3. 어떤 테스트를 골라야 하는가

### 전문가 설명

아래 기준으로 고르면 된다.

- 입력과 출력만 있으면 된다:
  - 순수 단위 테스트
- 의존 객체 반환값을 조합하는 서비스다:
  - Mockito 기반 서비스 단위 테스트
- HTTP 요청/응답 구조를 확인해야 한다:
  - `@WebMvcTest`
- DB 저장 결과와 조회 규칙이 중요하다:
  - `@DataJpaTest`
- 실제 API 계약이 깨졌는지 끝단에서 확인해야 한다:
  - `@SpringBootTest`

### 초등학생 설명

- 무엇을 검사하고 싶은지 먼저 정하면,
- 어떤 시험지를 써야 할지가 보인다.

### 보조설명

- 작은 계산 문제에 학교 전체 시험을 쓰면 너무 무겁고,
- 반대로 학교 전체 문제를 계산 문제 시험지로 보면 중요한 걸 놓친다.

## 4. 이 프로젝트에서 좋은 테스트를 쓰는 규칙

### 전문가 설명

- 한 테스트는 한 가지 행동만 검증한다.
- 테스트 이름은 결과가 보이게 적는다.
- 성공 케이스와 실패 케이스를 같이 쓴다.
- 비슷한 케이스가 많으면 `@ParameterizedTest`와 `@MethodSource`를 쓴다.
- 픽스처 생성이 반복되면 private helper 메서드로 뺀다.
- 컨트롤러 테스트는 서비스 구현을 다시 검증하지 않는다.
- 통합 테스트는 "최종 계약"에 집중하고, 세부 분기까지 다 넣지 않는다.

우리 프로젝트에서 바로 볼 수 있는 패턴:

- 정책표 테스트:
  - `ApplyDisplayStatusResolverTest`
- 경로만 다른 API 재사용 테스트:
  - `AddressApiControllerTest`
- 빈 결과/예외 처리 테스트:
  - `MyPageControllerTest`, `AddressServiceTest`
- 실제 계약 회귀 테스트:
  - `MyPageApiContractTest`

### 초등학생 설명

- 시험 문제는 한 번에 하나씩 물어봐야 한다.
- 너무 많은 걸 한 문제에 넣으면 뭐가 틀렸는지 모른다.

### 보조설명

- 테스트가 실패했을 때 원인을 빨리 찾을 수 있어야 좋은 테스트다.
- 그래서 "작게", "명확하게", "같은 패턴은 묶어서" 작성하는 게 중요하다.

## 5. 프로젝트 예시로 배우는 작성법

## 5-1. 정책표 테스트는 표처럼 쓴다

### 전문가 설명

`ApplyDisplayStatusResolverTest`는 신청 상태 정책처럼 조합이 많은 로직을 테스트할 때 좋은 형태다.

핵심 포인트:

- `@ParameterizedTest(name = "{0}")`로 케이스 설명을 이름에 넣는다.
- `@MethodSource("policyCases")`로 정책표를 한 곳에 모은다.
- 각 행은 "설명, 입력, 기대 상태, 기대 액션"으로 구성한다.

이 방식이 좋은 이유:

- 정책표와 테스트가 1:1로 대응된다.
- 신규 케이스 추가가 쉽다.
- 어떤 조합이 빠졌는지 찾기 쉽다.

### 초등학생 설명

- 규칙이 많으면 줄 세워서 표로 적는 게 제일 보기 쉽다.

### 보조설명

- 신청내역 상태처럼 경우의 수가 많은 로직은
- 케이스를 문장으로 늘어놓기보다 표 한 줄씩 관리하는 쪽이 훨씬 안전하다.

## 5-2. 컨트롤러 테스트는 JSON 계약을 본다

### 전문가 설명

`MyPageControllerTest`는 서비스가 리턴한 DTO가 HTTP 응답으로 어떻게 보이는지 검증한다.

핵심 포인트:

- `mockMvc.perform(get("/mypage/entries"))`
- `jsonPath("$.data.counts.all").value(1)`
- `jsonPath("$.data.items[0].action.type").value("NONE")`

여기서는 내부 서비스 계산 로직을 다시 검증하지 않는다.
오직 아래만 본다.

- 요청이 잘 들어가는가
- 헤더 누락 시 잘 막히는가
- 응답 필드가 계약대로 나가는가

### 초등학생 설명

- 택배 상자를 열어 보고 안에 물건 이름표가 제대로 붙었는지 확인하는 것이다.

### 보조설명

- 컨트롤러 테스트는 "안의 물건을 어떻게 만들었는가"보다
- "밖으로 어떤 상자 모양으로 나가는가"를 보는 시험이다.

## 5-3. 중복 경로 테스트는 파라미터화한다

### 전문가 설명

`AddressApiControllerTest`는 `/addresses`와 `/address` 두 경로를 같은 규칙으로 검증한다.

핵심 포인트:

- `routeSpecs()`에서 경로 조합을 만든다.
- 같은 테스트 메서드를 두 경로에 재사용한다.

이 패턴은 호환 API가 같이 살아 있을 때 특히 유용하다.

### 초등학생 설명

- 같은 문제를 A반, B반에 똑같이 내는 것과 같다.

### 보조설명

- URL만 다르고 규칙은 같다면 테스트도 복붙하지 말고 묶어야 한다.
- 그래야 한쪽만 수정되고 다른 한쪽은 빠지는 버그를 줄일 수 있다.

## 5-4. 저장 규칙은 예외까지 함께 본다

### 전문가 설명

`AddressServiceTest`는 성공 케이스와 실패 케이스를 같이 잡고 있다.

예:

- 첫 배송지는 자동 기본배송지
- trim 후 중복 별칭 금지
- 전화번호 형식 오류 예외
- 최대 10개 제한

이런 규칙은 정상 흐름만 보면 절반짜리 테스트다.
반드시 예외 코드까지 같이 고정해야 한다.

### 초등학생 설명

- 잘 되는 것도 시험이고,
- 하면 안 되는 걸 했을 때 제대로 막는 것도 시험이다.

### 보조설명

- 실무에서는 "성공"보다 "잘못된 입력을 어떻게 막는가"가 더 자주 문제를 만든다.

## 5-5. 계약 테스트는 빈 상태도 꼭 본다

### 전문가 설명

`MyPageApiContractTest`는 실제 데이터가 있는 사용자와 빈 사용자를 둘 다 만든다.

이 테스트에서 배울 점:

- 실제 데이터가 있을 때 응답 구조 검증
- 데이터가 없을 때도 응답 shape 유지
- 필터별 결과가 다를 때도 공통 필드 유지

특히 신청내역 API는 아래처럼 봐야 한다.

- `counts`는 전체 기준인지
- `totalCount`는 필터 기준인지
- `items`가 비면 `emptyState`가 맞게 오는지

### 초등학생 설명

- 가방이 찼을 때랑 비었을 때 둘 다 열어 봐야 진짜 확인이 된다.

### 보조설명

- 빈 결과 처리는 항상 마지막에 빠지기 쉽다.
- 그런데 실제 운영에서는 빈 결과 화면이 매우 자주 나오므로 반드시 테스트해야 한다.

## 6. 신청내역 정책 테스트를 추가할 때 쓰는 방법

### 전문가 설명

신청내역 상태 계산기 테스트를 추가할 때는 아래 순서로 가면 된다.

1. 정책표에서 새 케이스를 한 줄 뽑는다.
2. `ApplyDisplayStatusResolverTest.policyCases()`에 `Arguments.of(...)`를 한 줄 추가한다.
3. 입력은 `surface`, `appType`, `eventStatus`, `userStatus`, `resultStatus`를 명시한다.
4. 기대값은 `displayStatus`, `actionType`, `actionEnabled`를 넣는다.
5. helper text를 아직 쓰지 않는다면 `null` 유지까지 같이 확인한다.

예시:

```java
Arguments.of(
    "신청내역 + 추첨 + 마감 + 응모완료는 결과 발표 대기",
    context(APPLICATION_HISTORY, LOTTERY, END, APPLIED, NONE),
    RESULT_PENDING,
    NONE,
    false
)
```

중요한 원칙:

- 이 테스트는 Spring을 띄우지 않는다.
- DB를 붙이지 않는다.
- 입력과 기대 결과의 매핑만 본다.

### 초등학생 설명

- 새 규칙이 생기면 표에 한 줄 더 적고,
- 답이 맞는지만 보면 된다.

### 보조설명

- 정책 엔진 테스트는 빠르고 단순해야 한다.
- 무거운 환경을 붙이면 케이스를 많이 늘리기 어려워진다.

## 7. 테스트 이름은 어떻게 짓는가

### 전문가 설명

이 프로젝트에서는 아래 방식이 읽기 좋다.

- 메서드 이름:
  - 영어 camelCase
  - 동작 중심
- `@DisplayName`:
  - 한글 문장
  - 왜 중요한지 드러나게 작성

좋은 예:

- `getAccountReturnsEmptyAddressWhenDefaultAddressMissing`
- `duplicateLabelIsRejectedIgnoringTrimAndCase`
- `resolveFollowsPolicyTable`

피해야 할 예:

- `test1`
- `successCase`
- `works`

### 초등학생 설명

- 시험지 제목만 봐도 무슨 문제인지 알아야 한다.

### 보조설명

- 테스트 이름은 실패 로그에 그대로 찍힌다.
- 이름이 구체적일수록 디버깅 시간이 줄어든다.

## 8. 실행은 어떻게 하는가

### 전문가 설명

백엔드 루트에서 필요한 테스트만 골라 실행하면 된다.

```bash
cd /Users/wusu/dev/On-Race/backend
./gradlew :main:test --tests "com.kt.onrace.domain.mypage.service.apply.ApplyDisplayStatusResolverTest"
./gradlew :main:test --tests "com.kt.onrace.domain.address.controller.AddressApiControllerTest"
./gradlew :main:test --tests "com.kt.onrace.domain.mypage.controller.MyPageApiContractTest"
```

테스트를 새로 작성할 때는

- 먼저 가장 작은 단위 테스트 실행
- 그 다음 관련 컨트롤러 테스트 실행
- 마지막으로 필요하면 계약 테스트 실행

순서가 효율적이다.

### 초등학생 설명

- 작은 시험부터 풀고,
- 그다음 큰 시험을 보면 된다.

### 보조설명

- 무거운 테스트부터 돌리면 시간이 오래 걸린다.
- 그래서 빠른 테스트로 먼저 규칙을 맞추고, 마지막에 큰 테스트로 확인하는 게 좋다.

## 9. 지금 바로 따라 쓰는 체크리스트

### 전문가 설명

- 이 로직은 순수 계산인가, HTTP인가, DB인가를 먼저 정한다.
- 가장 작은 테스트 유형을 고른다.
- 성공, 실패, 빈 결과 중 무엇을 검증할지 정한다.
- 반복 케이스면 `@ParameterizedTest`로 묶는다.
- 결과가 문자열이면 의미 enum이나 에러 코드까지 같이 본다.
- 테스트 이름만 읽어도 요구사항이 떠오르게 적는다.
- 실행은 관련 테스트만 골라 빠르게 확인한다.

### 초등학생 설명

- 어떤 시험인지 먼저 고르고,
- 문제를 하나씩 만들고,
- 답이 맞는지 확인하면 된다.

### 보조설명

- 테스트 작성은 "많이 쓰는 것"보다
- "정확한 범위에 맞는 테스트를 쓰는 것"이 더 중요하다.

## 10. 관련 예시 파일

- `backend/main/src/test/java/com/kt/onrace/domain/mypage/service/apply/ApplyDisplayStatusResolverTest.java`
- `backend/main/src/test/java/com/kt/onrace/domain/mypage/service/MyPageAccountQueryServiceTest.java`
- `backend/main/src/test/java/com/kt/onrace/domain/mypage/dto/MyPageAddressDtoTest.java`
- `backend/main/src/test/java/com/kt/onrace/domain/mypage/controller/MyPageControllerTest.java`
- `backend/main/src/test/java/com/kt/onrace/domain/address/controller/AddressApiControllerTest.java`
- `backend/main/src/test/java/com/kt/onrace/domain/address/AddressServiceTest.java`
- `backend/main/src/test/java/com/kt/onrace/domain/mypage/controller/MyPageApiContractTest.java`
