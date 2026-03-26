package com.kt.onrace.auth.common.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class FeignGatewayInterceptorConfig {

	@Value("${gateway.internal.secret}")
	private String gatewaySecret;

	@Bean
	public RequestInterceptor requestInterceptor() {
		return new RequestInterceptor() {
			@Override
			public void apply(RequestTemplate requestTemplate) {
				requestTemplate.header("X-Gateway-Token", gatewaySecret);
			}
		};
	}

}
