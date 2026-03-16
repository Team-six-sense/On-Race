package com.kt.onrace.domain.order.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.kt.onrace.domain.order.entity.OrderStatus;

public record OrderDetailResponseDto(
	Long eventId,
	String orderNumber,
	OrderStatus orderStatus,
	LocalDateTime createdAt,
	Long itemTotalAmount,
	Long shippingFee,
	Long discountAmount,
	Long finalAmount,
	String eventTitle,
	String thumbnailUrl,
	String courseName,
	String paceName,
	String recipientName,
	String addressLabel,
	String recipientPhone,
	String zipCode,
	String address,
	String detailAddress,
	String deliveryMemo,
	List<PackageInfo> packages
) {
	public record PackageInfo(
		Long eventPackageId,
		String name,
		Long price
	) {
	}
}
