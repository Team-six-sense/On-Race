package com.kt.onrace.domain.mypage.client;

public interface AuthAccountClient {

	AccountSummary getMyInfo(Long userId);

	record AccountSummary(
		String name,
		String phone,
		String authProvider,
		String verificationStatus,
		boolean marketingConsent
	) {
	}
}
