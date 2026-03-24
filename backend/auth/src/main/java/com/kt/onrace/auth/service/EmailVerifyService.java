package com.kt.onrace.auth.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;

import org.redisson.api.RBucket;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.kt.onrace.auth.entity.EmailSend;
import com.kt.onrace.auth.entity.EmailSendType;
import com.kt.onrace.auth.repository.EmailSendRepository;
import com.kt.onrace.auth.repository.UserRepository;
import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.common.util.RedisKeyGenerator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailVerifyService {

	private static final long CODE_TTL_MINUTES = 15;
	private static final long VERIFIED_TTL_MINUTES = 10;
	private static final long COOLDOWN_SECONDS = 60;
	private static final long MAX_SEND_COUNT = 5;
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	private final RedissonClient redissonClient;
	private final JavaMailSender mailSender;
	private final UserRepository userRepository;
	private final EmailSendRepository emailSendRepository;

	public void sendCode(String email) {
		if (userRepository.existsByEmail(email)) {
			throw new BusinessException(BusinessErrorCode.AUTH_DUPLICATE_EMAIL);
		}

		RBucket<String> cooldownBucket = redissonClient.getBucket(
			RedisKeyGenerator.emailSendCooldownKey(email), StringCodec.INSTANCE);
		if (cooldownBucket.isExists()) {
			throw new BusinessException(BusinessErrorCode.AUTH_EMAIL_SEND_COOLDOWN);
		}

		String today = LocalDate.now().toString();
		RAtomicLong counter = redissonClient.getAtomicLong(RedisKeyGenerator.emailSendCountKey(email, today));
		if (counter.get() >= MAX_SEND_COUNT) {
			throw new BusinessException(BusinessErrorCode.AUTH_EMAIL_SEND_LIMIT_EXCEEDED);
		}

		String code = generateCode();

		RBucket<String> bucket = redissonClient.getBucket(RedisKeyGenerator.emailVerifyCodeKey(email),
			StringCodec.INSTANCE);
		bucket.set(code, Duration.ofMinutes(CODE_TTL_MINUTES));

		cooldownBucket.set("1", Duration.ofSeconds(COOLDOWN_SECONDS));

		long count = counter.incrementAndGet();
		if (count == 1) {
			counter.expire(Duration.ofDays(1));
		}

		try {
			sendEmail(email, code);
			emailSendRepository.save(EmailSend.success(email, EmailSendType.EMAIL_VERIFY));
		} catch (org.springframework.mail.MailException e) {
			emailSendRepository.save(EmailSend.fail(email, EmailSendType.EMAIL_VERIFY, e.getMessage()));
			throw e;
		}
	}

	public void verifyCode(String email, String code) {
		RBucket<String> bucket = redissonClient.getBucket(RedisKeyGenerator.emailVerifyCodeKey(email),
			StringCodec.INSTANCE);
		String stored = bucket.get();

		if (stored == null || !stored.equals(code)) {
			throw new BusinessException(BusinessErrorCode.AUTH_INVALID_EMAIL_CODE);
		}

		bucket.delete();

		RBucket<String> verifiedBucket = redissonClient.getBucket(RedisKeyGenerator.emailVerifiedKey(email));
		verifiedBucket.set("1", Duration.ofMinutes(VERIFIED_TTL_MINUTES));
	}

	public boolean isVerified(String email) {
		return redissonClient.getBucket(RedisKeyGenerator.emailVerifiedKey(email)).isExists();
	}

	public void deleteVerified(String email) {
		redissonClient.getBucket(RedisKeyGenerator.emailVerifiedKey(email)).delete();
	}

	/**
	 * 인증 상태를 원자적으로 확인하고 삭제합니다.
	 * getAndDelete()를 사용해 확인과 삭제를 단일 연산으로 처리하여 race condition을 방지합니다.
	 *
	 * @return 인증 상태가 존재했으면 true, 아니면 false
	 */
	public boolean consumeVerification(String email) {
		RBucket<String> verifiedBucket = redissonClient.getBucket(RedisKeyGenerator.emailVerifiedKey(email));
		return verifiedBucket.getAndDelete() != null;
	}

	private String generateCode() {
		return String.format("%04d", SECURE_RANDOM.nextInt(10_000));
	}

	private void sendEmail(String email, String code) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(email);
		message.setSubject("[On-Race] 이메일 인증 코드");
		message.setText("인증 코드: " + code + "\n\n15분 이내에 입력해 주세요.");
		mailSender.send(message);
	}
}
