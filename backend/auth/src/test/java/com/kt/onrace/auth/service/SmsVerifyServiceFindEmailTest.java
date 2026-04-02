package com.kt.onrace.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

import com.kt.onrace.auth.common.config.SolapiProperties;
import com.kt.onrace.auth.repository.UserRepository;
import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.common.util.RedisKeyGenerator;
import com.solapi.sdk.message.service.DefaultMessageService;

@ExtendWith(MockitoExtension.class)
class SmsVerifyServiceFindEmailTest {

	@InjectMocks
	private SmsVerifyService smsVerifyService;

	@Mock
	private RedissonClient redissonClient;

	@Mock
	private DefaultMessageService messageService;

	@Mock
	private SolapiProperties properties;

	@Mock
	private UserRepository userRepository;

	@Mock
	private RAtomicLong ipCounter;

	// ── IP Rate Limit ───────────────────────────────────────────

	@Test
	@DisplayName("IP 첫 요청: Redis 카운터에 TTL 10분 설정")
	void sendCodeForFind_firstRequest_setsTtl() {
		given(redissonClient.getAtomicLong(RedisKeyGenerator.smsFindIpAttemptKey("1.2.3.4"))).willReturn(ipCounter);
		given(ipCounter.incrementAndGet()).willReturn(1L);
		given(userRepository.existsByPhoneNumber(anyString())).willReturn(false);

		smsVerifyService.sendCodeForFind("01012345678", "1.2.3.4");

		then(ipCounter).should().expire(Duration.ofMinutes(10));
	}

	@Test
	@DisplayName("IP 요청 2회~5회: TTL 재설정 없이 정상 통과")
	void sendCodeForFind_withinLimit_doesNotResetTtl() {
		given(redissonClient.getAtomicLong(RedisKeyGenerator.smsFindIpAttemptKey("1.2.3.4"))).willReturn(ipCounter);
		given(ipCounter.incrementAndGet()).willReturn(3L);
		given(userRepository.existsByPhoneNumber(anyString())).willReturn(false);

		assertThatCode(() -> smsVerifyService.sendCodeForFind("01012345678", "1.2.3.4"))
			.doesNotThrowAnyException();

		then(ipCounter).should(never()).expire(any());
	}

	@Test
	@DisplayName("IP 요청 5회: 한도 내 마지막 요청 정상 통과")
	void sendCodeForFind_exactlyAtLimit_passes() {
		given(redissonClient.getAtomicLong(RedisKeyGenerator.smsFindIpAttemptKey("1.2.3.4"))).willReturn(ipCounter);
		given(ipCounter.incrementAndGet()).willReturn(5L);
		given(userRepository.existsByPhoneNumber(anyString())).willReturn(false);

		assertThatCode(() -> smsVerifyService.sendCodeForFind("01012345678", "1.2.3.4"))
			.doesNotThrowAnyException();
	}

	@Test
	@DisplayName("IP 요청 6회 이상: AUTH_FIND_EMAIL_IP_RATE_LIMITED 예외 발생")
	void sendCodeForFind_exceedsLimit_throwsException() {
		given(redissonClient.getAtomicLong(RedisKeyGenerator.smsFindIpAttemptKey("1.2.3.4"))).willReturn(ipCounter);
		given(ipCounter.incrementAndGet()).willReturn(6L);

		assertThatThrownBy(() -> smsVerifyService.sendCodeForFind("01012345678", "1.2.3.4"))
			.isInstanceOf(BusinessException.class)
			.extracting(e -> ((BusinessException)e).getErrorCode())
			.isEqualTo(BusinessErrorCode.AUTH_FIND_EMAIL_IP_RATE_LIMITED);
	}

	// ── SMS 발송 여부 ────────────────────────────────────────────

	@Test
	@DisplayName("가입된 전화번호: SMS 발송")
	@SuppressWarnings("unchecked")
	void sendCodeForFind_registeredPhone_sendsSms() {
		RAtomicLong sendAttemptCounter = mock(RAtomicLong.class);
		RAtomicLong verifyAttemptCounter = mock(RAtomicLong.class);
		RBucket<String> codeBucket = mock(RBucket.class);

		given(redissonClient.getAtomicLong(RedisKeyGenerator.smsFindIpAttemptKey("1.2.3.4"))).willReturn(ipCounter);
		given(ipCounter.incrementAndGet()).willReturn(1L);

		given(redissonClient.getAtomicLong(RedisKeyGenerator.smsSendAttemptKey("01012345678"))).willReturn(
			sendAttemptCounter);
		given(sendAttemptCounter.incrementAndGet()).willReturn(1L);

		given(redissonClient.getAtomicLong(RedisKeyGenerator.smsVerifyAttemptKey("01012345678"))).willReturn(
			verifyAttemptCounter);
		given(redissonClient.getBucket(RedisKeyGenerator.smsVerifyCodeKey("01012345678"), StringCodec.INSTANCE))
			.willReturn(codeBucket);

		given(userRepository.existsByPhoneNumber("01012345678")).willReturn(true);
		given(properties.getSender()).willReturn("01000000000");

		smsVerifyService.sendCodeForFind("01012345678", "1.2.3.4");

		then(messageService).should().send(any(), isNull());
	}

	@Test
	@DisplayName("미가입 전화번호: SMS 미발송 (항상 성공 응답)")
	void sendCodeForFind_unregisteredPhone_doesNotSendSms() {
		given(redissonClient.getAtomicLong(RedisKeyGenerator.smsFindIpAttemptKey("1.2.3.4"))).willReturn(ipCounter);
		given(ipCounter.incrementAndGet()).willReturn(1L);
		given(userRepository.existsByPhoneNumber("01099999999")).willReturn(false);

		assertThatCode(() -> smsVerifyService.sendCodeForFind("01099999999", "1.2.3.4"))
			.doesNotThrowAnyException();

		then(messageService).shouldHaveNoInteractions();
	}
}
