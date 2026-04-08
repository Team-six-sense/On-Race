package com.kt.onrace.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.kt.onrace.common.logging.helper.TraceIdPropagation;

@Configuration
@ConditionalOnClass(name = "org.springframework.web.client.RestClient")
public class TraceIdClientConfig {

	@Bean
	RestClientCustomizer traceIdRestClientCustomizer() {
		return builder -> builder.requestInterceptor(
			TraceIdPropagation.restClientInterceptor());
	}
}
