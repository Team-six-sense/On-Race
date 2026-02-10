package com.kt.onrace.domain.auth.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.kt.onrace.domain.auth.entity.User;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.common.exception.ErrorCode;

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
	private final CustomUserDetailsService userDetailsService;

	//OS 기본 charset에 의존할 수 있어서 명시적으로 UTF-8 설정
	private SecretKey getSigningKey() {
			return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
	}


	public String generateAccessToken(User user) {
			Date now = new Date();
			Date expiry = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration());
			String jti = UUID.randomUUID().toString();

			return Jwts.builder()
							.subject(user.getLoginId())
							.id(jti)
							.claim("role", user.getRole().name())
							.issuedAt(now)
							.expiration(expiry)
							.signWith(getSigningKey())
							.compact();
	}


	public String generateRefreshToken(User user){
			Date now = new Date();
			Date expiry = new Date(now.getTime() + jwtProperties.getRefreshTokenExpiration());

			return Jwts.builder()
							.subject(user.getLoginId())
							.issuedAt(now)
							.expiration(expiry)
							.signWith(getSigningKey())
							.compact();
	}

	public LocalDateTime getRefreshTokenExpiryDateTime() {
			Instant expiresAt = Instant.now().plusMillis(jwtProperties.getRefreshTokenExpiration());
			return LocalDateTime.ofInstant(expiresAt, ZoneId.systemDefault());
	}

	private Jws<Claims> parseClaims(String token) {
			return Jwts.parser()
							.verifyWith(getSigningKey())
							.build()
							.parseSignedClaims(token);
	}

	public String getLoginId(String token) {
			try {
					return parseClaims(token).getPayload().getSubject();
			} catch (ExpiredJwtException e) {
					return e.getClaims().getSubject();
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
					throw new BusinessException(ErrorCode.AUTH_EXPIRED_JWT_TOKEN);
			} catch (MalformedJwtException e) {
					throw new BusinessException(ErrorCode.AUTH_MALFORMED_JWT_TOKEN);
			} catch (JwtException | IllegalArgumentException e) {
					throw new BusinessException(ErrorCode.AUTH_INVALID_JWT_TOKEN);
			}
	}

	public Authentication getAuthentication(String token) {
			String loginId = getLoginId(token);
			var userDetails = userDetailsService.loadUserByUsername(loginId);
			return new UsernamePasswordAuthenticationToken(
							userDetails,
							null,
							userDetails.getAuthorities()
			);
	}

	// parseClaims가 pricate이라 parseClaims로 access토큰 블랙리스트 등록을위해서 따로 설정
	public String getJti(String token) {
			try {
					return parseClaims(token).getPayload().getId();
			} catch (ExpiredJwtException e) {
					return e.getClaims().getId();
			} catch (JwtException | IllegalArgumentException e) {
					throw new BusinessException(ErrorCode.AUTH_INVALID_JWT_TOKEN);
			}
	}

	public Date getExpiration(String token) {
			try {
					return parseClaims(token).getPayload().getExpiration();
			} catch (ExpiredJwtException e) {
					return e.getClaims().getExpiration();
			}catch (JwtException | IllegalArgumentException e) {
					throw new BusinessException(ErrorCode.AUTH_INVALID_JWT_TOKEN);
			}
	}
}
