package com.kt.onrace.domain.order.dto;

import java.util.List;

import com.kt.onrace.domain.event.entity.Event;
import com.kt.onrace.domain.event.entity.EventCourse;
import com.kt.onrace.domain.event.entity.EventPace;
import com.kt.onrace.domain.event.entity.EventPackage;

import lombok.Builder;

@Builder
public record CheckoutPrepareResponseDto(
	String prepareToken,
	OrderRequestInfo orderRequestInfo,
	List<PackageInfo> packages,
	PaymentDetail paymentDetail
) {

	public static CheckoutPrepareResponseDto of(
		String prepareToken,
		Event event,
		EventCourse course,
		EventPace pace,
		List<EventPackage> packages,
		PaymentDetail paymentDetail
	) {
		return CheckoutPrepareResponseDto.builder()
			.prepareToken(prepareToken)
			.orderRequestInfo(
				new OrderRequestInfo(event.getTitle(), course.getName(), pace.getName(), course.getPrice()))
			.packages(packages.stream()
				.map(p -> new PackageInfo(p.getId(), p.getName(), p.getPrice(), p.getDescription()))
				.toList())
			.paymentDetail(paymentDetail)
			.build();
	}

	public record OrderRequestInfo(
		String eventName,
		String courseName,
		String paceName,
		Long coursePrice
	) {
	}

	public record PackageInfo(
		Long id,
		String name,
		Long price,
		String description
	) {
	}

	public record PaymentDetail(
		Long itemTotalAmount,
		Long shippingFee,
		Long discountAmount,
		Long finalAmount
	) {
	}

}
