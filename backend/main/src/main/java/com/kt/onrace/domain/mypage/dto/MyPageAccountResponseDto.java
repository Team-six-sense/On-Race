package com.kt.onrace.domain.mypage.dto;

import java.util.List;

import com.kt.onrace.domain.address.dto.AddressDto;
import com.kt.onrace.domain.mypage.client.AuthAccountClient;

/**
 * 마이페이지 회원정보 화면에서 사용하는 회원정보 응답 DTO이다.
 * auth 원천값과 배송지 전체 목록을 조합한 aggregate 응답이다.
 */
public record MyPageAccountResponseDto(
	Long id,
	String name,
	String phoneNumber,
	String email,
	boolean isPassAuth,
	List<AddressItem> addressList
) {
	public MyPageAccountResponseDto {
		addressList = addressList == null ? List.of() : List.copyOf(addressList);
	}

	public static MyPageAccountResponseDto from(
		Long userId,
		AuthAccountClient.AccountSummary account,
		List<AddressDto.Response> addressList
	) {
		List<AddressItem> items = addressList == null
			? List.of()
			: addressList.stream()
				.map(AddressItem::from)
				.toList();

		return new MyPageAccountResponseDto(
			userId,
			account.name(),
			account.phone(),
			account.email(),
			"VERIFIED".equalsIgnoreCase(account.verificationStatus()),
			items
		);
	}

	public record AddressItem(
		Long id,
		String label,
		String receiverName,
		String phoneNumber,
		String zipcode,
		String address1,
		String address2,
		String memo,
		boolean isDefault
	) {
		public static AddressItem from(AddressDto.Response address) {
			return new AddressItem(
				address.id(),
				address.label(),
				address.receiverName(),
				address.phone(),
				address.zipcode(),
				address.address1(),
				address.address2(),
				address.memo(),
				address.isDefault()
			);
		}
	}
}
