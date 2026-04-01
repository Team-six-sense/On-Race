package com.kt.onrace.domain.entry.dto;

import com.kt.onrace.domain.entry.entity.EntryStockStatus;

import lombok.Builder;

@Builder
public record EntryStockCheckResponse(
	EntryStockStatus stockStatus,
	long remainingStock
) {

	public static EntryStockCheckResponse available(long remainingStock) {
		return EntryStockCheckResponse.builder()
			.stockStatus(EntryStockStatus.AVAILABLE)
			.remainingStock(remainingStock)
			.build();
	}

	public static EntryStockCheckResponse tempSoldOut() {
		return EntryStockCheckResponse.builder()
			.stockStatus(EntryStockStatus.TEMP_SOLD_OUT)
			.remainingStock(0)
			.build();
	}

	public static EntryStockCheckResponse soldOut() {
		return EntryStockCheckResponse.builder()
			.stockStatus(EntryStockStatus.SOLD_OUT)
			.remainingStock(0)
			.build();
	}

}
