package com.kt.gateway.common.filter;

import java.nio.charset.StandardCharsets;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;

import com.kt.gateway.common.security.JwtTokenProvider;

import lombok.Data;
import reactor.core.publisher.Mono;

@Component
public class BotDetectionFilter extends AbstractGatewayFilterFactory<BotDetectionFilter.Config> {

	private final JwtTokenProvider jwtTokenProvider;

	public BotDetectionFilter(JwtTokenProvider jwtTokenProvider) {
		super(Config.class);
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {
			String botToken = exchange.getRequest().getHeaders().getFirst("Bot-Clear-Token");

			if (botToken == null || !jwtTokenProvider.validateToken(botToken)) {
				ServerHttpResponse response = exchange.getResponse();
				response.setStatusCode(HttpStatus.FORBIDDEN);
				response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

				String jsonBody = String.format("{\"error\":\"CHALLENGE_REQUIRED\", \"challengeUrl\":\"%s\"}",
					config.getChallengeUri());
				byte[] bytes = jsonBody.getBytes(StandardCharsets.UTF_8);
				DataBuffer buffer = response.bufferFactory().wrap(bytes);

				return response.writeWith(Mono.just(buffer));
			}

			return chain.filter(exchange);
		};
	}

	@Data
	public static class Config {
		private boolean challengeMode = false;
		private String challengeUri;
	}
}
