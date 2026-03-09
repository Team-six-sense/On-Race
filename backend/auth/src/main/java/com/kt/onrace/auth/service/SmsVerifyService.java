package com.kt.onrace.auth.service;

import java.security.SecureRandom;
import java.time.Duration;

import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Service;

import com.kt.onrace.auth.common.config.SolapiProperties;
import com.kt.onrace.auth.repository.UserRepository;
import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.common.util.RedisKeyGenerator;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SmsVerifyService {

	private static final long CODE_TTL_MINUTES = 3;
	private static final long VERIFIED_TTL_MINUTES = 10;

	private final RedissonClient redissonClient;
	private final RedisKeyGenerator redisKeyGenerator;

	private final DefaultMessageService messageService;
	private final SolapiProperties properties;
	private final UserRepository userRepository;

	public void sendCodeForFind(String phoneNumber) {
		if (!userRepository.existsByPhoneNumber(phoneNumber)) {
			throw new BusinessException(BusinessErrorCode.AUTH_NOT_FOUND_USER);
		}
		sendSmsCode(phoneNumber);
	}

	public void sendCode(String phoneNumber) {
		if (userRepository.existsByPhoneNumber(phoneNumber)) {
			throw new BusinessException(BusinessErrorCode.AUTH_DUPLICATE_PHONE);
		}
		sendSmsCode(phoneNumber);
	}

	private void sendSmsCode(String phoneNumber) {
		RAtomicLong sendAttemptCounter = redissonClient.getAtomicLong(redisKeyGenerator.smsSendAttemptKey(phoneNumber));
		long currentSendAttempts = sendAttemptCounter.incrementAndGet();

		// 하루 단위로 전송 제한 (자정에 만료되도록 설정하는 것도 좋으나, 여기선 24시간 기준으로 설정)
		if (currentSendAttempts == 1) {
			sendAttemptCounter.expire(Duration.ofHours(24));
		}

		// 하루 최대 5회까지만 SMS 전송 허용
		if (currentSendAttempts > 5) {
			throw new BusinessException(BusinessErrorCode.AUTH_TOO_MANY_SEND_ATTEMPTS);
		}

		redissonClient.getAtomicLong(redisKeyGenerator.smsVerifyAttemptKey(phoneNumber)).delete();

		String code = generateCode();

		RBucket<String> bucket = redissonClient.getBucket(redisKeyGenerator.smsVerifyCodeKey(phoneNumber),
				StringCodec.INSTANCE);
		bucket.set(code, Duration.ofMinutes(CODE_TTL_MINUTES));

		String text = "[On-Race] 휴대폰 인증 코드: [" + code + "] 3분 이내에 입력해 주세요.";

		Message message = new Message();
		message.setFrom(properties.getSender());
		message.setTo(phoneNumber);
		message.setText(text);

		try {
			this.messageService.send(message, null);
		} catch (Exception e) {
			throw new BusinessException(BusinessErrorCode.COMMON_SYSTEM_ERROR);
		}
	}

	public void verifyCode(String phoneNumber, String code) {
		RAtomicLong attemptCounter = redissonClient.getAtomicLong(redisKeyGenerator.smsVerifyAttemptKey(phoneNumber));
		long currentAttempts = attemptCounter.incrementAndGet();

		if (currentAttempts == 1) {
			attemptCounter.expire(Duration.ofMinutes(CODE_TTL_MINUTES));
		}

		if (currentAttempts > 5) {
			throw new BusinessException(BusinessErrorCode.AUTH_TOO_MANY_VERIFY_ATTEMPTS);
		}

		RBucket<String> bucket = redissonClient.getBucket(redisKeyGenerator.smsVerifyCodeKey(phoneNumber),
				StringCodec.INSTANCE);
		String stored = bucket.get();

		if (stored == null || !stored.equals(code)) {
			throw new BusinessException(BusinessErrorCode.AUTH_INVALID_PHONE_CODE);
		}

		attemptCounter.delete();
		bucket.delete();

		RBucket<String> verifiedBucket = redissonClient.getBucket(redisKeyGenerator.smsVerifiedKey(phoneNumber));
		verifiedBucket.set("1", Duration.ofMinutes(VERIFIED_TTL_MINUTES));
	}

	public boolean isVerified(String phoneNumber) {
		return redissonClient.getBucket(redisKeyGenerator.smsVerifiedKey(phoneNumber)).isExists();
	}

	public void deleteVerified(String phoneNumber) {
		redissonClient.getBucket(redisKeyGenerator.smsVerifiedKey(phoneNumber)).delete();
	}

	private String generateCode() {
		return String.format("%06d", new SecureRandom().nextInt(1_000_000));
	}

}
