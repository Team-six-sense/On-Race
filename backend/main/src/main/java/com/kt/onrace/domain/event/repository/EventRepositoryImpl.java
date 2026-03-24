package com.kt.onrace.domain.event.repository;

import static com.kt.onrace.domain.event.entity.QEvent.*;
import static com.kt.onrace.domain.event.entity.QEventCourse.*;
import static com.kt.onrace.domain.event.entity.QEventImage.*;
import static com.kt.onrace.domain.event.entity.QEventPace.*;
import static com.kt.onrace.domain.event.entity.QEventPackage.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.kt.onrace.domain.event.dto.EventCursorData;
import com.kt.onrace.domain.event.dto.EventSearchRequest;
import com.kt.onrace.domain.event.entity.Event;
import com.kt.onrace.domain.event.entity.EventAppType;
import com.kt.onrace.domain.event.entity.EventStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class EventRepositoryImpl implements EventRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<Event> findVisibleEvents(EventSearchRequest request, int size) {
		LocalDateTime now = LocalDateTime.now();

		BooleanBuilder builder = new BooleanBuilder();

		builder.and(event.isView.isTrue());
		builder.and(event.isDeleted.isFalse());
		builder.and(event.eventAt.goe(LocalDate.now().atStartOfDay()));

		if (request.type() != null) {
			builder.and(event.type.eq(request.type()));
		}

		if (request.appType() != null) {
			builder.and(event.appType.eq(request.appType()));
		}

		if (request.status() != null) {
			builder.and(statusFilterCondition(request.status(), now));
		}

		EventCursorData cursorData = request.decodeCursor();
		if (cursorData != null) {
			builder.and(keysetCondition(cursorData, now));
		}

		if (request.minDistance() != null || request.maxDistance() != null) {
			BooleanBuilder distanceCondition = new BooleanBuilder();

			if (request.minDistance() != null) {
				distanceCondition.and(eventCourse.distanceMeter.goe(request.minDistance()));
			}
			if (request.maxDistance() != null) {
				distanceCondition.and(eventCourse.distanceMeter.loe(request.maxDistance()));
			}

			builder.and(event.id.in(
				JPAExpressions.select(eventCourse.event.id)
					.from(eventCourse)
					.where(distanceCondition)
			));
		}

		if (request.eventStartDate() != null && request.eventEndDate() != null) {
			builder.and(event.eventAt.between(
				request.eventStartDate().atStartOfDay(),
				request.eventEndDate().atTime(23, 59, 59)
			));
		}

		if (request.region() != null) {
			builder.and(event.region.eq(request.region()));
		}

		if (request.keyword() != null && !request.keyword().isBlank()) {
			builder.and(event.venue.contains(request.keyword()).or(event.title.contains(request.keyword())));
		}

		OrderSpecifier<?>[] orderSpecifiers = multiSortOrder(now);

		List<Long> eventIds = queryFactory
			.select(event.id)
			.from(event)
			.where(builder)
			.orderBy(orderSpecifiers)
			.limit(size + 1)
			.fetch();

		if (eventIds.isEmpty()) {
			return List.of();
		}

		List<Event> events = queryFactory
			.selectDistinct(event)
			.from(event)
			.leftJoin(event.courses, eventCourse).fetchJoin()
			.where(event.id.in(eventIds))
			.orderBy(orderSpecifiers)
			.fetch();

		// 코스와 이미지 모두 N인 관계로 카테시안 곱 발생 MultipleBagFetchException -> 쿼리 분리
		// 2차 쿼리 결과를 자동으로 기존 Event 엔티티에 매핑해줌
		if (!events.isEmpty()) {
			queryFactory
				.selectFrom(event)
				.leftJoin(event.images, eventImage).fetchJoin()
				.where(event.id.in(eventIds))
				.fetch();
		}

		return events;
	}

	private OrderSpecifier<?>[] multiSortOrder(LocalDateTime now) {
		return new OrderSpecifier<?>[] {
			statusOrderExpression(now).asc(),
			event.eventAt.asc(),
			event.title.asc(),
			event.id.desc()
		};
	}

	private NumberExpression<Integer> statusOrderExpression(LocalDateTime now) {
		BooleanExpression inAppPeriod = event.appStartAt.loe(now)
			.and(event.appEndAt.goe(now));

		BooleanExpression notSoldOut = event.appType.ne(EventAppType.FIRST_COME)
			.or(event.soldOut.isFalse());

		BooleanExpression isClosingSoon = inAppPeriod
			.and(event.appEndAt.loe(now.plusDays(2)))
			.and(notSoldOut);

		BooleanExpression isInProgress = inAppPeriod
			.and(event.appEndAt.after(now.plusDays(2)))
			.and(notSoldOut);

		BooleanExpression isSoldOut = inAppPeriod
			.and(event.appType.eq(EventAppType.FIRST_COME))
			.and(event.soldOut.isTrue());

		BooleanExpression isDrawCompleted = event.appEndAt.before(now)
			.and(event.appType.eq(EventAppType.LOTTERY))
			.and(event.lotteryAnnouncedAt.isNotNull())
			.and(event.lotteryAnnouncedAt.loe(now));

		BooleanExpression isEnd = event.appEndAt.before(now);

		// CaseBuilder는 first-match이므로 순서가 중요
		return new CaseBuilder()
			.when(isClosingSoon).then(EventStatus.CLOSING_SOON.getSortOrder())
			.when(isInProgress).then(EventStatus.IN_PROGRESS.getSortOrder())
			.when(isSoldOut).then(EventStatus.END.getSortOrder())
			.when(isDrawCompleted).then(EventStatus.DRAW_COMPLETED.getSortOrder())
			.when(isEnd).then(EventStatus.END.getSortOrder())
			.otherwise(EventStatus.READY.getSortOrder());
	}

	private BooleanExpression keysetCondition(EventCursorData cursor, LocalDateTime now) {
		NumberExpression<Integer> statusOrder = statusOrderExpression(now);
		BooleanExpression statusEq = statusOrder.eq(cursor.statusPriority());
		BooleanExpression eventAtEq = event.eventAt.eq(cursor.eventAt());
		BooleanExpression titleEq = event.title.eq(cursor.title());

		return statusOrder.gt(cursor.statusPriority())
			.or(
				statusEq.and(event.eventAt.after(cursor.eventAt()))
			)
			.or(
				statusEq.and(eventAtEq).and(event.title.gt(cursor.title()))
			)
			.or(
				statusEq.and(eventAtEq).and(titleEq).and(event.id.lt(cursor.id()))
			);
	}

	private BooleanExpression statusFilterCondition(EventStatus status, LocalDateTime now) {
		BooleanExpression notSoldOut = event.appType.ne(EventAppType.FIRST_COME)
			.or(event.soldOut.isFalse());

		return switch (status) {
			case READY -> event.appStartAt.after(now);

			case IN_PROGRESS -> event.appStartAt.loe(now)
				.and(event.appEndAt.after(now.plusDays(2)))
				.and(notSoldOut);

			case CLOSING_SOON -> event.appStartAt.loe(now)
				.and(event.appEndAt.goe(now))
				.and(event.appEndAt.loe(now.plusDays(2)))
				.and(notSoldOut);

			case END -> event.appEndAt.before(now)
				.and(
					event.appType.ne(EventAppType.LOTTERY)
						.or(event.lotteryAnnouncedAt.isNull())
						.or(event.lotteryAnnouncedAt.after(now))
				)
				.or(
					event.appStartAt.loe(now)
						.and(event.appEndAt.goe(now))
						.and(event.appType.eq(EventAppType.FIRST_COME))
						.and(event.soldOut.isTrue())
				);

			case DRAW_COMPLETED -> event.appEndAt.before(now)
				.and(event.appType.eq(EventAppType.LOTTERY))
				.and(event.lotteryAnnouncedAt.isNotNull())
				.and(event.lotteryAnnouncedAt.loe(now));
		};
	}

	@Override
	public Optional<Event> findVisibleEvent(Long id) {
		return Optional.ofNullable(
			queryFactory
				.selectFrom(event)
				.where(
					event.id.eq(id),
					event.isView.isTrue(),
					event.isDeleted.isFalse()
				)
				.fetchOne()
		);
	}

	@Override
	public Optional<Event> findEventWithCoursesAndPaces(Long id) {
		Event foundEvent = queryFactory
			.selectDistinct(event)
			.from(event)
			.leftJoin(event.courses, eventCourse).fetchJoin()
			.where(event.id.eq(id).and(event.isDeleted.isFalse()))
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

		return Optional.of(foundEvent);
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

		// 이미지도 별도 쿼리로 조회 (courses와 동시 fetch join 시 MultipleBagFetchException 발생)
		queryFactory
			.selectFrom(event)
			.leftJoin(event.images, eventImage).fetchJoin()
			.where(event.id.eq(foundEvent.getId()))
			.fetchOne();

		return Optional.of(foundEvent);
	}

	@Override
	public List<Event> findQueueEnabledEvents(long beforeStartMinutes, long afterEndMinutes) {
		LocalDateTime now = LocalDateTime.now();

		BooleanBuilder builder = new BooleanBuilder();
		builder.and(event.isQueue.isTrue());
		builder.and(event.isDeleted.isFalse());
		builder.and(event.appStartAt.loe(now.plusMinutes(beforeStartMinutes)));
		builder.and(event.appEndAt.goe(now.minusMinutes(afterEndMinutes)));

		return queryFactory
			.selectFrom(event)
			.where(builder)
			.fetch();
	}
}
