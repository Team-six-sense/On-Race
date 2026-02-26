package com.kt.onrace.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Date;
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

import com.kt.onrace.auth.api.dto.WithdrawRequest;
import com.kt.onrace.auth.domain.entity.Gender;
import com.kt.onrace.auth.domain.entity.User;
import com.kt.onrace.auth.domain.repository.UserRepository;
import com.kt.onrace.auth.infrastructure.TokenStoreService;
import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.common.security.JwtProperties;
import com.kt.onrace.common.security.JwtTokenProvider;

@ExtendWith(MockitoExtension.class)
class AuthServiceLogoutWithdrawTest {

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
		testUser = User.createUser("testuser", "테스터", "encodedPw", "test@test.com", "010-1234-5678", Gender.MALE);
		ReflectionTestUtils.setField(testUser, "id", 1L);
	}

	// ── logout ─────────────────────────────────────────────────

	@Test
	@DisplayName("로그아웃 성공: JTI 블랙리스트 등록 + Refresh Token 삭제")
	void logout_success() {
		// given
		Date futureExpiration = new Date(System.currentTimeMillis() + 300000L);
		given(jwtTokenProvider.getJti("access-token")).willReturn("test-jti");
		given(jwtTokenProvider.getExpiration("access-token")).willReturn(futureExpiration);

		// when
		authService.logout(1L, "access-token");

		// then
		then(tokenStoreService).should().blacklistJti(eq("test-jti"), longThat(ttl -> ttl > 0));
		then(tokenStoreService).should().deleteRefreshToken(1L);
	}

	@Test
	@DisplayName("로그아웃: 이미 만료된 토큰이면 블랙리스트 등록 생략, Refresh Token은 삭제")
	void logout_alreadyExpiredToken() {
		// given
		Date pastExpiration = new Date(System.currentTimeMillis() - 1000L);
		given(jwtTokenProvider.getJti("expired-token")).willReturn("expired-jti");
		given(jwtTokenProvider.getExpiration("expired-token")).willReturn(pastExpiration);

		// when
		authService.logout(1L, "expired-token");

		// then
		then(tokenStoreService).should(never()).blacklistJti(any(), anyLong());
		then(tokenStoreService).should().deleteRefreshToken(1L);
	}

	// ── withdraw ───────────────────────────────────────────────

	@Test
	@DisplayName("회원탈퇴 성공: 비밀번호 확인 → is_deleted=true + 토큰 무효화")
	void withdraw_success() {
		// given
		WithdrawRequest request = new WithdrawRequest();
		request.setPassword("password123!");

		Date futureExpiration = new Date(System.currentTimeMillis() + 300000L);
		given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
		given(passwordEncoder.matches("password123!", "encodedPw")).willReturn(true);
		given(jwtTokenProvider.getJti("access-token")).willReturn("test-jti");
		given(jwtTokenProvider.getExpiration("access-token")).willReturn(futureExpiration);

		// when
		authService.withdraw(1L, "access-token", request);

		// then
		assertThat(testUser.isDeleted()).isTrue();
		then(tokenStoreService).should().blacklistJti(eq("test-jti"), longThat(ttl -> ttl > 0));
		then(tokenStoreService).should().deleteRefreshToken(1L);
	}

	@Test
	@DisplayName("회원탈퇴 실패: 존재하지 않는 userId")
	void withdraw_userNotFound() {
		// given
		WithdrawRequest request = new WithdrawRequest();
		request.setPassword("password123!");

		given(userRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> authService.withdraw(999L, "access-token", request))
			.isInstanceOf(BusinessException.class)
			.extracting(e -> ((BusinessException) e).getErrorCode())
			.isEqualTo(BusinessErrorCode.AUTH_NOT_FOUND_USER);
	}

	@Test
	@DisplayName("회원탈퇴 실패: 이미 탈퇴한 계정")
	void withdraw_alreadyWithdrawn() {
		// given
		WithdrawRequest request = new WithdrawRequest();
		request.setPassword("password123!");

		testUser.markDeleted();
		given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

		// when & then
		assertThatThrownBy(() -> authService.withdraw(1L, "access-token", request))
			.isInstanceOf(BusinessException.class)
			.extracting(e -> ((BusinessException) e).getErrorCode())
			.isEqualTo(BusinessErrorCode.AUTH_ALREADY_WITHDRAWN);
	}

	@Test
	@DisplayName("회원탈퇴 실패: 비밀번호 불일치")
	void withdraw_passwordMismatch() {
		// given
		WithdrawRequest request = new WithdrawRequest();
		request.setPassword("wrongPassword!");

		given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
		given(passwordEncoder.matches("wrongPassword!", "encodedPw")).willReturn(false);

		// when & then
		assertThatThrownBy(() -> authService.withdraw(1L, "access-token", request))
			.isInstanceOf(BusinessException.class)
			.extracting(e -> ((BusinessException) e).getErrorCode())
			.isEqualTo(BusinessErrorCode.AUTH_INVALID_PASSWORD);
	}
}
