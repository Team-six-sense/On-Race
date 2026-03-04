package com.kt.onrace.domain.entry.repository;

import static com.kt.onrace.domain.entry.entity.QEntry.*;

import org.springframework.stereotype.Repository;

import com.kt.onrace.domain.entry.dto.EntryCountResult;
import com.kt.onrace.domain.entry.entity.EntryStatus;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class EntryRepositoryImpl implements EntryRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public EntryCountResult countTotalAndAppliedByPaceId(Long paceId) {
		Tuple result = queryFactory
			.select(
				entry.count(),
				new CaseBuilder()
					.when(entry.status.eq(EntryStatus.APPLIED)).then(1L)
					.otherwise(0L)
					.sum()
			)
			.from(entry)
			.where(
				entry.eventPace.id.eq(paceId),
				entry.status.in(EntryStatus.PRE_SAVED, EntryStatus.APPLIED)
			)
			.fetchOne();

		if (result == null) {
			return new EntryCountResult(0, 0);
		}

		long totalCount = result.get(0, Long.class);
		Long appliedSum = result.get(1, Long.class);

		return new EntryCountResult(totalCount, appliedSum != null ? appliedSum : 0L);
	}
}
