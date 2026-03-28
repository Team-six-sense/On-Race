package com.kt.onrace.domain.order.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;

class OrderTest {

	@Test
	@DisplayName("pending 주문 생성 팩토리는 초기 상태를 PENDING으로 고정한다")
	void createPendingInitializesPendingStatus() {
		Order order = Order.createPending(
			"ORD-ENTITY-001",
			7L,
			10L,
			20L,
			1000L,
			60000L,
			3000L,
			0L,
			63000L,
			"홍길동",
			"집",
			"010-1111-2222",
			"04100",
			"서울시 마포구",
			"301호",
			"문앞"
		);

		assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
	}

	@Test
	@DisplayName("markPaid는 pending 주문만 paid로 전이한다")
	void markPaidTransitionsPendingOrder() {
		Order order = createOrder(OrderStatus.PENDING);

		order.markPaid();

		assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAID);
	}

	@Test
	@DisplayName("markCancelled는 pending 주문만 cancelled로 전이한다")
	void markCancelledTransitionsPendingOrder() {
		Order order = createOrder(OrderStatus.PENDING);

		order.markCancelled();

		assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
	}

	@Test
	@DisplayName("markExpired는 pending 주문만 expired로 전이한다")
	void markExpiredTransitionsPendingOrder() {
		Order order = createOrder(OrderStatus.PENDING);

		order.markExpired();

		assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.EXPIRED);
	}

	@Test
	@DisplayName("markFailed는 pending 주문만 failed로 전이한다")
	void markFailedTransitionsPendingOrder() {
		Order order = createOrder(OrderStatus.PENDING);

		order.markFailed();

		assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.FAILED);
	}

	@Test
	@DisplayName("각 전이 메서드는 이미 같은 상태면 멱등하게 유지된다")
	void transitionMethodsAreIdempotentWhenAlreadyInTargetStatus() {
		Order paidOrder = createOrder(OrderStatus.PAID);
		Order cancelledOrder = createOrder(OrderStatus.CANCELLED);
		Order expiredOrder = createOrder(OrderStatus.EXPIRED);
		Order failedOrder = createOrder(OrderStatus.FAILED);

		paidOrder.markPaid();
		cancelledOrder.markCancelled();
		expiredOrder.markExpired();
		failedOrder.markFailed();

		assertThat(paidOrder.getOrderStatus()).isEqualTo(OrderStatus.PAID);
		assertThat(cancelledOrder.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
		assertThat(expiredOrder.getOrderStatus()).isEqualTo(OrderStatus.EXPIRED);
		assertThat(failedOrder.getOrderStatus()).isEqualTo(OrderStatus.FAILED);
	}

	@Test
	@DisplayName("각 전이 메서드는 pending이 아닌 다른 상태에서는 예외를 던진다")
	void transitionMethodsRejectInvalidSourceStatus() {
		Order paidOrder = createOrder(OrderStatus.PAID);
		Order cancelledOrder = createOrder(OrderStatus.CANCELLED);
		Order expiredOrder = createOrder(OrderStatus.EXPIRED);
		Order failedOrder = createOrder(OrderStatus.FAILED);

		assertThatThrownBy(cancelledOrder::markPaid)
			.isInstanceOf(BusinessException.class)
			.extracting(exception -> ((BusinessException)exception).getErrorCode())
			.isEqualTo(BusinessErrorCode.ORDER_CANNOT_CONFIRM);

		assertThatThrownBy(paidOrder::markCancelled)
			.isInstanceOf(BusinessException.class)
			.extracting(exception -> ((BusinessException)exception).getErrorCode())
			.isEqualTo(BusinessErrorCode.ORDER_CANNOT_CONFIRM);

		assertThatThrownBy(cancelledOrder::markExpired)
			.isInstanceOf(BusinessException.class)
			.extracting(exception -> ((BusinessException)exception).getErrorCode())
			.isEqualTo(BusinessErrorCode.ORDER_CANNOT_CONFIRM);

		assertThatThrownBy(expiredOrder::markFailed)
			.isInstanceOf(BusinessException.class)
			.extracting(exception -> ((BusinessException)exception).getErrorCode())
			.isEqualTo(BusinessErrorCode.ORDER_CANNOT_CONFIRM);
	}

	private Order createOrder(OrderStatus orderStatus) {
		return Order.builder()
			.orderNumber("ORD-ENTITY-001-" + orderStatus.name())
			.userId(7L)
			.eventCourseId(10L)
			.eventPaceId(20L)
			.entryId(1000L)
			.orderStatus(orderStatus)
			.itemTotalAmount(60000L)
			.shippingFee(3000L)
			.discountAmount(0L)
			.finalAmount(63000L)
			.recipientName("홍길동")
			.addressLabel("집")
			.recipientPhone("010-1111-2222")
			.zipCode("04100")
			.address("서울시 마포구")
			.detailAddress("301호")
			.deliveryMemo("문앞")
			.build();
	}
}
