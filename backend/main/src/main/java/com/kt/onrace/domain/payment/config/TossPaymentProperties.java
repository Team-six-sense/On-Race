package com.kt.onrace.domain.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "toss.payment")
public class TossPaymentProperties {
	private String secretKey;
	private String url;
}
