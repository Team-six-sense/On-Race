package com.kt.onrace.domain.event.repository;

import static com.kt.onrace.domain.event.entity.QEventCourse.*;
import static com.kt.onrace.domain.event.entity.QEventPace.*;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.kt.onrace.domain.event.entity.EventPace;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class EventPaceRepositoryImpl implements EventPaceRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Optional<EventPace> findWithCourse(Long paceId, Long courseId, Long eventId) {
		EventPace result = queryFactory
			.selectFrom(eventPace)
			.join(eventPace.eventCourse, eventCourse).fetchJoin()
			.where(
				eventPace.id.eq(paceId),
				eventCourse.id.eq(courseId),
				eventCourse.event.id.eq(eventId)
			)
			.fetchOne();

		return Optional.ofNullable(result);
	}
}
