package com.kt.onrace.gateway.filter;

import java.util.UUID;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // 가장 높은 우선순위로 설정하여 다른 필터보다 먼저 실행되도록 함
public class TraceIdWebFilter implements WebFilter {

	private static final String TRACE_ID_HEADER = "X-Trace-Id";

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

		String traceId = exchange.getRequest().getHeaders().getFirst(TRACE_ID_HEADER);

		// 게이트웨이는 webflux 기반이라 기존에 사용하던 MDC 방식이 적용되지 않으므로, Reactor Context에 저장하는 방식
		if (traceId == null || traceId.isEmpty()) {
			traceId = UUID.randomUUID().toString().substring(0, 8);
		}

		String finalTraceId = traceId;
		ServerWebExchange mutatedExchange = exchange.mutate()
			.request(r -> r.header(TRACE_ID_HEADER, finalTraceId))
			.build();

		mutatedExchange.getResponse().getHeaders().add(TRACE_ID_HEADER, finalTraceId);

		return chain.filter(mutatedExchange).contextWrite(ctx -> ctx.put("traceId", finalTraceId));
	}
}
