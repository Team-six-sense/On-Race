package com.kt.onrace.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.kt.onrace.auth.config.AppProperties;
import com.kt.onrace.auth.dto.AccountMeResponse;
import com.kt.onrace.auth.entity.AuthProvider;
import com.kt.onrace.auth.entity.User;
import com.kt.onrace.auth.entity.VerificationStatus;
import com.kt.onrace.auth.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

	@Mock
	private AppProperties appProperties;

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private PasswordResetService passwordResetService;

	@InjectMocks
	private AccountService accountService;

	@Test
	@DisplayName("LOCAL 계정의 내 정보 조회는 phone 과 비밀번호 변경 가능 여부를 함께 반환한다")
	void getMyInfoReturnsPhoneAndCanChangePasswordForLocalAccount() throws Exception {
		User user = User.createUser("local@test.com", "로컬유저", "encoded-password", "01012345678");
		setId(user, 7L);
		when(userRepository.findById(7L)).thenReturn(Optional.of(user));

		AccountMeResponse response = accountService.getMyInfo(7L);

		assertThat(response.id()).isEqualTo(7L);
		assertThat(response.email()).isEqualTo("local@test.com");
		assertThat(response.name()).isEqualTo("로컬유저");
		assertThat(response.phone()).isEqualTo("01012345678");
		assertThat(response.canChangePassword()).isTrue();
		assertThat(response.verificationStatus()).isEqualTo(VerificationStatus.NOT_STARTED);
		assertThat(response.marketingConsent()).isFalse();
		assertThat(response.authProvider()).isEqualTo(AuthProvider.LOCAL);
	}

	@Test
	@DisplayName("OAuth 계정의 내 정보 조회는 비밀번호 변경 불가를 반환한다")
	void getMyInfoReturnsCannotChangePasswordForOAuthAccount() throws Exception {
		User user = User.createOAuthUser("oauth@test.com", "소셜유저", AuthProvider.KAKAO, "provider-1");
		setId(user, 9L);
		when(userRepository.findById(9L)).thenReturn(Optional.of(user));

		AccountMeResponse response = accountService.getMyInfo(9L);

		assertThat(response.id()).isEqualTo(9L);
		assertThat(response.email()).isEqualTo("oauth@test.com");
		assertThat(response.name()).isEqualTo("소셜유저");
		assertThat(response.phone()).isNull();
		assertThat(response.canChangePassword()).isFalse();
		assertThat(response.verificationStatus()).isEqualTo(VerificationStatus.NOT_STARTED);
		assertThat(response.marketingConsent()).isFalse();
		assertThat(response.authProvider()).isEqualTo(AuthProvider.KAKAO);
	}

	@Test
	@DisplayName("마케팅 수신 동의와 본인인증 상태 변경은 사용자 엔티티에 반영된다")
	void updateMarketingConsentAndVerificationStatus() throws Exception {
		User user = User.createUser("user@test.com", "유저", "encoded-password", "01012345678");
		setId(user, 11L);
		when(userRepository.findById(11L)).thenReturn(Optional.of(user));

		accountService.updateMarketingConsent(11L, true);
		accountService.updateVerificationStatus(11L, VerificationStatus.COMPLETED);

		AccountMeResponse response = accountService.getMyInfo(11L);
		assertThat(response.marketingConsent()).isTrue();
		assertThat(response.verificationStatus()).isEqualTo(VerificationStatus.COMPLETED);
	}

	private void setId(User user, Long id) throws Exception {
		Field idField = user.getClass().getSuperclass().getDeclaredField("id");
		idField.setAccessible(true);
		idField.set(user, id);
	}
}
