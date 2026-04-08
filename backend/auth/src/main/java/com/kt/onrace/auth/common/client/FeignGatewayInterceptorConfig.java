package com.kt.onrace.auth.common.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import com.kt.onrace.common.logging.helper.TraceIdPropagation;

import feign.RequestInterceptor;

public class FeignGatewayInterceptorConfig {

	@Value("${gateway.internal.secret}")
	private String gatewaySecret;

	@Bean
	public RequestInterceptor requestInterceptor() {
		return requestTemplate -> {
			requestTemplate.header("X-Gateway-Token", gatewaySecret);
			TraceIdPropagation.setTraceHeader(
				(name, value) -> requestTemplate.header(name, value));
		};
	}

}
