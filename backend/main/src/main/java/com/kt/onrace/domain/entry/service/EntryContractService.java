package com.kt.onrace.domain.entry.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.util.Preconditions;
import com.kt.onrace.domain.entry.entity.Entry;
import com.kt.onrace.domain.entry.entity.EntryStatus;
import com.kt.onrace.domain.entry.repository.EntryRepository;
import com.kt.onrace.domain.event.entity.Event;
import com.kt.onrace.domain.event.entity.EventAppType;
import com.kt.onrace.domain.event.repository.EventRepository;
import com.kt.onrace.domain.event.repository.EventStockRepository;
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
	private final EventStockRepository eventStockRepository;
	private final EntryService entryService;
	private final EntryMetrics entryMetrics;

	@Override
	public OrderCheckoutEligibility resolveCheckoutEligibility(Long userId, Long eventId, Long paceId) {
		Event event = eventRepository.findByIdAndIsViewTrueAndIsDeletedFalseOrThrow(eventId, BusinessErrorCode.EVENT_NOT_FOUND);
		Entry entry = entryRepository.findByUserIdAndEventPaceIdOrThrow(userId, paceId, BusinessErrorCode.ENTRY_NOT_FOUND);

		boolean canCheckout = event.getAppType() == EventAppType.LOTTERY ?
			entry.getStatus() == EntryStatus.WON : entry.isReserved() && eventStockService.hasReservation(paceId, userId);

		return OrderCheckoutEligibility.builder()
			.entryId(entry.getId())
			.canCheckout(canCheckout)
			.failureCode(canCheckout ? null : BusinessErrorCode.ENTRY_CANNOT_CHECKOUT.getCode())
			.build();
	}

	@Override
	@Transactional
	public void handlePaymentConfirmed(Long entryId) {
		Entry entry = entryRepository.findByIdOrThrow(entryId, BusinessErrorCode.ENTRY_NOT_FOUND);
		EventAppType appType = entry.getEvent().getAppType();

		if(appType == EventAppType.FIRST_COME) {
			Preconditions.validate(entry.getStatus() == EntryStatus.RESERVED, BusinessErrorCode.ENTRY_CANNOT_CHECKOUT);
		} else {
			Preconditions.validate(entry.getStatus() == EntryStatus.WON, BusinessErrorCode.ENTRY_CANNOT_CHECKOUT);
		}

		entryService.confirmReservation(entry.getUserId(), entry.getEventPace().getId(), appType);
	}

	@Override
	@Transactional
	public void rollbackPendingPayment(Long entryId) {
		Entry entry = entryRepository.findByIdOrThrow(entryId, BusinessErrorCode.ENTRY_NOT_FOUND);
		EventAppType appType = entry.getEvent().getAppType();

		boolean shouldRollback = (appType == EventAppType.FIRST_COME && entry.getStatus() == EntryStatus.APPLIED)
			|| (appType == EventAppType.LOTTERY && entry.getStatus() == EntryStatus.WON);

		if (!shouldRollback) return;

		entryMetrics.recordRollback(appType.name());

		Long paceId = entry.getEventPace().getId();
		eventStockRepository.decrementConfirmedStock(paceId);

		if (appType == EventAppType.FIRST_COME) {
			eventStockService.cancelTempStock(paceId);
			eventStockService.cancelConfirmedStock(paceId);
		}
	}
}
