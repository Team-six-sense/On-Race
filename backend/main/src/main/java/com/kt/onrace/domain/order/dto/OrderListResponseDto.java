package com.kt.onrace.domain.order.dto;

import java.util.List;

public record OrderListResponseDto(
	List<OrderSummaryDto> orders
) {
}
