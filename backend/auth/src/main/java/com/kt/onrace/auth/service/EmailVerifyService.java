package com.kt.onrace.auth.service;

import java.security.SecureRandom;
import java.time.Duration;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.common.util.RedisKeyGenerator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailVerifyService {

	private static final long CODE_TTL_MINUTES = 5;
	private static final long VERIFIED_TTL_MINUTES = 10;

	private final RedissonClient redissonClient;
	private final RedisKeyGenerator redisKeyGenerator;
	private final JavaMailSender mailSender;

	public void sendCode(String email) {
		String code = generateCode();

		RBucket<String> bucket = redissonClient.getBucket(redisKeyGenerator.emailVerifyCodeKey(email));
		bucket.set(code, Duration.ofMinutes(CODE_TTL_MINUTES));

		sendEmail(email, code);
	}

	public void verifyCode(String email, String code) {
		RBucket<String> bucket = redissonClient.getBucket(redisKeyGenerator.emailVerifyCodeKey(email));
		String stored = bucket.get();

		if (stored == null || !stored.equals(code)) {
			throw new BusinessException(BusinessErrorCode.AUTH_INVALID_EMAIL_CODE);
		}

		bucket.delete();

		RBucket<String> verifiedBucket = redissonClient.getBucket(redisKeyGenerator.emailVerifiedKey(email));
		verifiedBucket.set("1", Duration.ofMinutes(VERIFIED_TTL_MINUTES));
	}

	public boolean isVerified(String email) {
		return redissonClient.getBucket(redisKeyGenerator.emailVerifiedKey(email)).isExists();
	}

	public void deleteVerified(String email) {
		redissonClient.getBucket(redisKeyGenerator.emailVerifiedKey(email)).delete();
	}

	private String generateCode() {
		return String.format("%06d", new SecureRandom().nextInt(1_000_000));
	}

	private void sendEmail(String email, String code) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(email);
		message.setSubject("[On-Race] 이메일 인증 코드");
		message.setText("인증 코드: " + code + "\n\n5분 이내에 입력해 주세요.");
		mailSender.send(message);
	}
}
