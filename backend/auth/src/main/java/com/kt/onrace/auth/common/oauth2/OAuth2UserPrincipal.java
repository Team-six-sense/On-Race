package com.kt.onrace.auth.common.oauth2;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import lombok.Getter;

/**
 * Spring Security OAuth2 인증 주체.
 * 성공 핸들러에서 JWT 발급에 필요한 유저 정보를 전달하기 위해 사용.
 */
@Getter
public class OAuth2UserPrincipal implements OAuth2User {

	private final Long userId;
	private final String email;
	private final String role;
	private final Map<String, Object> attributes;
	private final Collection<? extends GrantedAuthority> authorities;

	public OAuth2UserPrincipal(Long userId, String email, String role, Map<String, Object> attributes) {
		this.userId = userId;
		this.email = email;
		this.role = role;
		this.attributes = attributes;
		this.authorities = Collections.singleton(new SimpleGrantedAuthority(role));
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getName() {
		return email;
	}
}
