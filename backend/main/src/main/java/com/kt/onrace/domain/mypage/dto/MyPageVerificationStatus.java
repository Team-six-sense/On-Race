package com.kt.onrace.domain.mypage.dto;

public enum MyPageVerificationStatus {
	PENDING,
	COMPLETED,
	FAILED;

	public static MyPageVerificationStatus fromAuthStatus(String verificationStatus) {
		if ("VERIFIED".equalsIgnoreCase(verificationStatus)) {
			return COMPLETED;
		}
		if ("FAILED".equalsIgnoreCase(verificationStatus)) {
			return FAILED;
		}
		return PENDING;
	}
}
