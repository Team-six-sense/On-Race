package com.kt.onrace.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "gateway")
public class GatewayProperties {
	private final Internal internal;
	private final QueueCache queueCache;
	private final String queueTokenSecret;

	@Getter
	@RequiredArgsConstructor
	public static class Internal {
		private final String secret;
	}

	@Getter
	@RequiredArgsConstructor
	public static class QueueCache {
		private final String serviceUri;
		private final long pollIntervalMs;
		private final String enabledEventsPath;
	}
}
