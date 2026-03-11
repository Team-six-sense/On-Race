package com.kt.onrace.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "oauth")
public class OAuthProperties {

	/**
	 * OAuth 로그인 성공/실패 후 프론트엔드로 리다이렉트할 URI
	 * 예: http://localhost:3000/oauth/callback
	 */
	private String redirectUri;
}
