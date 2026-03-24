package com.kt.onrace.domain.mypage.dto;

/**
 * 마이페이지 계정 화면 전용 응답 DTO이다.
 * 계정 원천 데이터는 auth에서 읽고, main은 화면용 read-model만 조립한다.
 */
public record MyPageAccountResponse(
	String name,
	String email,
	String phone,
	boolean canChangePassword,
	String verificationStatus,
	boolean marketingConsent
) {
}
