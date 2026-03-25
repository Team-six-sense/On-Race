package com.kt.onrace.domain.mypage.dto;

import com.kt.onrace.domain.address.dto.AddressDto;

/**
 * 마이페이지 계정/개요 화면에서 사용하는 기본 배송지 요약 DTO이다.
 * 배송지 관리 상세 정보는 기존 address 도메인 응답을 재사용하고, 마이페이지는 요약 필드만 노출한다.
 */
public record MyPageAddressDto(
	String receiverName,
	String label,
	String address,
	String phone
) {
	public static MyPageAddressDto from(AddressDto.Response address) {
		return new MyPageAddressDto(
			address.receiverName(),
			address.label(),
			composeAddress(address.address1(), address.address2()),
			address.phone()
		);
	}

	private static String composeAddress(String address1, String address2) {
		String primary = address1 == null ? "" : address1.trim();
		String secondary = address2 == null ? "" : address2.trim();

		if (secondary.isBlank()) {
			return primary;
		}
		return (primary + " " + secondary).trim();
	}
}
