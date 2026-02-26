package com.kt.onrace.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.kt.onrace.auth.dto.LoginRequest;
import com.kt.onrace.auth.dto.LoginResponse;
import com.kt.onrace.auth.dto.TokenRefreshRequest;
import com.kt.onrace.auth.dto.TokenRefreshResponse;
import com.kt.onrace.auth.entity.User;
import com.kt.onrace.auth.repository.UserRepository;
import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.common.security.JwtProperties;
import com.kt.onrace.common.security.JwtTokenProvider;

@ExtendWith(MockitoExtension.class)
class AuthServiceLoginTest {

	@InjectMocks
	private AuthService authService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@Mock
	private JwtProperties jwtProperties;

	@Mock
	private TokenStoreService tokenStoreService;

	private User testUser;

	@BeforeEach
	void setUp() {
		testUser = User.createUser("test@test.com", "encodedPw");
		ReflectionTestUtils.setField(testUser, "id", 1L);
	}

	// ── login ──────────────────────────────────────────────────

	@Test
	@DisplayName("로그인 성공: 올바른 자격증명으로 Access/Refresh Token 반환")
	void login_success() {
		// given
		LoginRequest request = new LoginRequest("test@test.com", "password123!");

		given(userRepository.findByEmailAndIsDeletedFalse("test@test.com")).willReturn(Optional.of(testUser));
		given(passwordEncoder.matches("password123!", "encodedPw")).willReturn(true);
		given(jwtTokenProvider.generateAccessToken(1L, "test@test.com", "USER")).willReturn("access-token");
		given(jwtTokenProvider.generateRefreshToken(1L)).willReturn("refresh-token");
		given(jwtProperties.getRefreshTokenExpiration()).willReturn(43200000L);
		given(jwtProperties.getAccessTokenExpiration()).willReturn(300000L);

		// when
		LoginResponse response = authService.login(request);

		// then
		assertThat(response.accessToken()).isEqualTo("access-token");
		assertThat(response.refreshToken()).isEqualTo("refresh-token");
		assertThat(response.tokenType()).isEqualTo("Bearer");
		assertThat(response.expiresIn()).isEqualTo(300000L);

		then(tokenStoreService).should().saveRefreshToken(1L, "refresh-token", 43200000L);
	}

	@Test
	@DisplayName("로그인 실패: 존재하지 않는 loginId")
	void login_userNotFound() {
		// given
		LoginRequest request = new LoginRequest("unknown@test.com", "password123!");

		given(userRepository.findByEmailAndIsDeletedFalse("unknown@test.com")).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> authService.login(request))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(BusinessErrorCode.AUTH_NOT_FOUND_USER);
	}

	@Test
	@DisplayName("로그인 실패: 비밀번호 불일치")
	void login_passwordMismatch() {
		// given
		LoginRequest request = new LoginRequest("test@test.com", "wrongPassword!");

		given(userRepository.findByEmailAndIsDeletedFalse("test@test.com")).willReturn(Optional.of(testUser));
		given(passwordEncoder.matches("wrongPassword!", "encodedPw")).willReturn(false);

		// when & then
		assertThatThrownBy(() -> authService.login(request))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(BusinessErrorCode.AUTH_INVALID_PASSWORD);
	}

	// ── refreshToken ───────────────────────────────────────────

	@Test
	@DisplayName("토큰 재발급 성공: 유효한 Refresh Token으로 새 Access Token 반환")
	void refreshToken_success() {
		// given
		TokenRefreshRequest request = new TokenRefreshRequest("valid-refresh-token");

		given(jwtTokenProvider.validateToken("valid-refresh-token")).willReturn(true);
		given(jwtTokenProvider.getUserId("valid-refresh-token")).willReturn(1L);
		given(tokenStoreService.getRefreshToken(1L)).willReturn(Optional.of("valid-refresh-token"));
		given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
		given(jwtTokenProvider.generateAccessToken(1L, "test@test.com", "USER")).willReturn("new-access-token");
		given(jwtProperties.getAccessTokenExpiration()).willReturn(300000L);

		// when
		TokenRefreshResponse response = authService.refreshToken(request);

		// then
		assertThat(response.accessToken()).isEqualTo("new-access-token");
		assertThat(response.expiresIn()).isEqualTo(300000L);
	}

	@Test
	@DisplayName("토큰 재발급 실패: 유효하지 않은 JWT (서명 오류 또는 만료)")
	void refreshToken_invalidToken() {
		// given
		TokenRefreshRequest request = new TokenRefreshRequest("invalid-token");

		given(jwtTokenProvider.validateToken("invalid-token")).willReturn(false);

		// when & then
		assertThatThrownBy(() -> authService.refreshToken(request))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(BusinessErrorCode.AUTH_INVALID_REFRESH_TOKEN);
	}

	@Test
	@DisplayName("토큰 재발급 실패: Redis에 저장된 Refresh Token 없음 (로그아웃 상태)")
	void refreshToken_notInRedis() {
		// given
		TokenRefreshRequest request = new TokenRefreshRequest("valid-refresh-token");

		given(jwtTokenProvider.validateToken("valid-refresh-token")).willReturn(true);
		given(jwtTokenProvider.getUserId("valid-refresh-token")).willReturn(1L);
		given(tokenStoreService.getRefreshToken(1L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> authService.refreshToken(request))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(BusinessErrorCode.AUTH_INVALID_REFRESH_TOKEN);
	}

	@Test
	@DisplayName("토큰 재발급 실패: Redis 값과 요청 토큰 불일치 (재사용 공격)")
	void refreshToken_tokenMismatch() {
		// given
		TokenRefreshRequest request = new TokenRefreshRequest("valid-refresh-token");

		given(jwtTokenProvider.validateToken("valid-refresh-token")).willReturn(true);
		given(jwtTokenProvider.getUserId("valid-refresh-token")).willReturn(1L);
		given(tokenStoreService.getRefreshToken(1L)).willReturn(Optional.of("different-token-in-redis"));

		// when & then
		assertThatThrownBy(() -> authService.refreshToken(request))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(BusinessErrorCode.AUTH_INVALID_REFRESH_TOKEN);
	}
}
