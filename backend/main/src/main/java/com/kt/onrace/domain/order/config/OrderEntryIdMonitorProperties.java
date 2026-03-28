package com.kt.onrace.domain.order.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "order.entry-id.monitor")
public class OrderEntryIdMonitorProperties {

	private boolean enabled = true;

	private long fixedDelayMs = 300_000L;

	private int sampleSize = 10;
}
