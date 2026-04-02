package com.kt.onrace.domain.mypage.service;

import static com.kt.onrace.domain.order.entity.QOrder.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.common.logging.annotation.ServiceLog;
import com.kt.onrace.domain.mypage.dto.MyPageOrderDetailResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageOrderItemDto;
import com.kt.onrace.domain.mypage.dto.MyPageOrderListResponseDto;
import com.kt.onrace.domain.mypage.dto.MyPageOrderTab;
import com.kt.onrace.domain.mypage.dto.MyPagePaymentHistoryItemDto;
import com.kt.onrace.domain.mypage.dto.MyPageStatusDto;
import com.kt.onrace.domain.order.entity.Order;
import com.kt.onrace.domain.order.entity.OrderStatus;
import com.kt.onrace.domain.order.repository.OrderPackageRepository;
import com.kt.onrace.domain.order.repository.OrderRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

/**
 * 사용자의 주문 목록과 주문 상세 정보를 마이페이지 응답으로 변환하는 서비스이다.
 * 탭별 상태 필터링, 연관 이벤트 정보 조회, 취소·환불 가능 여부 계산을 함께 처리한다.
 */
@ServiceLog
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderHistoryService {

	private final OrderRepository orderRepository;
	private final OrderPackageRepository orderPackageRepository;
	private final JPAQueryFactory queryFactory;
	private final MyPageDisplayStatusResolver displayStatusResolver;

	public List<MyPagePaymentHistoryItemDto> getPaymentHistories(Long userId, MyPageOrderTab tab) {
		List<Order> orders = queryFactory.selectFrom(order)
				.where(order.userId.eq(userId), statusCondition(tab))
				.orderBy(order.createdAt.desc())
				.fetch();

		return orders.stream()
				.map(this::toPaymentHistoryItem)
				.toList();
	}

	public MyPageOrderListResponseDto getOrders(Long userId, MyPageOrderTab tab, int page, int size) {
		MyPagePagingPolicy.validate(page, size);

		long totalCount = countOrders(userId, tab);
		if (totalCount == 0) {
			return MyPageOrderListResponseDto.empty(page, size);
		}

		List<Order> orders = queryFactory.selectFrom(order)
				.where(order.userId.eq(userId), statusCondition(tab))
				.orderBy(order.createdAt.desc())
				.offset(MyPagePagingPolicy.offset(page, size))
				.limit(size)
				.fetch();

		List<MyPageOrderItemDto> items = orders.stream()
				.map(this::toOrderItem)
				.toList();

		return MyPageOrderListResponseDto.of(page, size, totalCount, items);
	}

	public MyPageOrderDetailResponseDto getOrderDetail(Long userId, String orderNumber) {
		Order currentOrder = orderRepository.findByOrderNumberAndUserId(orderNumber, userId)
				.orElseThrow(() -> new BusinessException(BusinessErrorCode.ORDER_NOT_FOUND));

		MyPageStatusDto status = displayStatusResolver.resolveOrderStatus(currentOrder);

		List<MyPageOrderDetailResponseDto.PackageInfo> packages = orderPackageRepository.findByOrderIdOrderByIdAsc(
				currentOrder.getId())
				.stream()
				.map(orderPackage -> MyPageOrderDetailResponseDto.PackageInfo.of(
						orderPackage.getEventPackageId(),
						orderPackage.getName(),
						orderPackage.getPrice()))
				.toList();

		return MyPageOrderDetailResponseDto.of(
				currentOrder.getEventId(),
				currentOrder.getOrderNumber(),
				status.statusText(),
				status.actionType(),
				status.actionLabel(),
				status.actionEnabled(),
				currentOrder.getCreatedAt(),
				resolvePaymentDeadlineAt(currentOrder),
				currentOrder.getEventTitle(),
				null,
				currentOrder.getCourseName(),
				currentOrder.getPaceName(),
				currentOrder.getItemTotalAmount(),
				currentOrder.getShippingFee(),
				currentOrder.getDiscountAmount(),
				currentOrder.getFinalAmount(),
				currentOrder.getRecipientName(),
				currentOrder.getAddressLabel(),
				currentOrder.getRecipientPhone(),
				currentOrder.getZipCode(),
				currentOrder.getAddress(),
				currentOrder.getDetailAddress(),
				currentOrder.getDeliveryMemo(),
				null,
				false,
				null,
				null,
				displayStatusResolver.canCancel(currentOrder.getOrderStatus()),
				displayStatusResolver.canRefund(currentOrder.getOrderStatus()),
				displayStatusResolver.canExchange(currentOrder.getOrderStatus()),
				packages);
	}

	private long countOrders(Long userId, MyPageOrderTab tab) {
		Long count = queryFactory.select(order.count())
				.from(order)
				.where(order.userId.eq(userId), statusCondition(tab))
				.fetchOne();

		return count == null ? 0 : count;
	}

	private BooleanExpression statusCondition(MyPageOrderTab tab) {
		if (tab == null || tab == MyPageOrderTab.ALL) {
			return null;
		}

		return switch (tab) {
			case ALL -> null;
			case PENDING -> order.orderStatus.eq(OrderStatus.PENDING);
			case COMPLETED -> order.orderStatus.eq(OrderStatus.PAID);
			case CANCELLED -> order.orderStatus.eq(OrderStatus.CANCELLED);
		};
	}

	<<<<<<<HEAD

	private OrderLookup buildOrderLookup(List<Order> orders) {
		Set<Long> courseIds = orders.stream()
			.map(Order::getEventCourseId)
			.filter(Objects::nonNull)
			currentOrder.getOrderNumber(),
			status.statusText(),
			status.actionType(),
			status.actionLabel(),
			status.actionEnabled(),
			null,
			currentOrder.getEventTitle(),
			currentOrder.getCourseName(),
			currentOrder.getPaceName(),
			currentOrder.getFinalAmount(),
			currentOrder.getCreatedAt(),
			resolvePaymentDeadlineAt(currentOrder)
		);
	}

	private MyPagePaymentHistoryItemDto toPaymentHistoryItem(Order currentOrder) {
		return MyPagePaymentHistoryItemDto.of(
				currentOrder.getOrderNumber(),
				currentOrder.getEventId(),
				currentOrder.getEventTitle(),
				currentOrder.getEventAppType(),
				currentOrder.getEventStatus(),
				resolveOrderStatusLabel(currentOrder.getOrderStatus()),
				currentOrder.getCreatedAt(),
				currentOrder.getEventAt(),
				currentOrder.getEventVenue(),
				currentOrder.getCourseName(),
				currentOrder.getPaceName(),
				currentOrder.getFinalAmount());
	}

	private String resolveOrderStatusLabel(OrderStatus orderStatus) {
		return switch (orderStatus) {
			case PENDING -> "입금대기";
			case PAID -> "결제완료";
			case CANCELLED, EXPIRED, FAILED -> "결제취소";
		};
	}

	private LocalDateTime resolvePaymentDeadlineAt(Order currentOrder) {
		// MVP에서는 실제 결제 마감 시각 원천이 아직 없어 null을 유지한다.
		if (currentOrder.getOrderStatus() != OrderStatus.PENDING) {
			return null;
		}

		return null;
	}
}
