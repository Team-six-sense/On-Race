package com.kt.onrace.auth.common.oauth2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.Arrays;
import java.util.Optional;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

/**
 * Stateless 환경에서 OAuth2 인가 요청을 세션 대신 쿠키에 저장하는 저장소.
 * 인가 코드 플로우의 state 파라미터 위변조 방지를 위해 사용.
 */
@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
		implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

	private static final String COOKIE_NAME = "oauth2_auth_request";
	private static final int COOKIE_EXPIRE_SECONDS = 180;

	@Override
	public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
		return getCookie(request, COOKIE_NAME)
				.map(this::deserialize)
				.orElse(null);
	}

	@Override
	public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
			HttpServletRequest request, HttpServletResponse response) {
		if (authorizationRequest == null) {
			deleteCookie(request, response, COOKIE_NAME);
			return;
		}
		addCookie(response, COOKIE_NAME, serialize(authorizationRequest), COOKIE_EXPIRE_SECONDS);
	}

	@Override
	public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
			HttpServletResponse response) {
		return loadAuthorizationRequest(request);
	}

	public void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
		deleteCookie(request, response, COOKIE_NAME);
	}

	private Optional<Cookie> getCookie(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) return Optional.empty();
		return Arrays.stream(cookies)
				.filter(c -> c.getName().equals(name))
				.findFirst();
	}

	private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
		Cookie cookie = new Cookie(name, value);
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		cookie.setMaxAge(maxAge);
		response.addCookie(cookie);
	}

	private void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) return;
		Arrays.stream(cookies)
				.filter(c -> c.getName().equals(name))
				.findFirst()
				.ifPresent(cookie -> {
					cookie.setValue("");
					cookie.setPath("/");
					cookie.setMaxAge(0);
					response.addCookie(cookie);
				});
	}

	private String serialize(OAuth2AuthorizationRequest request) {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(bos)) {
			oos.writeObject(request);
			return Base64.getUrlEncoder().encodeToString(bos.toByteArray());
		} catch (IOException e) {
			throw new IllegalStateException("OAuth2AuthorizationRequest 직렬화 실패", e);
		}
	}

	private OAuth2AuthorizationRequest deserialize(Cookie cookie) {
		try {
			byte[] bytes = Base64.getUrlDecoder().decode(cookie.getValue());
			try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
				return (OAuth2AuthorizationRequest) ois.readObject();
			}
		} catch (IOException | ClassNotFoundException e) {
			return null;
		}
	}
}
