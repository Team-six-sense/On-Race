package com.kt.onrace.domain.event.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.onrace.domain.event.entity.EventPackage;

public interface EventPackageRepository extends JpaRepository<EventPackage, Long> {

	List<EventPackage> findByEventId(Long eventId);

}
