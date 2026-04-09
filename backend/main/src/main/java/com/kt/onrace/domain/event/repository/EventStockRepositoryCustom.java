package com.kt.onrace.domain.event.repository;

public interface EventStockRepositoryCustom {

	int incrementConfirmedStock(Long paceId);
	int decrementConfirmedStock(Long paceId);
}
