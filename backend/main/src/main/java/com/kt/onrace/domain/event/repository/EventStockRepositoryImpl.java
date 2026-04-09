package com.kt.onrace.domain.event.repository;

import static com.kt.onrace.domain.event.entity.QEventStock.*;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class EventStockRepositoryImpl implements EventStockRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public int incrementConfirmedStock(Long paceId) {
		return (int)queryFactory
			.update(eventStock)
			.set(eventStock.confirmedStock, eventStock.confirmedStock.add(1))
			.set(eventStock.version, eventStock.version.add(1L))
			.where(eventStock.eventPace.id.eq(paceId))
			.execute();
	}

	@Override
	public int decrementConfirmedStock(Long paceId) {
		return (int)queryFactory
			.update(eventStock)
			.set(eventStock.confirmedStock, eventStock.confirmedStock.subtract(1))
			.set(eventStock.version, eventStock.version.add(1L))
			.where(eventStock.eventPace.id.eq(paceId))
			.execute();
	}
}
