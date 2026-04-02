package com.kt.onrace.domain.event.listener;

import java.util.Set;
import java.util.stream.Collectors;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.kt.onrace.common.util.RedisKeyGenerator;
import com.kt.onrace.domain.event.entity.Event;
import com.kt.onrace.domain.event.repository.EventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueEventPublisher {
	private final EventRepository eventRepository;
	private final RedissonClient redissonClient;

	// 이벤트가 발생한 후 트랜잭션이 커밋된 시점에 실행되는 이벤트 리스너(service - enableQueue)
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onQueueStatusChange(QueueStatusChangedEvent event) {
		Set<Long> enabledIds = eventRepository.findAllByIsQueueTrueAndIsDeletedFalse()
			.stream()
			.map(Event::getId)
			.collect(Collectors.toSet());

		String message = enabledIds.stream()
			.map(String::valueOf)
			.collect(Collectors.joining(","));

		RTopic topic = redissonClient.getTopic(RedisKeyGenerator.queueEnableChange(), StringCodec.INSTANCE);
		topic.publish(message);
	}
}
