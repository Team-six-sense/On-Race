# Auth 모듈 분석 보고서

> 작성일: 2026-03-26
> 대상 브랜치: `feat/backend/auth/terms-seed`
> 분석 범위: `backend/auth`, `backend/gateway` (JWT 필터), `backend/common` (JWT, 보안 유틸)

---

## 1. 현재 구조 분석

### 1.1 모듈 구성

```
Client → Gateway(8080) → [X-Gateway-Token, X-User-Id, X-User-Role] → Auth(8081)
```

| 계층 | 주요 클래스 | 역할 |
|------|------------|------|
| **Gateway 필터** | `JwtAuthenticationWebFilter` | JWT 검증, `X-User-Id`/`X-User-Role` 헤더 주입 |
| **Gateway 보안** | `SecurityConfig` (gateway) | WebFlux 보안 체인, `/auth/**` permitAll |
| **Gateway 방어** | `BotDetectionFilter`, `RateLimitConfig`, `WaitingRoomFilter` | 봇 탐지, Rate Limit, 대기열 |
| **Auth 보안** | `SecurityConfig` (auth) | Servlet 보안 체인, OAuth2 설정 |
| **Auth 방어** | `GatewayAccessFilter` (common) | `X-Gateway-Token` 검증으로 직접 접근 차단 |
| **컨트롤러** | `AuthController`, `AccountController`, `OAuthController`, `EmailController`, `SmsController`, `PasswordResetController`, `TermController` | 7개 컨트롤러 |
| **서비스** | `AuthService`, `AccountService`, `OAuthService`, `TokenStoreService`, `EmailVerifyService`, `SmsVerifyService`, `PasswordResetService`, `LoginHistoryService`, `TermService` | 9개 서비스 |
| **엔티티** | `User`, `LoginHistory`, `TermMaster`, `TermVersion`, `TermUser`, `EmailSend` | 6개 엔티티 |
| **JWT** | `JwtTokenProvider` (common), `JwtProperties` (common) | HMAC-SHA 기반, Access + Refresh 토큰 |
| **Redis** | `TokenStoreService`, `RedisKeyGenerator` | Refresh 토큰, JTI 블랙리스트, 인증코드, 쿨다운/카운터 |

### 1.2 인증 플로우 요약

```
[회원가입] 이메일 인증 → SMS 인증 → signup → DB 저장 + 약관 동의 + main 서비스 동기화
[로그인]   실패 횟수 체크 → DB 조회(ACTIVE) → 비밀번호 검증 → JWT 발급 + Redis 저장
[토큰 갱신] Refresh 토큰 검증 → Redis 대조 → 새 Access + Refresh 발급 (Rotation)
[로그아웃] Access 토큰 JTI 블랙리스트 등록 + Refresh 토큰 삭제
[OAuth]   프론트에서 provider 인증 후 → `/oauth/{provider}` → 자동 가입/로그인 → JWT 발급
```

---

## 2. 보안상 개선 지점

### 2.1 [심각] Gateway에서 JTI 블랙리스트 미검증

**현재**: `JwtAuthenticationWebFilter`는 JWT 서명/만료만 검증하고, **블랙리스트(로그아웃된 토큰) 체크를 하지 않음**.

```java
// JwtAuthenticationWebFilter.java:41 — 이 검증만 수행
jwtTokenProvider.validateAccessTokenOrThrow(token);
// ❌ tokenStoreService.isBlacklisted(jti) 호출 없음
```

**위험**: 로그아웃한 사용자의 Access 토큰이 만료 전까지 계속 유효. 계정 탈취 시 로그아웃으로 세션을 끊을 수 없음.

**심각도**: **Critical** — 로그아웃 무효화의 핵심 보안 장치가 작동하지 않음.

---

### 2.2 [심각] OAuth2 Success Handler — URL 쿼리로 토큰 노출

**현재**: `OAuth2AuthenticationSuccessHandler`가 토큰을 **redirect URL의 쿼리 파라미터**로 전달.

```java
// OAuth2AuthenticationSuccessHandler.java:46-49
String targetUrl = UriComponentsBuilder.fromUriString(oAuthProperties.getRedirectUri())
    .queryParam("accessToken", accessToken)    // ❌ URL에 노출
    .queryParam("refreshToken", refreshToken)  // ❌ URL에 노출
    .build().toUriString();
```

**위험**:
- 브라우저 히스토리에 토큰 기록
- Referrer 헤더로 외부 서비스에 토큰 유출
- 서버 액세스 로그에 토큰 기록
- 프록시/CDN 로그에 토큰 기록

**심각도**: **High** — 토큰 유출 경로가 다수 존재.

---

### 2.3 [높음] Auth SecurityConfig — `/**` permitAll로 사실상 인가 무효

```java
// SecurityConfig.java:62 (auth)
request.requestMatchers("/**").permitAll();  // ← 모든 요청 허용
request.anyRequest().authenticated();        // ← 도달 불가 (dead code)
```

**현재 의도**: "auth 서비스는 Gateway 뒤에 위치하며 JWT 검증은 Gateway가 담당" 이라는 주석이 있음.
**문제**: `GatewayAccessFilter`가 `X-Gateway-Token`으로 직접 접근을 차단하고 있어 실질적 보안은 유지되지만, **SecurityConfig 자체가 의미 없는 상태**. 만약 `GatewayAccessFilter`에 우회 취약점이 발견되면 모든 내부 엔드포인트가 무방비.

**심각도**: **Medium** — 방어 심층 원칙(Defense in Depth) 위반.

---

### 2.4 [높음] GatewayAccessFilter — 타이밍 공격 취약

```java
// GatewayAccessFilter.java:34
if (gatewayToken == null || !gatewayToken.equals(gatewaySecret)) {
```

`String.equals()`는 일치하지 않는 첫 바이트에서 즉시 반환 → 응답 시간 차이로 secret을 한 바이트씩 추론 가능.

**심각도**: **Medium** — 네트워크 노이즈로 실제 공격 난이도는 높지만, `MessageDigest.isEqual()` 한 줄로 해결 가능.

---

### 2.5 [높음] OAuth 로그인 시 비활성 사용자 체크 누락

```java
// OAuthService.java:30-31
User user = userRepository.findByProviderIdAndAuthProvider(request.providerId(), provider)
    .orElseGet(() -> registerOAuthUser(request, provider));
// ❌ user.isActive() 체크 없음 → 탈퇴한 사용자도 OAuth로 로그인 가능
```

**심각도**: **High** — 탈퇴 처리가 우회됨.

---

### 2.6 [중간] OAuth2 쿠키 — Secure 플래그 미설정

```java
// HttpCookieOAuth2AuthorizationRequestRepository.java:73
cookie.setHttpOnly(true);
// ❌ cookie.setSecure(true) 누락
// ❌ cookie.setSameSite("Lax") 미설정
```

**위험**: HTTP 연결에서 쿠키가 평문 전송될 수 있음. CSRF 공격 벡터 존재.

**심각도**: **Medium** — 프로덕션이 HTTPS라면 리스크 낮지만, 명시적으로 설정해야 함.

---

### 2.7 [중간] LoginRequest에 @SensitiveLog 누락

`SignupRequest`에는 `@SensitiveLog`가 있지만, `LoginRequest`에는 없음.
→ `@ApiLog` AOP에서 로그인 요청의 비밀번호가 로그에 기록될 수 있음.

**심각도**: **Medium** — 로그를 통한 비밀번호 유출.

---

### 2.8 [낮음] Refresh Token에 JTI 미포함

```java
// JwtTokenProvider.java:63-68 — Refresh Token 생성
return Jwts.builder()
    .subject(String.valueOf(userId))
    // ❌ .id(UUID.randomUUID().toString()) 없음
    .issuedAt(now)
    .expiration(expiry)
    .signWith(getSigningKey())
    .compact();
```

**현재 대안**: Redis에서 refresh token을 통째로 비교하므로 실질적 문제는 적음.
**그러나**: 향후 refresh token 개별 추적/감사 시 식별자가 없어 어려움.

**심각도**: **Low** — 현재 Redis 기반 대조로 커버되나, 감사 추적을 위해 추가 권장.

---

## 3. 리팩토링 목표

| # | 목표 | 타입 | 우선순위 |
|---|------|------|----------|
| G1 | Gateway JTI 블랙리스트 검증 추가 | 보안 버그 수정 | **P0** |
| G2 | OAuth 로그인 시 사용자 상태 검증 | 보안 버그 수정 | **P0** |
| G3 | OAuth2 토큰 전달 방식 변경 (쿼리 → fragment 또는 POST) | 보안 개선 | **P1** |
| G4 | GatewayAccessFilter에 constant-time 비교 적용 | 보안 개선 | **P1** |
| G5 | LoginRequest에 @SensitiveLog 추가 | 보안 개선 | **P1** |
| G6 | OAuth2 쿠키 Secure/SameSite 플래그 추가 | 보안 개선 | **P2** |
| G7 | Auth SecurityConfig 정리 (명시적 인가 규칙 설정) | 코드 품질 | **P2** |
| G8 | Refresh Token에 JTI 추가 | 확장성 | **P3** |

---

## 4. 최소 수정 범위

각 목표별 변경 대상 파일:

### G1 — Gateway JTI 블랙리스트 검증
| 파일 | 변경 내용 |
|------|----------|
| `gateway/.../filter/JwtAuthenticationWebFilter.java` | `TokenStoreService.isBlacklisted(jti)` 호출 추가 |
| `gateway/build.gradle` | Redisson 의존성 확인 (gateway가 이미 Redis 사용 중이므로 추가 불필요할 수 있음) |
| (선택) 새 서비스 or 직접 Redis 호출 | Gateway는 WebFlux이므로 reactive Redis 접근 필요 |

> **주의**: Gateway는 WebFlux(reactive) 기반이고, `TokenStoreService`는 Servlet(blocking) 기반 Redisson 사용. Gateway에서는 `ReactiveRedisTemplate` 또는 reactive Redisson API 사용 필요.

### G2 — OAuth 사용자 상태 검증
| 파일 | 변경 내용 |
|------|----------|
| `auth/.../service/OAuthService.java` | `user.isActive()` 체크 추가 (3줄) |

### G3 — OAuth2 토큰 전달 방식
| 파일 | 변경 내용 |
|------|----------|
| `auth/.../common/handler/OAuth2AuthenticationSuccessHandler.java` | fragment(`#`) 방식 또는 중간 페이지 POST 방식으로 변경 |
| (프론트 연동 필요) | 프론트에서 fragment 파싱 or postMessage 수신 로직 |

### G4 — Constant-time 비교
| 파일 | 변경 내용 |
|------|----------|
| `common/.../filter/GatewayAccessFilter.java` | `equals()` → `MessageDigest.isEqual()` (1줄) |

### G5 — LoginRequest 로그 마스킹
| 파일 | 변경 내용 |
|------|----------|
| `auth/.../dto/LoginRequest.java` | `@SensitiveLog` 어노테이션 추가 (1줄) |

### G6 — OAuth2 쿠키 플래그
| 파일 | 변경 내용 |
|------|----------|
| `auth/.../common/oauth2/HttpCookieOAuth2AuthorizationRequestRepository.java` | `setSecure(true)`, SameSite 설정 추가 |

### G7 — Auth SecurityConfig 정리
| 파일 | 변경 내용 |
|------|----------|
| `auth/.../common/config/SecurityConfig.java` | `/**` permitAll 제거, 명시적 경로별 규칙 설정 |

### G8 — Refresh Token JTI
| 파일 | 변경 내용 |
|------|----------|
| `common/.../security/JwtTokenProvider.java` | `generateRefreshToken()`에 `.id()` 추가 (1줄) |

---

## 5. 단계별 작업 계획

### Phase 1: 즉시 수정 (보안 버그) — P0

| 단계 | 작업 | 예상 파일 수 | 의존성 |
|------|------|-------------|--------|
| 1-1 | **OAuthService 비활성 사용자 체크** | 1 | 없음 |
| 1-2 | **LoginRequest @SensitiveLog 추가** | 1 | 없음 |
| 1-3 | **GatewayAccessFilter constant-time 비교** | 1 | 없음 |

> 1-1 ~ 1-3은 각각 독립적이며, 1~3줄 변경. **하나의 PR로 묶어도 무방**.

| 단계 | 작업 | 예상 파일 수 | 의존성 |
|------|------|-------------|--------|
| 1-4 | **Gateway JTI 블랙리스트 검증** | 2~3 | Gateway의 Redis 접근 방식 결정 필요 |

> 1-4는 Gateway가 reactive 환경이므로 별도 설계 필요. **별도 PR 권장**.

### Phase 2: 보안 개선 — P1~P2

| 단계 | 작업 | 예상 파일 수 | 의존성 |
|------|------|-------------|--------|
| 2-1 | **OAuth2 쿠키 Secure/SameSite 플래그** | 1 | 없음 |
| 2-2 | **Auth SecurityConfig 명시적 인가 규칙** | 1 | 전체 엔드포인트 목록 확인 필요 |
| 2-3 | **OAuth2 토큰 전달 방식 변경** | 1~2 | 프론트엔드와 협의 필요 |

### Phase 3: 확장성 — P3

| 단계 | 작업 | 예상 파일 수 | 의존성 |
|------|------|-------------|--------|
| 3-1 | **Refresh Token JTI 추가** | 1 | 기존 토큰 호환성 확인 |

---

## 부록: 잘 되어 있는 점

분석 과정에서 확인된 **긍정적인 설계 사항**:

- Refresh Token Rotation 적용 (갱신 시 새 refresh token 발급)
- Access Token에 JTI 포함 + JTI 블랙리스트 구조 (Gateway 연동만 누락)
- 이메일/SMS 인증의 `consumeVerification()` — `getAndDelete()`로 race condition 방지
- 로그인 실패 횟수 제한 + CAPTCHA 분기
- 인증코드 발송 쿨다운 + 일일 한도 제한
- Gateway `stripInternalHeaders()` — 외부에서 X-User-Id 헤더 조작 방지
- 비밀번호 재설정의 2단계 검증 (토큰 확인 → 인증 플래그 → 변경)
- `LoginHistoryService`의 `REQUIRES_NEW` 전파 — 로그인 실패 시에도 이력 독립 저장
- `SecureRandom` 사용으로 인증코드 예측 방지
- `@SensitiveLog`를 통한 회원가입 요청 로그 마스킹
