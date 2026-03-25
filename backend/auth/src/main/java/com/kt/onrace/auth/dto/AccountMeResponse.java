package com.kt.onrace.auth.dto;

import com.kt.onrace.auth.entity.AuthProvider;
import com.kt.onrace.auth.entity.UserStatus;
import com.kt.onrace.auth.entity.VerificationStatus;

public record AccountMeResponse(
	Long id,
	String email,
	String name,
	AuthProvider authProvider,
	UserStatus status,
	String phone,
	boolean canChangePassword,
	VerificationStatus verificationStatus,
	boolean marketingConsent
) {
}
