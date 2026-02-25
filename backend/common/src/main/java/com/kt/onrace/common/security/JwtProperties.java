package com.kt.onrace.common.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Component
@ConditionalOnClass(name = "org.springframework.security.core.Authentication")
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
	private String secret;
	private Long accessTokenExpiration;
	private Long refreshTokenExpiration;
}
