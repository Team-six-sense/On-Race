package com.kt.onrace.domain.event.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.common.exception.ErrorCode;
import com.kt.onrace.domain.event.entity.EventCourse;

public interface EventCourseRepository extends JpaRepository<EventCourse, Long> {

	default EventCourse findByIdAndEventIdOrThrow(Long id, Long eventId, ErrorCode errorCode) {
		return findByIdAndEventId(id, eventId).orElseThrow(() -> new BusinessException(errorCode));
	}

	Optional<EventCourse> findByIdAndEventId(Long id, Long eventId);
}
