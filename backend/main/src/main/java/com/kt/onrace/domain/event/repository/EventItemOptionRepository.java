package com.kt.onrace.domain.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kt.onrace.domain.event.entity.EventItemOption;

@Repository
public interface EventItemOptionRepository extends JpaRepository<EventItemOption, Long> {
}
