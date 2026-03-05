package com.kt.onrace.domain.event.repository;

import static com.kt.onrace.domain.event.entity.QEvent.*;
import static com.kt.onrace.domain.event.entity.QEventCourse.*;
import static com.kt.onrace.domain.event.entity.QEventImage.*;
import static com.kt.onrace.domain.event.entity.QEventPace.*;
import static com.kt.onrace.domain.event.entity.QEventPackage.*;

import java.time.LocalDate;
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
		builder.and(event.eventAt.goe(LocalDate.now().atStartOfDay()));

		if (request.type() != null) {
			builder.and(event.type.eq(request.type()));
		}

		if (request.appType() != null) {
			builder.and(event.appType.eq(request.appType()));
		}

		if (request.status() != null) {
			builder.and(event.status.eq(request.status()));
		}

		if (request.cursor().cursorId() != null) {
			builder.and(event.id.lt(request.cursor().cursorId()));
		}

		if (request.minDistance() != null || request.maxDistance() != null) {
			BooleanBuilder distanceCondition = new BooleanBuilder();

			if (request.minDistance() != null) {
				distanceCondition.and(eventCourse.distanceM.goe(request.minDistance()));
			}
			if (request.maxDistance() != null) {
				distanceCondition.and(eventCourse.distanceM.loe(request.maxDistance()));
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

		List<Event> events = queryFactory
			.selectDistinct(event)
			.from(event)
			.leftJoin(event.courses, eventCourse).fetchJoin()
			.where(event.id.in(eventIds))
			.orderBy(event.id.desc())
			.fetch();

		// 코스와 이미지 모두 N인 관계로 카테시안 곱 발생 MultipleBagFetchException -> 쿼리 분리
		// 2차 쿼리 결과를 자동으로 기존 Event 엔티티에 매핑해줌
		if (!events.isEmpty()) {
			queryFactory
				.selectFrom(event)
				.leftJoin(event.images, eventImage).fetchJoin()
				.where(event.id.in(eventIds))
				.fetch();
				// 아래 부분 문제 이런식으로 where에 필터 적용 시 inner join 처럼 동작하여 영속성 컨텍스트에서 미초기화 상태로 남음
				// WHERE 절에 THUMBNAIL 필터 → THUMBNAIL 없는 이벤트가 결과에서 제외 → images 컬렉션 미초기화 → DTO 매핑 시 lazy loading N+1
				//.where(event.id.in(eventIds).and(eventImage.type.eq(EventImageType.THUMBNAIL)))
		}

		return events;
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
}
