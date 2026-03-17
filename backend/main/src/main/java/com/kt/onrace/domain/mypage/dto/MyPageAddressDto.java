package com.kt.onrace.domain.mypage.dto;

public record MyPageAddressDto(
	Long addressId,
	String label,
	String receiverName,
	String phone,
	String zipcode,
	String address1,
	String address2,
	String memo,
	boolean isDefault
) {
}
