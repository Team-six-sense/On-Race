package com.kt.onrace.auth.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.service.DefaultMessageService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SolapiConfig {

	private final SolapiProperties properties;

	@Bean
	public DefaultMessageService messageService() {
		return SolapiClient.INSTANCE.createInstance(properties.getApiKey(), properties.getApiSecret());
	}
	
}
