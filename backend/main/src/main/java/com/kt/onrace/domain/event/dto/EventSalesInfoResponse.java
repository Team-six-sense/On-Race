package com.kt.onrace.domain.event.dto;

import java.time.LocalDateTime;

import com.kt.onrace.domain.event.entity.EventSalesInfo;

import lombok.Builder;

@Builder
public record EventSalesInfoResponse (
	RefundPolicyInfo refundPolicy,
	DeliveryInfo delivery,
	SellerInfo seller
) {

	@Builder
	public record RefundPolicyInfo(
		Boolean isRefundable,
		Boolean isTransferable,
		LocalDateTime refundStartAt,
		LocalDateTime refundEndAt,
		LocalDateTime nonRefundableAt,
		String cancellationFee,
		String refundPolicy,
		String weatherRefund
	) {
	}

	@Builder
	public record DeliveryInfo(
		String deliveryTarget,
		String deliveryMethod,
		LocalDateTime deliveryStartAt,
		LocalDateTime deliveryEndAt,
		String deliveryFee,
		String deliveryArea,
		LocalDateTime addressChangePeriod,
		String deliveryCompensation
	) {
	}

	@Builder
	public record SellerInfo(
		String sellerName,
		String businessNo,
		String ecommerceNo,
		Boolean isEcommerceMediator,
		String customerService,
		String sellerAddress
	) {
	}

	public static EventSalesInfoResponse from(EventSalesInfo salesInfo) {
		RefundPolicyInfo refundPolicy = RefundPolicyInfo.builder()
			.isRefundable(salesInfo.isRefundable())
			.isTransferable(salesInfo.isTransferable())
			.refundStartAt(salesInfo.getRefundStartAt())
			.refundEndAt(salesInfo.getRefundEndAt())
			.nonRefundableAt(salesInfo.getNonRefundableAt())
			.cancellationFee(salesInfo.getCancellationFee())
			.refundPolicy(salesInfo.getRefundPolicy())
			.weatherRefund(salesInfo.getWeatherRefund())
			.build();

		DeliveryInfo delivery = DeliveryInfo.builder()
			.deliveryTarget(salesInfo.getDeliveryTarget())
			.deliveryMethod(salesInfo.getDeliveryMethod())
			.deliveryStartAt(salesInfo.getDeliveryStartAt())
			.deliveryEndAt(salesInfo.getDeliveryEndAt())
			.deliveryFee(salesInfo.getDeliveryFee())
			.deliveryArea(salesInfo.getDeliveryArea())
			.addressChangePeriod(salesInfo.getAddressChangePeriod())
			.deliveryCompensation(salesInfo.getDeliveryCompensation())
			.build();

		SellerInfo seller = SellerInfo.builder()
			.sellerName(salesInfo.getSellerName())
			.businessNo(salesInfo.getBusinessNo())
			.ecommerceNo(salesInfo.getEcommerceNo())
			.isEcommerceMediator(salesInfo.isEcommerceMediator())
			.customerService(salesInfo.getCustomerService())
			.sellerAddress(salesInfo.getSellerAddress())
			.build();

		return EventSalesInfoResponse.builder()
			.refundPolicy(refundPolicy)
			.delivery(delivery)
			.seller(seller)
			.build();
	}
}
