package com.kt.onrace.auth.common.handler;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.kt.onrace.auth.common.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.kt.onrace.auth.config.OAuthProperties;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	private final OAuthProperties oAuthProperties;
	private final HttpCookieOAuth2AuthorizationRequestRepository cookieRepository;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException {
		String targetUrl = UriComponentsBuilder.fromUriString(oAuthProperties.getRedirectUri())
				.queryParam("error", exception.getMessage())
				.build().toUriString();

		cookieRepository.removeAuthorizationRequestCookies(request, response);
		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}
}
