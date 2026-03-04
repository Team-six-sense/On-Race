package com.kt.onrace.auth.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "solapi")
public class SolapiProperties {
	private final String apiKey;
	private final String apiSecret;
	private final String sender;
}
