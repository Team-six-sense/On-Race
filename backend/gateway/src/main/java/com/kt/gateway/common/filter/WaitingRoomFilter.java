package com.kt.gateway.common.filter;

import java.nio.charset.StandardCharsets;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;

import com.kt.gateway.common.security.JwtTokenProvider;

import lombok.Data;
import reactor.core.publisher.Mono;

@Component
public class WaitingRoomFilter extends AbstractGatewayFilterFactory<WaitingRoomFilter.Config> {

	private final JwtTokenProvider jwtTokenProvider;
	private final ReactiveStringRedisTemplate redisTemplate;

	private static final String QUEUE_ENABLED_KEY = "GLOBAL_WAITING_ROOM_ENABLED";

	public WaitingRoomFilter(JwtTokenProvider jwtTokenProvider, ReactiveStringRedisTemplate redisTemplate) {
		super(Config.class);
		this.jwtTokenProvider = jwtTokenProvider;
		this.redisTemplate = redisTemplate;
	}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> redisTemplate.opsForValue().get(QUEUE_ENABLED_KEY)
			.defaultIfEmpty("FALSE")
			.flatMap(isEnabled -> {
				if (!"TRUE".equalsIgnoreCase(isEnabled)) {
					return chain.filter(exchange);
				}

				String passToken = exchange.getRequest().getHeaders().getFirst("WaitingRoom-Pass-Token");

				if (passToken == null || !jwtTokenProvider.validateToken(passToken)) {
					ServerHttpResponse response = exchange.getResponse();
					response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
					response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

					String jsonBody = String.format("{\"error\":\"QUEUE_REQUIRED\", \"queueUrl\":\"%s\"}",
						config.getQueueUrl());
					byte[] bytes = jsonBody.getBytes(StandardCharsets.UTF_8);
					DataBuffer buffer = response.bufferFactory().wrap(bytes);

					return response.writeWith(Mono.just(buffer));
				}

				return chain.filter(exchange);
			});
	}

	@Data
	public static class Config {
		private String queueUrl;
	}
}
