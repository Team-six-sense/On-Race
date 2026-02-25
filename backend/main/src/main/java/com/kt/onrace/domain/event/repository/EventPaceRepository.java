package com.kt.onrace.domain.event.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.common.exception.ErrorCode;
import com.kt.onrace.domain.event.entity.EventPace;

public interface EventPaceRepository extends JpaRepository<EventPace, Long> {

	default EventPace findByIdAndEventCourseIdOrThrow(Long id, Long eventId, ErrorCode errorCode) {
		return findByIdAndEventCourseId(id, eventId).orElseThrow(() -> new BusinessException(errorCode));
	}

	Optional<EventPace> findByIdAndEventCourseId(Long eventId, Long userId);
}
