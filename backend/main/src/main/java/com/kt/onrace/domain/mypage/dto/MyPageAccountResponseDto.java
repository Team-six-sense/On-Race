package com.kt.onrace.domain.mypage.dto;

/**
 * 마이페이지 계정관리 첫 화면에서 사용하는 계정 요약 응답 DTO이다.
 * auth 원천값과 기본 배송지 요약 정보를 조합한 aggregate 응답이다.
 */
public record MyPageAccountResponseDto(
	MyPageAccountType accountType,
	String authProvider,
	String email,
	String name,
	String phone,
	boolean canChangePassword,
	MyPageVerificationStatus verificationStatus,
	boolean marketingConsent,
	MyPageAddressResponseDto address
) {
}
