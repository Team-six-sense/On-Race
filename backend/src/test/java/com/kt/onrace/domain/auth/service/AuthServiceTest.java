package com.kt.onrace.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.common.exception.ErrorCode;
import com.kt.onrace.domain.auth.entity.User;
import com.kt.onrace.domain.auth.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private AuthService authService;

	@Nested
	@DisplayName("signup")
	class Signup {

		@Test
		@DisplayName("signup_success: 이메일 중복 없으면 저장 후 id, email, createdAt 반환")
		void signup_success() {
			String email = "user@example.com";
			String rawPassword = "password1!";
			String name = "홍길동";
			String mobile = "01012345678";
			String encodedPassword = "$2a$10$encoded";
			User savedUser = User.createForSignup(email, name, encodedPassword, mobile);
			// BaseEntity id, createdAt은 JPA가 채움 - 리플렉션으로 설정하거나 별도 생성
			when(userRepository.existsByEmail(email)).thenReturn(false);
			when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
			LocalDateTime fixedTime = LocalDateTime.of(2025, 2, 13, 12, 0);
			when(userRepository.save(any(User.class))).thenAnswer(inv -> {
				User u = inv.getArgument(0);
				try {
					var base = User.class.getSuperclass();
					var idField = base.getDeclaredField("id");
					idField.setAccessible(true);
					idField.set(u, 1L);
					var createdAtField = base.getDeclaredField("createdAt");
					createdAtField.setAccessible(true);
					createdAtField.set(u, fixedTime);
				} catch (Exception ignored) {
				}
				return u;
			});

			AuthService.SignupResult result = authService.signup(email, rawPassword, name, mobile);

			assertThat(result.id()).isEqualTo(1L);
			assertThat(result.email()).isEqualTo(email);
			assertThat(result.createdAt()).isNotNull();
			verify(userRepository).existsByEmail(email);
			verify(passwordEncoder).encode(rawPassword);
			verify(userRepository).save(any(User.class));
		}

		@Test
		@DisplayName("signup_duplicate_email_fail: 이메일 중복이면 BusinessException(AUTH_DUPLICATE_EMAIL)")
		void signup_duplicate_email_fail() {
			String email = "dup@example.com";
			when(userRepository.existsByEmail(email)).thenReturn(true);

			assertThatThrownBy(() -> authService.signup(email, "pass", "name", null))
				.isInstanceOf(BusinessException.class)
				.satisfies(e -> assertThat(((BusinessException)e).getErrorCode()).isEqualTo(ErrorCode.AUTH_DUPLICATE_EMAIL));

			verify(userRepository).existsByEmail(email);
			verify(passwordEncoder, org.mockito.Mockito.never()).encode(anyString());
			verify(userRepository, org.mockito.Mockito.never()).save(any(User.class));
		}
	}
}
