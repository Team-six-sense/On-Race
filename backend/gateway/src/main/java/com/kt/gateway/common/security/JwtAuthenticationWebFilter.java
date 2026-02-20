package com.kt.gateway.common.security;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationWebFilter implements WebFilter {

	private final JwtTokenProvider jwtTokenProvider;
	private final ClaimsAuthenticationConverter claimsAuthenticationConverter;

	@Override
	public @NonNull Mono<Void> filter(
		@NonNull ServerWebExchange exchange,
		@NonNull WebFilterChain chain) {

		String token = resolveBearerToken(exchange);

		if (token == null || !jwtTokenProvider.validateToken(token)) {
			return chain.filter(exchange);
		}

		Claims claims = jwtTokenProvider.getClaims(token);
		Authentication authentication = claimsAuthenticationConverter.convert(claims, token);

		String userId = claims.getSubject();

		@SuppressWarnings("unchecked")
		List<String> roles = claims.get("roles", List.class);
		
		String rolesHeader = roles == null ? "" : String.join(",", roles);

		ServerWebExchange mutatedExchange = exchange.mutate()
			.request(request -> request
				.header("X-User-Id", userId)
				.header("X-User-Role", rolesHeader)
			).build();

		return chain.filter(mutatedExchange)
			.contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
	}

	private String resolveBearerToken(ServerWebExchange exchange) {
		String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

		if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
			return authorization.substring(7);
		}

		return null;
	}

}
