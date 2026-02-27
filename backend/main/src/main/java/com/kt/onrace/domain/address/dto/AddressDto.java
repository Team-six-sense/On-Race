package com.kt.onrace.domain.address.dto;

import jakarta.validation.constraints.NotBlank;

public class AddressDto {

	public record SaveRequest(
		@NotBlank String receiverName,
		@NotBlank String phone,
		@NotBlank String zipcode,
		@NotBlank String address1,
		String address2,
		String memo,
		Boolean isDefault
	) {
	}

	public record Response(
		Long id,
		String receiverName,
		String phone,
		String zipcode,
		String address1,
		String address2,
		String memo,
		boolean isDefault
	) {
		public static Response from(com.kt.onrace.domain.address.entity.Address address) {
			return new Response(
				address.getId(),
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
}
