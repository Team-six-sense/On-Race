package com.kt.onrace.domain.entry.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "entry.reservation")
public class EntryProperties {
	private final long ttlSeconds;
}
