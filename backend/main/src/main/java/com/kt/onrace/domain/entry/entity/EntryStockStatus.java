package com.kt.onrace.domain.entry.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EntryStockStatus {
	AVAILABLE("재고있음"),
	TEMP_SOLD_OUT("일시품절"),
	SOLD_OUT("품절");

	private final String description;
}
