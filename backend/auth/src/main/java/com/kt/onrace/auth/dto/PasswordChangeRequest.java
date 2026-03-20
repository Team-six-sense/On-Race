package com.kt.onrace.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordChangeRequest(
	@NotBlank String currentPassword
) {
}
