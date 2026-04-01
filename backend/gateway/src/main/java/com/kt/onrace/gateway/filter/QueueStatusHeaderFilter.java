package com.kt.onrace.gateway.filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.kt.onrace.gateway.cache.QueueEventCache;

import lombok.Data;

// 신청 버튼 클릭 -> 재고 조회(일시품절, 매진, 가능) -> 가능 시 gateway에서 응답헤더에 이벤트 대기열 여부 포함
@Component
public class QueueStatusHeaderFilter extends AbstractGatewayFilterFactory<QueueStatusHeaderFilter.Config> {

	private final QueueEventCache queueEventCache;

	private static final String QUEUE_ENABLED_HEADER = "X-Queue-Enabled";
	private static final Pattern EVENT_ID_PATTERN = Pattern.compile("/events/(\\d+)/");

	public QueueStatusHeaderFilter(QueueEventCache queueEventCache) {
		super(Config.class);
		this.queueEventCache = queueEventCache;
	}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {
			Long eventId = extractEventId(exchange);

			if (eventId != null) {
				boolean enabled = queueEventCache.isQueueEnabled(eventId);
				exchange.getResponse().getHeaders().add(QUEUE_ENABLED_HEADER, String.valueOf(enabled));
			}

			return chain.filter(exchange);
		};
	}

	private Long extractEventId(ServerWebExchange exchange) {
		String path = exchange.getRequest().getURI().getPath();
		Matcher matcher = EVENT_ID_PATTERN.matcher(path);
		if (matcher.find()) {
			try {
				return Long.parseLong(matcher.group(1));
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return null;
	}

	@Data
	public static class Config {
	}
}
