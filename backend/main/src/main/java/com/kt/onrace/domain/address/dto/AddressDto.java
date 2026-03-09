package com.kt.onrace.domain.address.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AddressDto {

	public record SaveRequest(
		@NotBlank String receiverName,
		@Size(max = 20, message = "주소 별칭은 20자를 초과할 수 없습니다.")
		@Pattern(regexp = "^[가-힣a-zA-Z0-9 ]*$", message = "주소 별칭에는 한글, 영문, 숫자, 공백만 사용할 수 있습니다.")
		String label,
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
}
