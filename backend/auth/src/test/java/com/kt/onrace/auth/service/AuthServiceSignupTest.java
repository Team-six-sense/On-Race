package com.kt.onrace.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RedissonClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.kt.onrace.auth.common.client.MainServiceClient;
import com.kt.onrace.auth.dto.SignupRequest;
import com.kt.onrace.auth.dto.SignupResponse;
import com.kt.onrace.auth.dto.TermAgreement;
import com.kt.onrace.auth.entity.TermMaster;
import com.kt.onrace.auth.entity.TermVersion;
import com.kt.onrace.auth.entity.User;
import com.kt.onrace.auth.repository.TermUserRepository;
import com.kt.onrace.auth.repository.TermVersionRepository;
import com.kt.onrace.auth.repository.UserRepository;
import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.common.security.JwtProperties;
import com.kt.onrace.common.security.JwtTokenProvider;
import com.kt.onrace.common.util.RedisKeyGenerator;

@ExtendWith(MockitoExtension.class)
class AuthServiceSignupTest {

	@InjectMocks
	private AuthService authService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private TermVersionRepository termVersionRepository;

	@Mock
	private TermUserRepository termUserRepository;

	@Mock
	private MainServiceClient mainServiceClient;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@Mock
	private JwtProperties jwtProperties;

	@Mock
	private TokenStoreService tokenStoreService;

	@Mock
	private EmailVerifyService emailVerifyService;

	@Mock
	private SmsVerifyService smsVerifyService;

	@Mock
	private LoginHistoryService loginHistoryService;

	@Mock
	private RedissonClient redissonClient;

	@Mock
	private RedisKeyGenerator redisKeyGenerator;

	@Mock
	private CaptchaVerifyService captchaVerifyService;

	private static final String EMAIL = "test@test.com";
	private static final String PHONE = "01012345678";
	private static final String PASSWORD = "Password123!";
	private static final String NAME = "테스터";

	private TermVersion requiredTermVersion;
	private TermVersion optionalTermVersion;

	@BeforeEach
	void setUp() {
		TermMaster requiredMaster = TermMaster.create("서비스 이용약관", true);
		ReflectionTestUtils.setField(requiredMaster, "id", 10L);
		requiredTermVersion = TermVersion.create(requiredMaster, "v1.0", "content", true);
		ReflectionTestUtils.setField(requiredTermVersion, "id", 1L);

		TermMaster optionalMaster = TermMaster.create("마케팅 수신 동의", false);
		ReflectionTestUtils.setField(optionalMaster, "id", 11L);
		optionalTermVersion = TermVersion.create(optionalMaster, "v1.0", "content", true);
		ReflectionTestUtils.setField(optionalTermVersion, "id", 2L);
	}

	private SignupRequest buildRequest(List<TermAgreement> agreements) {
		return new SignupRequest(EMAIL, NAME, PASSWORD, PHONE, agreements);
	}

	// ── isVerified 단계 실패 ─────────────────────────────────────────────────

	@Test
	@DisplayName("회원가입 실패: 이메일 인증 미완료 → consumeVerification 미호출")
	void signup_emailNotVerified() {
		given(emailVerifyService.isVerified(EMAIL)).willReturn(false);

		assertThatThrownBy(() -> authService.signup(buildRequest(List.of(new TermAgreement(1L, true)))))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(BusinessErrorCode.AUTH_EMAIL_NOT_VERIFIED);

		then(emailVerifyService).should(never()).consumeVerification(any());
		then(smsVerifyService).should(never()).consumeVerification(any());
	}

	@Test
	@DisplayName("회원가입 실패: SMS 인증 미완료 → consumeVerification 미호출")
	void signup_phoneNotVerified() {
		given(emailVerifyService.isVerified(EMAIL)).willReturn(true);
		given(smsVerifyService.isVerified(PHONE)).willReturn(false);

		assertThatThrownBy(() -> authService.signup(buildRequest(List.of(new TermAgreement(1L, true)))))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(BusinessErrorCode.AUTH_PHONE_NOT_VERIFIED);

		then(emailVerifyService).should(never()).consumeVerification(any());
		then(smsVerifyService).should(never()).consumeVerification(any());
	}

	// ── 중복 검사 단계 실패 ───────────────────────────────────────────────────

	@Test
	@DisplayName("회원가입 실패: 이메일 중복")
	void signup_duplicateEmail() {
		given(emailVerifyService.isVerified(EMAIL)).willReturn(true);
		given(smsVerifyService.isVerified(PHONE)).willReturn(true);
		given(userRepository.existsByEmail(EMAIL)).willReturn(true);

		assertThatThrownBy(() -> authService.signup(buildRequest(List.of(new TermAgreement(1L, true)))))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(BusinessErrorCode.AUTH_DUPLICATE_EMAIL);
	}

	@Test
	@DisplayName("회원가입 실패: 전화번호 중복")
	void signup_duplicatePhone() {
		given(emailVerifyService.isVerified(EMAIL)).willReturn(true);
		given(smsVerifyService.isVerified(PHONE)).willReturn(true);
		given(userRepository.existsByEmail(EMAIL)).willReturn(false);
		given(userRepository.existsByPhoneNumber(PHONE)).willReturn(true);

		assertThatThrownBy(() -> authService.signup(buildRequest(List.of(new TermAgreement(1L, true)))))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(BusinessErrorCode.AUTH_DUPLICATE_PHONE);
	}

	// ── 약관 동의 단계 실패 → 인증 상태 유지 ─────────────────────────────────

	@Test
	@DisplayName("회원가입 실패: 필수 약관 미동의 → isVerified 보존(consumeVerification 미호출)")
	void signup_requiredTermNotAgreed_preservesVerification() {
		given(emailVerifyService.isVerified(EMAIL)).willReturn(true);
		given(smsVerifyService.isVerified(PHONE)).willReturn(true);
		given(userRepository.existsByEmail(EMAIL)).willReturn(false);
		given(userRepository.existsByPhoneNumber(PHONE)).willReturn(false);
		given(termVersionRepository.findAllActiveWithMaster()).willReturn(List.of(requiredTermVersion));

		// 필수 약관(ID=1)에 동의하지 않은 요청
		SignupRequest request = buildRequest(List.of(new TermAgreement(1L, false)));

		assertThatThrownBy(() -> authService.signup(request))
				.isInstanceOf(BusinessException.class)
				.extracting(e -> ((BusinessException) e).getErrorCode())
				.isEqualTo(BusinessErrorCode.AUTH_REQUIRED_TERM_NOT_AGREED);

		// 인증 상태가 소비(삭제)되지 않아야 한다 — 이번 PR의 핵심 보장
		then(emailVerifyService).should(never()).consumeVerification(any());
		then(smsVerifyService).should(never()).consumeVerification(any());
	}

	// ── 성공 ─────────────────────────────────────────────────────────────────

	@Test
	@DisplayName("회원가입 성공: 검증 통과 후 consumeVerification 호출, 사용자 저장, 약관 동의 기록")
	void signup_success() {
		User savedUser = User.createUser(EMAIL, NAME, "encodedPw", PHONE);
		ReflectionTestUtils.setField(savedUser, "id", 1L);

		given(emailVerifyService.isVerified(EMAIL)).willReturn(true);
		given(smsVerifyService.isVerified(PHONE)).willReturn(true);
		given(userRepository.existsByEmail(EMAIL)).willReturn(false);
		given(userRepository.existsByPhoneNumber(PHONE)).willReturn(false);
		given(termVersionRepository.findAllActiveWithMaster()).willReturn(List.of(requiredTermVersion, optionalTermVersion));
		given(emailVerifyService.consumeVerification(EMAIL)).willReturn(true);
		given(smsVerifyService.consumeVerification(PHONE)).willReturn(true);
		given(passwordEncoder.encode(PASSWORD)).willReturn("encodedPw");
		given(userRepository.save(any(User.class))).willReturn(savedUser);

		SignupRequest request = buildRequest(List.of(
				new TermAgreement(1L, true),   // 필수 동의
				new TermAgreement(2L, false)   // 선택 미동의
		));

		SignupResponse response = authService.signup(request);

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.email()).isEqualTo(EMAIL);

		then(emailVerifyService).should().consumeVerification(EMAIL);
		then(smsVerifyService).should().consumeVerification(PHONE);
		then(userRepository).should().save(any(User.class));
		then(termUserRepository).should(times(2)).save(any());
		then(mainServiceClient).should().syncUserCreated(1L);
	}
}
