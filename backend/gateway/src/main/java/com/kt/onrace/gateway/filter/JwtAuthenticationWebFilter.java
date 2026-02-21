package com.kt.onrace.gateway.filter;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.kt.onrace.common.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationWebFilter implements WebFilter {

	private final JwtTokenProvider jwtTokenProvider;

	private static final String HEADER_USER_ID = "X-User-Id";
	private static final String HEADER_USER_ROLE = "X-User-Role";

	@Override
	public @NonNull Mono<Void> filter(
		@NonNull ServerWebExchange exchange,
		@NonNull WebFilterChain chain) {

		ServerWebExchange sanitized = stripInternalHeaders(exchange);
		String token = resolveBearerToken(sanitized);

		if (token == null) {
			return chain.filter(sanitized);
		}

		try {
			jwtTokenProvider.validateAccessTokenOrThrow(token);
		} catch (Exception e) {
			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			return exchange.getResponse().setComplete();
		}

		Authentication authentication = jwtTokenProvider.getAuthentication(token);

		String userId = String.valueOf(jwtTokenProvider.getUserId(token));
		String role = jwtTokenProvider.getRole(token);
		String rolesHeader = role == null ? "" : role;

		ServerWebExchange mutatedExchange = sanitized.mutate()
			.request(request -> request
				.header(HEADER_USER_ID, userId)
				.header(HEADER_USER_ROLE, rolesHeader))
			.build();

		return chain.filter(mutatedExchange)
			.contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
	}

	private ServerWebExchange stripInternalHeaders(ServerWebExchange exchange) {
		return exchange.mutate()
			.request(request -> request
				.headers(header -> {
					header.remove(HEADER_USER_ID);
					header.remove(HEADER_USER_ROLE);
				}))
			.build();
	}

	private String resolveBearerToken(ServerWebExchange exchange) {
		String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

		if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
			return authorization.substring(7);
		}

		return null;
	}

}
