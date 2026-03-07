package com.kt.onrace.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "terms")
public class TermsProperties {

	/**
	 * 사용자가 동의해야 하는 최소 약관 버전.
	 * 이 버전보다 낮은 버전에 동의한 사용자는 재동의가 필요하다.
	 */
	private String version;
}
