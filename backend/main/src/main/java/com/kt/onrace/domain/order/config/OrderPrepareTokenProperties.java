package com.kt.onrace.domain.order.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * checkout prepare token TTL이다.
 * queue pass token TTL, reservation TTL과는 별개로 관리한다.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "order.prepare-token")
public class OrderPrepareTokenProperties {

	private long ttlSeconds = 300L;

	public Duration ttl() {
		return Duration.ofSeconds(ttlSeconds);
	}
}
