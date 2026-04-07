package com.kt.onrace.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
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

import com.kt.onrace.auth.config.AppProperties;
import com.kt.onrace.auth.entity.Gender;
import com.kt.onrace.auth.entity.User;
import com.kt.onrace.auth.entity.VerificationStatus;
import com.kt.onrace.auth.repository.UserRepository;
import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class AccountServicePassVerificationTest {

	@InjectMocks
	private AccountService accountService;

	@Mock
	private AppProperties appProperties;

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private PasswordResetService passwordResetService;

	private static final Long USER_ID = 1L;
	private static final String NAME = "홍길동";
	private static final Gender GENDER = Gender.MALE;
	private static final LocalDate BIRTHDATE = LocalDate.of(1995, 3, 15);

	private User activeUser;

	@BeforeEach
	void setUp() {
		activeUser = User.createUser("test@test.com", null, "encodedPw", "01012345678", null, null);
		ReflectionTestUtils.setField(activeUser, "id", USER_ID);
	}

	// ── completePassVerification ──────────────────────────────────────────────

	@Test
	@DisplayName("PASS 인증 완료 성공: name/gender/birthdate 저장, verificationStatus → VERIFIED")
	void completePassVerification_success() {
		given(userRepository.findById(USER_ID)).willReturn(Optional.of(activeUser));

		accountService.completePassVerification(USER_ID, NAME, GENDER, BIRTHDATE);

		assertThat(activeUser.getName()).isEqualTo(NAME);
		assertThat(activeUser.getGender()).isEqualTo(GENDER);
		assertThat(activeUser.getBirthdate()).isEqualTo(BIRTHDATE);
		assertThat(activeUser.getVerificationStatus()).isEqualTo(VerificationStatus.VERIFIED);
	}

	@Test
	@DisplayName("PASS 인증 완료 실패: 존재하지 않는 유저 → AUTH_NOT_FOUND_USER")
	void completePassVerification_userNotFound() {
		given(userRepository.findById(USER_ID)).willReturn(Optional.empty());

		assertThatThrownBy(() -> accountService.completePassVerification(USER_ID, NAME, GENDER, BIRTHDATE))
			.isInstanceOf(BusinessException.class)
			.extracting(e -> ((BusinessException) e).getErrorCode())
			.isEqualTo(BusinessErrorCode.AUTH_NOT_FOUND_USER);
	}

	// ── revokePassVerification ────────────────────────────────────────────────

	@Test
	@DisplayName("PASS 인증 해제 성공: verificationStatus → UNVERIFIED")
	void revokePassVerification_success() {
		// 먼저 VERIFIED 상태로 만들기
		activeUser.applyPassVerification(NAME, GENDER, BIRTHDATE);
		given(userRepository.findById(USER_ID)).willReturn(Optional.of(activeUser));

		accountService.revokePassVerification(USER_ID);

		assertThat(activeUser.getVerificationStatus()).isEqualTo(VerificationStatus.UNVERIFIED);
	}

	@Test
	@DisplayName("PASS 인증 해제 실패: 존재하지 않는 유저 → AUTH_NOT_FOUND_USER")
	void revokePassVerification_userNotFound() {
		given(userRepository.findById(USER_ID)).willReturn(Optional.empty());

		assertThatThrownBy(() -> accountService.revokePassVerification(USER_ID))
			.isInstanceOf(BusinessException.class)
			.extracting(e -> ((BusinessException) e).getErrorCode())
			.isEqualTo(BusinessErrorCode.AUTH_NOT_FOUND_USER);
	}
}
