package com.kt.onrace.domain.entry.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.common.logging.annotation.ServiceLog;
import com.kt.onrace.domain.entry.entity.Entry;
import com.kt.onrace.domain.entry.repository.EntryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EntryCleanupService {

	private final EntryRepository entryRepository;

	@ServiceLog
	public boolean cleanupExpiredEntry(Long userId, Long paceId) {
		return entryRepository.findByUserIdAndEventPaceId(userId, paceId)
			.filter(Entry::isReserved)
			.isPresent();
	}
}
