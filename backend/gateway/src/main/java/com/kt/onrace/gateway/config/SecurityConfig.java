package com.kt.onrace.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

import com.kt.onrace.gateway.filter.JwtAuthenticationWebFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationWebFilter jwtAuthenticationWebFilter;

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		http
			.csrf(ServerHttpSecurity.CsrfSpec::disable)
			.cors(Customizer.withDefaults())
			.httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
			.formLogin(ServerHttpSecurity.FormLoginSpec::disable)
			.securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
			.authorizeExchange(exchanges -> exchanges
				.pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.pathMatchers("/main/**").permitAll()
				.anyExchange().authenticated()
			)
			.addFilterAt(jwtAuthenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION);

		return http.build();
	}
}
