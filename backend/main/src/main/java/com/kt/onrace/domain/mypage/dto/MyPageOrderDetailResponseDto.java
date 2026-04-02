package com.kt.onrace.domain.mypage.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 상세 화면에 필요한 주문, 배송지, 금액, 패키지 정보를 담는 응답 DTO이다.
 * 패키지 목록은 불변 컬렉션으로 보정해 전달한다.
 */
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

	public static MyPageOrderDetailResponseDto of(
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
		return new MyPageOrderDetailResponseDto(
			eventId,
			orderNumber,
			status,
			actionType,
			actionLabel,
			actionEnabled,
			orderedAt,
			paymentDeadlineAt,
			eventTitle,
			thumbnailUrl,
			courseName,
			paceName,
			itemTotalAmount,
			shippingFee,
			discountAmount,
			finalAmount,
			recipientName,
			addressLabel,
			recipientPhone,
			zipCode,
			address,
			detailAddress,
			deliveryMemo,
			paymentMethod,
			hasShipmentInfo,
			shipmentStatus,
			trackingNumber,
			canCancel,
			canRefund,
			canExchange,
			packages
		);
	}

	/**
	 * 주문에 포함된 패키지 한 건의 이름과 금액 정보를 표현한다.
	 */
	public record PackageInfo(
		Long eventPackageId,
		String name,
		Long price
	) {
		public static PackageInfo of(
			Long eventPackageId,
			String name,
			Long price
		) {
			return new PackageInfo(eventPackageId, name, price);
		}
	}
}
