package com.kt.onrace.domain.mypage.dto;

/**
 * 마이페이지 계정관리 첫 화면에서 사용하는 계정 요약 응답 DTO이다.
 * auth 원천값과 기본 배송지 요약 정보를 함께 담는다.
 */
public record MyPageAccountResponseDto(
	String name,
	String phone,
	String authProvider,
	String verificationStatus,
	boolean marketingConsent,
	MyPageAddressResponseDto address
) {
}
