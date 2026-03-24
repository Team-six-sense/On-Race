package com.kt.onrace.domain.event.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.onrace.domain.event.entity.EventImage;
import com.kt.onrace.domain.event.entity.EventImageType;

public interface EventImageRepository extends JpaRepository<EventImage, Long> {

	List<EventImage> findByEvent_IdInAndTypeOrderBySortAsc(Collection<Long> eventIds, EventImageType type);
}
