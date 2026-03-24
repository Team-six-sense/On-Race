package com.kt.onrace.domain.address.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

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
		boolean isDefault,
		LocalDateTime createdAt
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
				address.isDefault(),
				address.getCreatedAt()
			);
		}

		@JsonProperty("addressId")
		public Long accountAddressId() {
			return id;
		}

		@JsonProperty("roadAddress")
		public String roadAddressAlias() {
			return address1;
		}

		@JsonProperty("detailAddress")
		public String detailAddressAlias() {
			return address2;
		}

		@JsonProperty("phoneNumber")
		public String phoneNumberAlias() {
			return phone;
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
