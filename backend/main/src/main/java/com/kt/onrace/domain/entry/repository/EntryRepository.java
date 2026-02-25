package com.kt.onrace.domain.entry.repository;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.common.exception.ErrorCode;
import com.kt.onrace.domain.entry.entity.Entry;
import com.kt.onrace.domain.entry.entity.EntryStatus;

public interface EntryRepository extends JpaRepository<Entry, Long> {

	default Entry findByUserIdAndEventIdOrThrow(Long userId, Long eventId, ErrorCode errorCode) {
		return findByUserIdAndEventId(userId, eventId).orElseThrow(() -> new BusinessException(errorCode));
	}

	Optional<Entry> findByUserIdAndEventId(Long userId, Long eventId);

	void deleteByUserIdAndEventId(Long userId, Long eventId);

	long countByEventPaceIdAndStatusIn(Long paceId, Collection<EntryStatus> statuses);
}
