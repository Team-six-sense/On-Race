package com.kt.onrace.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record OAuthLoginRequest(
		@NotBlank String providerId,
		String email,
		String name
) {
}
