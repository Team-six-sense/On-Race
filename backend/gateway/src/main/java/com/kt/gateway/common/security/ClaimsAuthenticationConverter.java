package com.kt.gateway.common.security;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;

@Component
public class ClaimsAuthenticationConverter {

	@SuppressWarnings("unchecked")
	public Authentication convert(Claims claims, String token) {
		String subject = claims.getSubject();

		List<String> roles = claims.get("roles", List.class);
		Collection<SimpleGrantedAuthority> authorities =
			(roles == null ? Collections.<String>emptyList() : roles).stream()
				.map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
				.map(SimpleGrantedAuthority::new)
				.collect(Collectors.toList());

		return new UsernamePasswordAuthenticationToken(subject, token, authorities);
	}

}
