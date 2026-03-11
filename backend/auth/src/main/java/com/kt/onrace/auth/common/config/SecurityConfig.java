package com.kt.onrace.auth.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.kt.onrace.auth.common.handler.OAuth2AuthenticationFailureHandler;
import com.kt.onrace.auth.common.handler.OAuth2AuthenticationSuccessHandler;
import com.kt.onrace.auth.common.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.kt.onrace.auth.service.CustomOAuth2UserService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final CustomOAuth2UserService customOAuth2UserService;
	private final OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler;
	private final OAuth2AuthenticationFailureHandler oAuth2FailureHandler;
	private final HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;

	private static final String[] GET_PERMIT_ALL = { "/api/health/**", "/swagger-ui.html", "/swagger-ui/**",
			"/v3/api-docs/**", "/actuator/**", "/api/main/**" };
	private static final String[] POST_PERMIT_ALL = {};
	private static final String[] PUT_PERMIT_ALL = { "/api/v1/public/**" };
	private static final String[] PATCH_PERMIT_ALL = { "/api/v1/public/**" };
	private static final String[] DELETE_PERMIT_ALL = { "/api/v1/public/**" };

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.logout(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(request -> {
					request.requestMatchers(HttpMethod.GET, GET_PERMIT_ALL).permitAll();
					request.requestMatchers(HttpMethod.POST, POST_PERMIT_ALL).permitAll();
					request.requestMatchers(HttpMethod.PATCH, PATCH_PERMIT_ALL).permitAll();
					request.requestMatchers(HttpMethod.PUT, PUT_PERMIT_ALL).permitAll();
					request.requestMatchers(HttpMethod.DELETE, DELETE_PERMIT_ALL).permitAll();
					// OAuth2 엔드포인트 허용
					request.requestMatchers("/oauth2/**", "/login/oauth2/code/**").permitAll();
					// auth 서비스는 Gateway 뒤에 위치하며 JWT 검증은 Gateway가 담당한다.
					request.requestMatchers("/**").permitAll();
					request.anyRequest().authenticated();
				})
				.oauth2Login(oauth2 -> oauth2
						.authorizationEndpoint(endpoint -> endpoint
								.authorizationRequestRepository(cookieAuthorizationRequestRepository))
						.userInfoEndpoint(endpoint -> endpoint
								.userService(customOAuth2UserService))
						.successHandler(oAuth2SuccessHandler)
						.failureHandler(oAuth2FailureHandler));

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}
}
