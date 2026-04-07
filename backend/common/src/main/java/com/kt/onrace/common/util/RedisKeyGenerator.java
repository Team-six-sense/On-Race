package com.kt.onrace.common.util;

public class RedisKeyGenerator {

	// AUTH
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

	public static String emailCheckRateLimitKey(String ip) {
		return String.format("email_check:rate_limit:%s", ip);
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

	public static String smsFindIpAttemptKey(String ip) {
		return String.format("sms:find:ip_attempt:%s", ip);
	}

	// STOCK
	public static String totalStockKey(Long paceId) {
		return String.format("stock:total:pace:%d", paceId);
	}

	public static String tempStockKey(Long paceId) {
		return String.format("stock:temp:pace:%d", paceId);
	}

	public static String confirmStockKey(Long paceId) {
		return String.format("stock:confirm:pace:%d", paceId);
	}

	public static String reservationKey(Long paceId, Long userId) {
		return String.format("stock:reservation:%d:%d", paceId, userId);
	}

	// QUEUE
	public static String queueWaiting(Long paceId) {
		return String.format("queue:waiting:%d", paceId);
	}

	public static String queuePass(Long paceId, Long userId) {
		return String.format("queue:pass:%d:%d", paceId, userId);
	}

	public static String queueRetry(Long paceId) {
		return String.format("queue:retry:%d", paceId);
	}

	public static String queueBatchLock(Long paceId) {
		return String.format("queue:batch:lock:%d", paceId);
	}

	public static String queueActivePaces() {
		return "queue:active-paces";
	}

	public static String queueEnableChange() {
		return "queue:event:enabled:changed";
	}
}
