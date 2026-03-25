package com.kt.onrace.queue.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.kt.onrace.queue.config.QueueProperties;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class QueueTokenGenerator {
	private final SecretKey key;
	private final long passTtlMillis;

	public QueueTokenGenerator(QueueProperties queueProperties) {
		this.key = Keys.hmacShaKeyFor(queueProperties.getTokenSecret().getBytes(StandardCharsets.UTF_8));
		this.passTtlMillis = queueProperties.getPassTtlSeconds() * 1000;
	}

	public String generatePassToken(Long userId, Long paceId) {
		Date now = new Date();
		return Jwts.builder()
			.subject(String.valueOf(userId))
			.claim("CLAIM_PACE_ID", paceId)
			.claim("type", "QUEUE_PASS")
			.issuedAt(now)
			.expiration(new Date(now.getTime() + passTtlMillis))
			.signWith(key)
			.compact();
	}
}
