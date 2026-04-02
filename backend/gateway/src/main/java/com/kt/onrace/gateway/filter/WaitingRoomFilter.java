package com.kt.onrace.gateway.filter;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.SecretKey;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.kt.onrace.gateway.cache.QueueEventCache;
import com.kt.onrace.gateway.config.GatewayProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import reactor.core.publisher.Mono;

@Component
public class WaitingRoomFilter extends AbstractGatewayFilterFactory<WaitingRoomFilter.Config> {

	private final SecretKey queueTokenKey;
	private final QueueEventCache queueEventCache;

	private static final String PASS_TOKEN_HEADER = "X-Queue-Token";
	private static final String QUEUE_PACE_ID_HEADER = "X-Queue-Pace-Id";
	private static final String QUEUE_PASS_TYPE = "QUEUE_PASS";
	private static final String CLAIM_PACE_ID = "CLAIM_PACE_ID";
	private static final Pattern EVENT_ID_PATTERN = Pattern.compile("/events/(\\d+)/");

	public WaitingRoomFilter(GatewayProperties gatewayProperties, QueueEventCache queueEventCache) {
		super(Config.class);
		this.queueTokenKey = Keys.hmacShaKeyFor(gatewayProperties.getQueueTokenSecret().getBytes(StandardCharsets.UTF_8));
		this.queueEventCache = queueEventCache;
	}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {
			Long eventId = extractEventId(exchange);

			if (eventId == null) {
				return chain.filter(exchange);
			}

			// 로컬 캐시에서 대기열 활성 여부 확인
			if (!queueEventCache.isQueueEnabled(eventId)) {
				return chain.filter(exchange);
			}

			String passToken = exchange.getRequest().getHeaders().getFirst(PASS_TOKEN_HEADER);

			Optional<Long> paceId = extractPaceIdFromToken(passToken);
			if (paceId.isEmpty()) {
				return sendQueueRequired(exchange, config);
			}

			ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
				.header(QUEUE_PACE_ID_HEADER, String.valueOf(paceId.get()))
				.build();
			return chain.filter(exchange.mutate().request(mutatedRequest).build());
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

	private Mono<Void> sendQueueRequired(ServerWebExchange exchange, Config config) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

		String jsonBody = String.format("{\"error\":\"QUEUE_REQUIRED\", \"queueUrl\":\"%s\"}", config.getQueueUrl());
		byte[] bytes = jsonBody.getBytes(StandardCharsets.UTF_8);
		DataBuffer buffer = response.bufferFactory().wrap(bytes);

		return response.writeWith(Mono.just(buffer));
	}

	private Optional<Long> extractPaceIdFromToken(String token) {
		if (token == null) {
			return Optional.empty();
		}
		try {
			Claims claims = Jwts.parser()
				.verifyWith(queueTokenKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();

			if (!QUEUE_PASS_TYPE.equals(claims.get("type", String.class))) {
				return Optional.empty();
			}

			Long paceId = claims.get(CLAIM_PACE_ID, Long.class);
			return Optional.ofNullable(paceId);
		} catch (JwtException | IllegalArgumentException e) {
			return Optional.empty();
		}
	}

	@Data
	public static class Config {
		private String queueUrl;
	}
}
