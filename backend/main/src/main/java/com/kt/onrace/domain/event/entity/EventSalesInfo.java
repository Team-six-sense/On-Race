package com.kt.onrace.domain.event.entity;

import java.time.LocalDateTime;

import com.kt.onrace.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "event_sales_info")
public class EventSalesInfo extends BaseEntity {

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "event_id", nullable = false, unique = true)
	private Event event;

	// 취소 및 환불 정책

	// 환불 가능 여부
	@Column(nullable = false)
	private boolean isRefundable;

	// 양도 가능 여부
	@Column(nullable = false)
	private boolean isTransferable;

	// 환불 가능 기간 ~
	private LocalDateTime refundStartAt;

	private LocalDateTime refundEndAt;

	// 환불 불가 시점
	private LocalDateTime nonRefundableAt;

	// 취소 수수료 기준
	private String cancellationFee;

	// 환불/취소 정책
	@Column(columnDefinition = "TEXT")
	private String refundPolicy;

	// 우천/천재지변 시 환불 정책
	@Column(columnDefinition = "TEXT")
	private String weatherRefund;

	// 배송정보
	// 배송 대상
	@Column(length = 100)
	private String deliveryTarget;

	// 배송 방법
	@Column(length = 50)
	private String deliveryMethod;

	// 배송 일정 ~
	private LocalDateTime deliveryStartAt;

	private LocalDateTime deliveryEndAt;

	// 배송비
	@Column(length = 50)
	private String deliveryFee;

	// 배송 가능 지역
	@Column(length = 200)
	private String deliveryArea;

	// 배송 정보
	@Column(length = 200)
	private String deliveryInfo;

	// 배송지 변경 가능 기간
	private LocalDateTime addressChangePeriod;

	// 미 배송 시 보상 기준
	@Column(length = 500)
	private String deliveryCompensation;

	// 판매자 정보
	@Column(length = 100)
	private String sellerName;

	// 사업자 등록번호
	@Column(length = 20)
	private String businessNo;

	// 통신 판매업 신고 번호
	@Column(length = 50)
	private String ecommerceNo;

	// 통신 판매 중개자 여부
	@Column(nullable = false)
	private boolean isEcommerceMediator;

	// 고객센터 번호
	@Column(length = 50)
	private String customerService;

	// 판매자 주소
	@Column(length = 200)
	private String sellerAddress;

	@Builder
	private EventSalesInfo(Event event, boolean isRefundable, boolean isTransferable,
		LocalDateTime refundStartAt, LocalDateTime refundEndAt, LocalDateTime nonRefundableAt,
		String cancellationFee, String refundPolicy, String weatherRefund,
		String deliveryTarget, String deliveryMethod,
		LocalDateTime deliveryStartAt, LocalDateTime deliveryEndAt,
		String deliveryFee, String deliveryArea, String deliveryInfo, LocalDateTime addressChangePeriod,
		String deliveryCompensation, String sellerName, String businessNo, String ecommerceNo,
		boolean isEcommerceMediator, String customerService, String sellerAddress) {
		this.event = event;
		this.isRefundable = isRefundable;
		this.isTransferable = isTransferable;
		this.refundStartAt = refundStartAt;
		this.refundEndAt = refundEndAt;
		this.nonRefundableAt = nonRefundableAt;
		this.cancellationFee = cancellationFee;
		this.refundPolicy = refundPolicy;
		this.weatherRefund = weatherRefund;
		this.deliveryTarget = deliveryTarget;
		this.deliveryMethod = deliveryMethod;
		this.deliveryStartAt = deliveryStartAt;
		this.deliveryEndAt = deliveryEndAt;
		this.deliveryFee = deliveryFee;
		this.deliveryArea = deliveryArea;
		this.deliveryInfo = deliveryInfo;
		this.addressChangePeriod = addressChangePeriod;
		this.deliveryCompensation = deliveryCompensation;
		this.sellerName = sellerName;
		this.businessNo = businessNo;
		this.ecommerceNo = ecommerceNo;
		this.isEcommerceMediator = isEcommerceMediator;
		this.customerService = customerService;
		this.sellerAddress = sellerAddress;
	}
}
