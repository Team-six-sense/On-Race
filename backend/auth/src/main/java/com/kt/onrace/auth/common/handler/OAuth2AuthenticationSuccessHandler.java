package com.kt.onrace.auth.common.handler;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.kt.onrace.auth.common.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.kt.onrace.auth.common.oauth2.OAuth2UserPrincipal;
import com.kt.onrace.auth.config.OAuthProperties;
import com.kt.onrace.auth.service.TokenStoreService;
import com.kt.onrace.common.security.JwtProperties;
import com.kt.onrace.common.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final JwtTokenProvider jwtTokenProvider;
	private final JwtProperties jwtProperties;
	private final TokenStoreService tokenStoreService;
	private final OAuthProperties oAuthProperties;
	private final HttpCookieOAuth2AuthorizationRequestRepository cookieRepository;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {
		if (response.isCommitted()) return;

		OAuth2UserPrincipal principal = (OAuth2UserPrincipal) authentication.getPrincipal();

		String accessToken = jwtTokenProvider.generateAccessToken(
				principal.getUserId(), principal.getEmail(), principal.getRole());
		String refreshToken = jwtTokenProvider.generateRefreshToken(principal.getUserId());

		tokenStoreService.saveRefreshToken(
				principal.getUserId(), refreshToken, jwtProperties.getRefreshTokenExpiration());

		String targetUrl = UriComponentsBuilder.fromUriString(oAuthProperties.getRedirectUri())
				.queryParam("accessToken", accessToken)
				.queryParam("refreshToken", refreshToken)
				.build().toUriString();

		cookieRepository.removeAuthorizationRequestCookies(request, response);
		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}
}
