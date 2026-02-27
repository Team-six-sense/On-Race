package com.kt.onrace.domain.order.dto;

import java.util.List;

public record CheckoutRequestDto(
        String prepareToken,
        Long eventId,
        Long eventCourseId,
        Long eventPaceId,
        List<Long> selectedPackageIds,
        Long expectedFinalAmount,
        String recipientName,
        String recipientPhone,
        String zipCode,
        String address,
        String detailAddress,
        String deliveryMemo) {
}
