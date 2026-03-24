package com.kt.onrace.common.util;

public class RedisKeyGenerator {

	public static String lockKey(String resource, Long id) {
		return String.format("lock:%s:%d", resource, id);
	}

	public static String refreshTokenKey(Long userId) {
		return String.format("refresh_token:%d", userId);
	}

	public static String blacklistKey(String jti) {
		return String.format("blacklist:jti:%s", jti);
	}

	public static String emailVerifyCodeKey(String email) {
		return String.format("email:verify_code:%s", email);
	}

	public static String emailVerifiedKey(String email) {
		return String.format("email:verified:%s", email);
	}

	public static String passwordResetTokenKey(String token) {
		return String.format("password:reset_token:%s", token);
	}

	public static String passwordResetVerifiedKey(Long userId) {
		return String.format("password:reset_verified:%d", userId);
	}

	public static String passwordResetCooldownKey(String email) {
		return String.format("password:reset_cooldown:%s", email);
	}

	public static String passwordResetCountKey(String email, String date) {
		return String.format("password:reset_count:%s:%s", email, date);
	}

	public static String emailSendCooldownKey(String email) {
		return String.format("email:send_cooldown:%s", email);
	}

	public static String loginFailCountKey(String email) {
		return String.format("login:fail_count:%s", email);
	}

	public static String emailSendCountKey(String email, String date) {
		return String.format("email:send_count:%s:%s", email, date);
	}

	public static String smsVerifyCodeKey(String phoneNumber) {
		return String.format("sms:verify_code:%s", phoneNumber);
	}

	public static String smsVerifiedKey(String phoneNumber) {
		return String.format("sms:verified:%s", phoneNumber);
	}

	public static String smsVerifyAttemptKey(String phoneNumber) {
		return String.format("sms:verify_attempt:%s", phoneNumber);
	}

	public static String smsSendAttemptKey(String phoneNumber) {
		return String.format("sms:send_attempt:%s", phoneNumber);
	}

	public static String stockKey(Long paceId) {
		return String.format("stock:pace:%d", paceId);
	}

	public static String reservationKey(Long paceId, Long userId) {
		return String.format("reservation:%d:%d", paceId, userId);
	}

	public static String stockInitializedKey(Long paceId) {
		return String.format("stock:initialized:%d", paceId);
	}

	public static String queueWaiting(Long paceId) {
		return "queue:waiting:" + paceId;
	}

	public static String queuePass(Long paceId, Long userId) {
		return "queue:pass:" + paceId + ":" + userId;
	}

	public static String queueBatchLock() {
		return "queue:batch:lock";
	}

	public static String queueActivePaces() {
		return "queue:active-paces";
	}
}
