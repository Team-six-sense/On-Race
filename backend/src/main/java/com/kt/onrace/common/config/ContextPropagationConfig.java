package com.kt.onrace.common.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.context.ContextRegistry;
import io.micrometer.context.ThreadLocalAccessor;

@Configuration
public class ContextPropagationConfig {

	@Bean
	public ContextRegistry contextRegistry() {
		ContextRegistry registry = ContextRegistry.getInstance();

		registry.registerThreadLocalAccessor(new ThreadLocalAccessor<String>() {
			private static final String KEY = "traceId";

			@Override
			public Object key() {
				return KEY;
			}

			@Override
			public String getValue() {
				return MDC.get(KEY);
			}

			@Override
			public void setValue(String value) {
				MDC.put(KEY, value);
			}

			@Override
			public void reset() {
				MDC.remove(KEY);
			}
		});

		return registry;
	}
}
