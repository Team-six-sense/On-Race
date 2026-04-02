package com.kt.onrace.domain.order.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.domain.order.config.OrderPrepareTokenProperties;
import com.kt.onrace.domain.order.dto.CheckoutRequestDto;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * checkout prepare 단계의 요청 맥락을 서명 토큰으로 넘기고, nonce 소비로 재사용을 막는다.
 */
@Service
public class OrderPrepareTokenService {

	private static final String CLAIM_TOKEN_TYPE = "tokenType";
	private static final String CLAIM_EVENT_ID = "eventId";
	private static final String CLAIM_EVENT_COURSE_ID = "eventCourseId";
	private static final String CLAIM_EVENT_PACE_ID = "eventPaceId";
	private static final String CLAIM_NONCE = "nonce";
	private static final String TOKEN_TYPE = "ORDER_PREPARE";

	private final OrderPrepareTokenProperties properties;
	private final OrderPrepareTokenNonceStore nonceStore;
	private final String jwtSecret;

	public String issueToken(Long userId, Long eventId, Long eventCourseId, Long eventPaceId) {
		Instant now = Instant.now();
		Instant expiresAt = now.plus(properties.ttl());

		// checkout 시 다시 대조할 식별값과 재사용 방지용 nonce를 함께 담는다.
		return Jwts.builder()
			.subject(String.valueOf(userId))
			.claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE)
			.claim(CLAIM_EVENT_ID, eventId)
			.claim(CLAIM_EVENT_COURSE_ID, eventCourseId)
			.claim(CLAIM_EVENT_PACE_ID, eventPaceId)
			.claim(CLAIM_NONCE, UUID.randomUUID().toString())
			.issuedAt(Date.from(now))
			.expiration(Date.from(expiresAt))
			.signWith(getSigningKey())
			.compact();
	}

	public ValidatedPrepareToken validatePrepareToken(String token, Long userId, CheckoutRequestDto request) {
		Claims claims = parseClaims(token);
		// prepare 응답 당시의 사용자/이벤트 선택값이 checkout 요청과 완전히 일치해야 한다.
		validateClaim(claims.getSubject(), String.valueOf(userId));
		validateClaim(claims.get(CLAIM_TOKEN_TYPE, String.class), TOKEN_TYPE);
		validateClaim(toLong(claims.get(CLAIM_EVENT_ID)), request.eventId());
		validateClaim(toLong(claims.get(CLAIM_EVENT_COURSE_ID)), request.eventCourseId());
		validateClaim(toLong(claims.get(CLAIM_EVENT_PACE_ID)), request.eventPaceId());

		String nonce = claims.get(CLAIM_NONCE, String.class);
		if (nonce == null || nonce.isBlank()) {
			throw new BusinessException(BusinessErrorCode.COMMON_INVALID_FORMAT);
		}

		Date expiration = claims.getExpiration();
		if (expiration == null) {
			throw new BusinessException(BusinessErrorCode.COMMON_INVALID_FORMAT);
		}

		LocalDateTime expiresAt = LocalDateTime.ofInstant(expiration.toInstant(), ZoneId.systemDefault());
		if (!expiresAt.isAfter(LocalDateTime.now())) {
			throw new BusinessException(BusinessErrorCode.COMMON_INVALID_REQUEST);
		}

		return new ValidatedPrepareToken(nonce, expiresAt);
	}

	public void consumePrepareToken(ValidatedPrepareToken validatedPrepareToken) {
		Duration remainingTtl = Duration.between(LocalDateTime.now(), validatedPrepareToken.expiresAt());
		if (remainingTtl.isZero() || remainingTtl.isNegative()) {
			throw new BusinessException(BusinessErrorCode.COMMON_INVALID_REQUEST);
		}

		// 남은 만료 시간 동안 nonce를 점유해 같은 토큰의 중복 checkout을 막는다.
		boolean marked = nonceStore.markAsUsed(validatedPrepareToken.nonce(), remainingTtl);
		if (!marked) {
			throw new BusinessException(BusinessErrorCode.COMMON_DUPLICATE_REQUEST);
		}
	}

	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
	}

	private Claims parseClaims(String token) {
		try {
			return Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
		} catch (ExpiredJwtException exception) {
			throw new BusinessException(BusinessErrorCode.COMMON_INVALID_REQUEST);
		} catch (JwtException | IllegalArgumentException exception) {
			throw new BusinessException(BusinessErrorCode.COMMON_INVALID_FORMAT);
		}
	}

	private void validateClaim(Object actual, Object expected) {
		if (actual == null || expected == null || !actual.equals(expected)) {
			throw new BusinessException(BusinessErrorCode.COMMON_INVALID_FORMAT);
		}
	}

	private Long toLong(Object claimValue) {
		if (claimValue instanceof Number number) {
			return number.longValue();
		}
		if (claimValue instanceof String text) {
			try {
				return Long.parseLong(text);
			} catch (NumberFormatException exception) {
				return null;
			}
		}
		return null;
	}

	public record ValidatedPrepareToken(
		String nonce,
		LocalDateTime expiresAt
	) {
	}

	public OrderPrepareTokenService(
		OrderPrepareTokenProperties properties,
		OrderPrepareTokenNonceStore nonceStore,
		@Value("${jwt.secret}") String jwtSecret
	) {
		this.properties = properties;
		this.nonceStore = nonceStore;
		this.jwtSecret = jwtSecret;
	}
}
