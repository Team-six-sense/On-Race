package com.kt.onrace.domain.mypage.dto;

public enum MyPageAccountType {
	EMAIL,
	SNS;

	public static MyPageAccountType fromAuthProvider(String authProvider) {
		return "LOCAL".equalsIgnoreCase(authProvider) ? EMAIL : SNS;
	}
}
