package com.kt.onrace.domain.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.common.exception.ErrorCode;
import com.kt.onrace.domain.event.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long>, EventRepositoryCustom {
	default Event findByIdOrThrow(Long id, ErrorCode errorCode) {
		return findById(id).orElseThrow(() -> new BusinessException(errorCode));
	}
}
