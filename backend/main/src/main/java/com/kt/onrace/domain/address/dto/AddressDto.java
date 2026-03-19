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
		Boolean isDefault,
		String label
	) {
	}

	public record Response(
		Long id,
		String label,
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

	public record DefaultResponse(
		boolean hasAddress,
		Response address
	) {
		public static DefaultResponse empty() {
			return new DefaultResponse(false, null);
		}

		public static DefaultResponse from(com.kt.onrace.domain.address.entity.Address address) {
			return new DefaultResponse(true, Response.from(address));
		}
	}
}
