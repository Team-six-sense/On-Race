package com.kt.onrace.domain.order.dto;

import java.time.LocalDateTime;
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
	PaymentDetail paymentDetail,
	ShippingAddressInfo shippingAddress
) {

	public static CheckoutPrepareResponseDto of(
		String prepareToken,
		Event event,
		EventCourse course,
		EventPace pace,
		List<EventPackage> packages,
		PaymentDetail paymentDetail,
		String thumbnailUrl,
		ShippingAddressInfo shippingAddress
	) {
		return CheckoutPrepareResponseDto.builder()
			.prepareToken(prepareToken)
			.orderRequestInfo(
				new OrderRequestInfo(
					event.getId(),
					event.getTitle(),
					event.getEventAt(),
					event.getVenue(),
					thumbnailUrl,
					course.getName(),
					pace.getName(),
					course.getPrice()))
			.packages(packages.stream()
				.map(p -> new PackageInfo(p.getId(), p.getName(), p.getPrice(), p.getDescription()))
				.toList())
			.paymentDetail(paymentDetail)
			.shippingAddress(shippingAddress)
			.build();
	}

	public record OrderRequestInfo(
		Long eventId,
		String eventName,
		LocalDateTime eventAt,
		String venue,
		String thumbnailUrl,
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

	public record ShippingAddressInfo(
		boolean hasAddress,
		Long addressId,
		String receiverName,
		String phone,
		String zipcode,
		String address1,
		String address2,
		String memo,
		boolean isDefault
	) {
		public static ShippingAddressInfo empty() {
			return new ShippingAddressInfo(false, null, null, null, null, null, null, null, false);
		}
	}

}
