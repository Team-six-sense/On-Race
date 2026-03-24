package com.kt.onrace.auth.dto;

import com.kt.onrace.auth.entity.AuthProvider;
import com.kt.onrace.auth.entity.VerificationStatus;
import com.kt.onrace.auth.entity.UserStatus;

public record AccountMeResponse(
	Long id,
	String email,
	String name,
	String phone,
	boolean canChangePassword,
	VerificationStatus verificationStatus,
	boolean marketingConsent,
	AuthProvider authProvider,
	UserStatus status
) {
}
