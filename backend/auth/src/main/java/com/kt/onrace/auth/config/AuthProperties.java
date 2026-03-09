package com.kt.onrace.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

	private LoginFail loginFail = new LoginFail();

	@Getter
	@Setter
	public static class LoginFail {

		/**
		 * 이 횟수 이상 실패 시 경고 메시지 반환 (예: "N회 남았습니다")
		 */
		private int warningThreshold;

		/**
		 * 이 횟수 이상 실패 시 CAPTCHA 요구
		 */
		private int captchaThreshold;

		/**
		 * 로그인 실패 횟수 카운터 Redis TTL (분)
		 */
		private long ttlMinutes;
	}
}
