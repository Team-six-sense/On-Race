package com.kt.onrace.domain.event.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "event")
public class EventProperties {
	private final long queueStartMinutes;
	private final long queueEndMinutes;
}
