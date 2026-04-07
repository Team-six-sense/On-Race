package com.kt.onrace.domain.mypage.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.kt.onrace.domain.mypage.dto.MyPageStatusDto;
import com.kt.onrace.domain.order.entity.Order;
import com.kt.onrace.domain.order.entity.OrderStatus;

class MyPageDisplayStatusResolverTest {

	private final MyPageDisplayStatusResolver resolver = new MyPageDisplayStatusResolver();

	@ParameterizedTest(name = "{0}")
	@MethodSource("cancelledLikeStatuses")
	void resolveOrderStatusUsesCancelledLabelForCancelledLikeStates(
		String description,
		OrderStatus orderStatus
	) {
		Order order = Order.builder()
			.orderNumber("ORD-TEST")
			.userId(7L)
			.eventCourseId(10L)
			.orderStatus(orderStatus)
			.itemTotalAmount(10000L)
			.shippingFee(3000L)
			.discountAmount(0L)
			.finalAmount(13000L)
			.recipientName("홍길동")
			.recipientPhone("010-1111-2222")
			.zipCode("04100")
			.address("서울시 마포구")
			.build();

		MyPageStatusDto result = resolver.resolveOrderStatus(order);

		assertThat(result.statusText()).isEqualTo("결제취소");
		assertThat(result.actionType()).isEqualTo("DETAIL");
		assertThat(result.actionLabel()).isEqualTo("주문 상세보기");
		assertThat(result.actionEnabled()).isTrue();
	}

	private static Stream<Arguments> cancelledLikeStatuses() {
		return Stream.of(
			Arguments.of("취소 주문은 결제취소를 반환한다", OrderStatus.CANCELLED),
			Arguments.of("만료 주문은 결제취소를 반환한다", OrderStatus.EXPIRED),
			Arguments.of("실패 주문은 결제취소를 반환한다", OrderStatus.FAILED)
		);
	}
}
