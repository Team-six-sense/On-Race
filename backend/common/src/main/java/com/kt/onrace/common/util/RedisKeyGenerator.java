package com.kt.onrace.common.util;

import org.springframework.stereotype.Component;

@Component
public class RedisKeyGenerator {

	public String lockKey(String resource, Long id) {
		return String.format("lock:%s:%d", resource, id);
	}

	public String refreshTokenKey(Long userId) {
		return String.format("refresh_token:%d", userId);
	}

	public String blacklistKey(String jti) {
		return String.format("blacklist:jti:%s", jti);
	}

	public String emailVerifyCodeKey(String email) {
		return String.format("email:verify_code:%s", email);
	}

	public String emailVerifiedKey(String email) {
		return String.format("email:verified:%s", email);
	}

	public String passwordResetTokenKey(String token) {
		return String.format("password:reset_token:%s", token);
	}

	public String passwordResetVerifiedKey(Long userId) {
		return String.format("password:reset_verified:%d", userId);
	}

	public String passwordResetCooldownKey(String email) {
		return String.format("password:reset_cooldown:%s", email);
	}

	public String passwordResetCountKey(String email, String date) {
		return String.format("password:reset_count:%s:%s", email, date);
	public String smsVerifyCodeKey(String phoneNumber) {
		return String.format("sms:verify_code:%s", phoneNumber);
	}

	public String smsVerifiedKey(String phoneNumber) {
		return String.format("sms:verified:%s", phoneNumber);
	}

	public String smsVerifyAttemptKey(String phoneNumber) {
		return String.format("sms:verify_attempt:%s", phoneNumber);
	}

	public String smsSendAttemptKey(String phoneNumber) {
		return String.format("sms:send_attempt:%s", phoneNumber);
	}
}
