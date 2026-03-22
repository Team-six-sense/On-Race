package com.kt.onrace.domain.mypage.dto;

import com.kt.onrace.domain.address.entity.Address;

/**
 * 마이페이지에서 사용하는 배송지 단건 정보를 표현하는 DTO이다.
 * 주소 엔티티를 화면용 필드 구조로 변환하는 정적 팩토리를 제공한다.
 */
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
