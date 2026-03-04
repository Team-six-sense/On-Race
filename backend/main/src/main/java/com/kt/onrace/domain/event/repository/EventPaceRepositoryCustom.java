package com.kt.onrace.domain.event.repository;

import java.util.Optional;

import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.common.exception.ErrorCode;
import com.kt.onrace.domain.event.entity.EventPace;

public interface EventPaceRepositoryCustom {

	default EventPace findWithCourseOrThrow(Long paceId, Long courseId, Long eventId, ErrorCode errorCode) {
		return findWithCourse(paceId, courseId, eventId)
			.orElseThrow(() -> new BusinessException(errorCode));
	}

	// 페이스 조회 + 코스 fetch join + event/course 소속 검증을 한 번에 수행
	Optional<EventPace> findWithCourse(Long paceId, Long courseId, Long eventId);
}
