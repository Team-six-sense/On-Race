package com.kt.onrace.domain.entry.service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.domain.entry.entity.Entry;
import com.kt.onrace.domain.entry.entity.EntryStatus;
import com.kt.onrace.domain.entry.repository.EntryRepository;
import com.kt.onrace.domain.event.entity.Event;
import com.kt.onrace.domain.event.entity.EventAppType;
import com.kt.onrace.domain.event.repository.EventRepository;
import com.kt.onrace.domain.event.service.EventStockService;
import com.kt.onrace.domain.order.contract.OrderCheckoutEligibility;
import com.kt.onrace.domain.order.contract.OrderEntryContract;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EntryContractService implements OrderEntryContract {

	private final EntryRepository entryRepository;
	private final EventRepository eventRepository;
	private final EventStockService eventStockService;
	private final EntryService entryService;

	@Override
	public OrderCheckoutEligibility resolveCheckoutEligibility(Long userId, Long eventId, Long paceId) {
		Event event = eventRepository.findByIdAndIsViewTrueAndIsDeletedFalseOrThrow(eventId,
			BusinessErrorCode.EVENT_NOT_FOUND);

		Entry entry = entryRepository.findByUserIdAndEventIdOrThrow(userId, eventId, BusinessErrorCode.ENTRY_NOT_FOUND);

		EventAppType appType = event.getAppType();

		return switch (appType) {
			case LOTTERY -> resolveLottery(entry, appType);
			case FIRST_COME -> resolveFirstCome(entry, appType, paceId, userId);
		};
	}

	private OrderCheckoutEligibility resolveLottery(Entry entry, EventAppType appType) {
		boolean canCheckout = entry.getStatus() == EntryStatus.WON;
		String failureCode = canCheckout ? null : BusinessErrorCode.ENTRY_CANNOT_APPLY.getCode();

		return OrderCheckoutEligibility.builder()
			.entryId(entry.getId())
			.appType(appType)
			.currentEntryStatus(entry.getStatus())
			.requiredEntryStatus(EntryStatus.WON)
			.reservedUntil(null)
			.requiresReservationValidation(false)
			.canCheckout(canCheckout)
			.failureCode(failureCode)
			.build();
	}

	private OrderCheckoutEligibility resolveFirstCome(Entry entry, EventAppType appType, Long paceId, Long userId) {
		boolean isReserved = entry.isReserved();
		boolean hasReservation = isReserved && eventStockService.hasReservation(paceId, userId);
		boolean canCheckout = isReserved && hasReservation;

		LocalDateTime reservedUntil = null;
		if (canCheckout) {
			long ttlMs = eventStockService.getReservationTtl(paceId, userId);
			if (ttlMs > 0) {
				reservedUntil = LocalDateTime.now().plusNanos(TimeUnit.MILLISECONDS.toNanos(ttlMs));
			}
		}

		String failureCode = canCheckout ? null : BusinessErrorCode.ENTRY_RESERVATION_EXPIRED.getCode();

		return OrderCheckoutEligibility.builder()
			.entryId(entry.getId())
			.appType(appType)
			.currentEntryStatus(entry.getStatus())
			.requiredEntryStatus(EntryStatus.RESERVED)
			.reservedUntil(reservedUntil)
			.requiresReservationValidation(true)
			.canCheckout(canCheckout)
			.failureCode(failureCode)
			.build();
	}

	@Override
	public boolean hasReservation(Long paceId, Long userId) {
		return eventStockService.hasReservation(paceId, userId);
	}

	@Override
	@Transactional
	public void confirmReservation(Long userId, Long paceId) {
		entryService.confirmReservation(userId, paceId);
	}
}
