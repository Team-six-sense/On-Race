package com.kt.onrace.domain.event.repository;

import java.util.List;
import java.util.Optional;

import com.kt.onrace.domain.event.dto.EventSearchRequest;
import com.kt.onrace.domain.event.entity.Event;

public interface EventRepositoryCustom {

	List<Event> findVisibleEvents(EventSearchRequest request, int size);

	Optional<Event> findVisibleEventDetail(Long id);
}
