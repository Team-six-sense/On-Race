package com.kt.onrace.auth.dto;

import com.kt.onrace.auth.entity.AuthProvider;
import com.kt.onrace.auth.entity.UserStatus;

public record AccountMeResponse(
	Long id,
	String email,
	String name,
	AuthProvider authProvider,
	UserStatus status
) {
}
