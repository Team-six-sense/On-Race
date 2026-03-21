package com.kt.onrace.domain.mypage.dto;

import com.kt.onrace.domain.address.entity.Address;

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
	public static MyPageAddressDto from(Address address) {
		return new MyPageAddressDto(
			address.getId(),
			address.getLabel(),
			address.getReceiverName(),
			address.getPhone(),
			address.getZipcode(),
			address.getAddress1(),
			address.getAddress2(),
			address.getMemo(),
			address.isDefault()
		);
	}
}
