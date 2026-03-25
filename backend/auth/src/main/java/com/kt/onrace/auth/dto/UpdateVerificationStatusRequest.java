package com.kt.onrace.auth.dto;

import com.kt.onrace.auth.entity.VerificationStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateVerificationStatusRequest(
	@NotNull VerificationStatus verificationStatus
) {
}
