package com.kt.onrace.domain.event.repository;

import static com.kt.onrace.domain.event.entity.QEvent.*;
import static com.kt.onrace.domain.event.entity.QEventCourse.*;
import static com.kt.onrace.domain.event.entity.QEventPackage.*;
import static com.kt.onrace.domain.event.entity.QEventPace.*;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.kt.onrace.domain.event.dto.EventSearchRequest;
import com.kt.onrace.domain.event.entity.Event;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class EventRepositoryImpl implements EventRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<Event> findVisibleEvents(EventSearchRequest request, int size) {
		BooleanBuilder builder = new BooleanBuilder();

		builder.and(event.isView.isTrue());
		builder.and(event.isDeleted.isFalse());

		if (request.status() != null) {
			builder.and(event.status.eq(request.status()));
		}

		if (request.type() != null) {
			builder.and(event.type.eq(request.type()));
		}

		if (request.cursor().cursorId() != null) {
			builder.and(event.id.lt(request.cursor().cursorId()));
		}

		if (request.minDistance() != null && request.maxDistance() != null) {
			builder.and(event.id.in(
				JPAExpressions
					.select(eventCourse.event.id)
					.from(eventCourse)
					.where(eventCourse.distanceM.between(request.minDistance(), request.maxDistance()))
			));
		}

		if (request.eventStartDate() != null && request.eventEndDate() != null) {
			builder.and(event.eventAt.between(
				request.eventStartDate().atStartOfDay(),
				request.eventEndDate().atTime(23, 59, 59)
			));
		}

		if (request.venueAddress() != null && !request.venueAddress().isBlank()) {
			builder.and(event.venueAddress.contains(request.venueAddress()));
		}

		List<Long> eventIds = queryFactory
			.select(event.id)
			.from(event)
			.where(builder)
			.orderBy(event.id.desc())
			.limit(size + 1)
			.fetch();

		if (eventIds.isEmpty()) {
			return List.of();
		}

		return queryFactory
			.selectDistinct(event)
			.from(event)
			.leftJoin(event.courses, eventCourse).fetchJoin()
			.where(event.id.in(eventIds))
			.orderBy(event.id.desc())
			.fetch();
	}

	@Override
	public Optional<Event> findVisibleEventDetail(Long id) {
		BooleanBuilder builder = new BooleanBuilder();

		builder.and(event.isView.isTrue());
		builder.and(event.isDeleted.isFalse());

		Event foundEvent = queryFactory
			.selectDistinct(event)
			.from(event)
			.leftJoin(event.courses, eventCourse).fetchJoin()
			.where(event.id.eq(id).and(builder))
			.fetchOne();

		if (foundEvent == null) {
			return Optional.empty();
		}

		if (!foundEvent.getCourses().isEmpty()) {
			queryFactory
				.selectFrom(eventCourse)
				.leftJoin(eventCourse.eventPaces, eventPace).fetchJoin()
				.where(eventCourse.event.id.eq(foundEvent.getId()))
				.fetch();
		}

		queryFactory
			.selectFrom(event)
			.leftJoin(event.packages, eventPackage).fetchJoin()
			.where(event.id.eq(foundEvent.getId()))
			.fetchOne();

		return Optional.of(foundEvent);
	}
}
