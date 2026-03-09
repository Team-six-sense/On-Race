package com.kt.onrace.auth.dto;

public record TokenRefreshResponse(
	String accessToken,
	String refreshToken,
	long expiresIn
) {
}
