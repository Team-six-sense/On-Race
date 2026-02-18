package com.kt.onrace.domain.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.onrace.domain.event.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long>, EventRepositoryCustom {
}
