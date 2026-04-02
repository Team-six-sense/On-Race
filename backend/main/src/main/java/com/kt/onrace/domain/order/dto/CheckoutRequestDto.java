package com.kt.onrace.domain.order.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CheckoutRequestDto(
	@NotBlank String prepareToken,
	@NotNull Long eventId,
	@NotNull Long eventCourseId,
	@NotNull Long eventPaceId,
	List<Long> selectedPackageIds,
	@NotNull Long expectedFinalAmount,
	Long addressId,
	String deliveryMemo
) {
}
