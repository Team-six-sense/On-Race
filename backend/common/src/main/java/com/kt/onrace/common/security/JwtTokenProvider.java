package com.kt.onrace.common.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

	private final JwtProperties jwtProperties;

	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
	}

	public String generateAccessToken(Long userId, String loginId, String role) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration());
		String jti = UUID.randomUUID().toString();

		return Jwts.builder()
			.subject(String.valueOf(userId))
			.id(jti)
			.claim("loginId", loginId)
			.claim("roles", List.of(role))
			.issuedAt(now)
			.expiration(expiry)
			.signWith(getSigningKey())
			.compact();
	}

	public String generateRefreshToken(Long userId) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + jwtProperties.getRefreshTokenExpiration());

		return Jwts.builder()
			.subject(String.valueOf(userId))
			.issuedAt(now)
			.expiration(expiry)
			.signWith(getSigningKey())
			.compact();
	}

	public LocalDateTime getRefreshTokenExpiryDateTime() {
		Instant expiresAt = Instant.now().plusMillis(jwtProperties.getRefreshTokenExpiration());
		return LocalDateTime.ofInstant(expiresAt, ZoneId.systemDefault());
	}

	public Jws<Claims> parseClaims(String token) {
		return Jwts.parser()
			.verifyWith(getSigningKey())
			.build()
			.parseSignedClaims(token);
	}

	public Long getUserId(String token) {
		try {
			return Long.valueOf(parseClaims(token).getPayload().getSubject());
		} catch (ExpiredJwtException e) {
			return Long.valueOf(e.getClaims().getSubject());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public String getLoginId(String token) {
		try {
			Claims claims = parseClaims(token).getPayload();
			String loginId = claims.get("loginId", String.class);
			return loginId != null ? loginId : claims.getSubject();
		} catch (ExpiredJwtException e) {
			Claims claims = e.getClaims();
			String loginId = claims.get("loginId", String.class);
			return loginId != null ? loginId : claims.getSubject();
		}
	}

	public String getRole(String token) {
		try {
			List<?> roles = parseClaims(token).getPayload().get("roles", List.class);
			if (roles != null && !roles.isEmpty()) {
				return roles.get(0).toString();
			}
			return parseClaims(token).getPayload().get("role", String.class);
		} catch (ExpiredJwtException e) {
			List<?> roles = e.getClaims().get("roles", List.class);
			if (roles != null && !roles.isEmpty()) {
				return roles.get(0).toString();
			}
			return e.getClaims().get("role", String.class);
		}
	}

	public boolean validateToken(String token) {
		try {
			parseClaims(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	public void validateAccessTokenOrThrow(String token) {
		try {
			parseClaims(token);
		} catch (ExpiredJwtException e) {
			throw new BusinessException(BusinessErrorCode.AUTH_EXPIRED_JWT_TOKEN);
		} catch (MalformedJwtException e) {
			throw new BusinessException(BusinessErrorCode.AUTH_MALFORMED_JWT_TOKEN);
		} catch (JwtException | IllegalArgumentException e) {
			throw new BusinessException(BusinessErrorCode.AUTH_INVALID_JWT_TOKEN);
		}
	}

	public Authentication getAuthentication(String token) {
		String loginId = getLoginId(token);
		String roleStr = getRole(token);

		List<SimpleGrantedAuthority> authorities = new ArrayList<>();
		if (roleStr != null && !roleStr.isEmpty()) {
			String authority = roleStr.startsWith("ROLE_") ? roleStr : "ROLE_" + roleStr;
			authorities.add(new SimpleGrantedAuthority(authority));
		}

		org.springframework.security.core.userdetails.User principal = new org.springframework.security.core.userdetails.User(
			loginId, "", authorities);

		return new UsernamePasswordAuthenticationToken(
			principal,
			token,
			authorities);
	}

	public String getJti(String token) {
		try {
			return parseClaims(token).getPayload().getId();
		} catch (ExpiredJwtException e) {
			return e.getClaims().getId();
		} catch (JwtException | IllegalArgumentException e) {
			throw new BusinessException(BusinessErrorCode.AUTH_INVALID_JWT_TOKEN);
		}
	}

	public Date getExpiration(String token) {
		try {
			return parseClaims(token).getPayload().getExpiration();
		} catch (ExpiredJwtException e) {
			return e.getClaims().getExpiration();
		} catch (JwtException | IllegalArgumentException e) {
			throw new BusinessException(BusinessErrorCode.AUTH_INVALID_JWT_TOKEN);
		}
	}
}
