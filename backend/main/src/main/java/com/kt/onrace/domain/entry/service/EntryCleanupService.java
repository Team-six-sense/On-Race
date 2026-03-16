package com.kt.onrace.domain.entry.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.common.logging.annotation.ServiceLog;
import com.kt.onrace.domain.entry.repository.EntryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * TTL 만료 시 RESERVED 상태의 Entry를 정리하는 서비스.
 * ReservationExpirationListener에서 호출되며,
 * 별도 서비스로 분리한 이유: 리스너는 Redisson 콜백 스레드에서 실행되어
 * 자체 @Transactional이 동작하지 않으므로 프록시를 통한 호출이 필요하다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EntryCleanupService {

	private final EntryRepository entryRepository;

	/**
	 * 만료된 예약의 Entry를 정리한다.
	 * RESERVED 상태면 삭제, APPLIED 상태면 무시 (결제 완료된 건)
	 */
	@ServiceLog
	@Transactional
	public void cleanupExpiredEntry(Long userId, Long paceId) {
		entryRepository.findByUserIdAndEventPaceId(userId, paceId)
			.ifPresent(entry -> {
				if (entry.isReserved()) {
					entryRepository.delete(entry);
					log.info("만료된 예약 Entry 삭제 완료 - userId: {}, paceId: {}", userId, paceId);
				}
			});
	}
}
