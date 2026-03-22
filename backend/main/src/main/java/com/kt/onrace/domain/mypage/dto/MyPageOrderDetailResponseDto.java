package com.kt.onrace.domain.mypage.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MyPageOrderDetailResponseDto(
	Long eventId,
	String orderNumber,
	String status,
	String actionType,
	String actionLabel,
	boolean actionEnabled,
	LocalDateTime orderedAt,
	LocalDateTime paymentDeadlineAt,
	String eventTitle,
	String thumbnailUrl,
	String courseName,
	String paceName,
	Long itemTotalAmount,
	Long shippingFee,
	Long discountAmount,
	Long finalAmount,
	String recipientName,
	String addressLabel,
	String recipientPhone,
	String zipCode,
	String address,
	String detailAddress,
	String deliveryMemo,
	String paymentMethod,
	boolean hasShipmentInfo,
	String shipmentStatus,
	String trackingNumber,
	boolean canCancel,
	boolean canRefund,
	boolean canExchange,
	List<PackageInfo> packages
) {
	public MyPageOrderDetailResponseDto {
		packages = packages == null ? List.of() : List.copyOf(packages);
	}

	public record PackageInfo(
		Long eventPackageId,
		String name,
		Long price
	) {
	}
}
