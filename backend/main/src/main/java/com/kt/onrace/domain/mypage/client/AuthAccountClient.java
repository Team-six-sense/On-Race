package com.kt.onrace.domain.mypage.client;

public interface AuthAccountClient {

	AccountSummary getMyInfo(Long userId);

	record AccountSummary(
		String email,
		String name,
		String phone,
		String authProvider,
		boolean canChangePassword,
		String verificationStatus,
		boolean marketingConsent
	) {
	}
}
