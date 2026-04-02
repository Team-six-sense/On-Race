package com.kt.onrace.domain.order.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

class CheckoutRequestDtoTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void ignoresLegacyAddressFieldsDuringDeserialization() throws Exception {
		String json = """
			{
			  "prepareToken": "prepare-token",
			  "eventId": 1,
			  "eventCourseId": 10,
			  "eventPaceId": 20,
			  "selectedPackageIds": [30],
			  "expectedFinalAmount": 63000,
			  "addressId": 101,
			  "recipientName": "직접입력",
			  "recipientPhone": "01012345678",
			  "zipCode": "12345",
			  "address": "서울 어딘가",
			  "detailAddress": "101동",
			  "deliveryMemo": "직접 입력 메모"
			}
			""";

		CheckoutRequestDto request = objectMapper.readValue(json, CheckoutRequestDto.class);

		assertThat(request.prepareToken()).isEqualTo("prepare-token");
		assertThat(request.eventId()).isEqualTo(1L);
		assertThat(request.eventCourseId()).isEqualTo(10L);
		assertThat(request.eventPaceId()).isEqualTo(20L);
		assertThat(request.selectedPackageIds()).isEqualTo(List.of(30L));
		assertThat(request.expectedFinalAmount()).isEqualTo(63000L);
		assertThat(request.addressId()).isEqualTo(101L);
		assertThat(request.deliveryMemo()).isEqualTo("직접 입력 메모");
	}
}
