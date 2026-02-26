package com.kt.onrace.auth.api.dto;

public record TokenRefreshResponse(
	String accessToken,
	long expiresIn
) {}
