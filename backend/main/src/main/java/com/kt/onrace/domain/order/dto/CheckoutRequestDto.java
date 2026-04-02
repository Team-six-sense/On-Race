package com.kt.onrace.domain.order.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CheckoutRequestDto(
	String prepareToken,
	Long eventId,
	Long eventCourseId,
	Long eventPaceId,
	List<Long> selectedPackageIds,
	Long expectedFinalAmount,
	Long addressId,
	String deliveryMemo
) {
}
