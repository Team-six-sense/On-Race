package com.kt.onrace.gateway.cache;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kt.onrace.common.util.RedisKeyGenerator;
import com.kt.onrace.gateway.config.GatewayProperties;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 해당 부분은 대기열 이벤트 활성화 여부를 주기적으로 조회하여 캐싱하는 역할로 현재 gateway->redis 직접 조회를 방지하지 위함이지만
 * 오히려 main service에서 gateway->redis 조회보다 gateway->gateway 조회가 더 많아질 수 있어 추후 개선이 필요할 수 있음(생각좀..)
 */
@Slf4j
@Component
public class QueueEventCache {

	private volatile Set<Long> enabledEventIds = Set.of();
	private final WebClient webClient;
	private final GatewayProperties.QueueCache queueCache;
	private final RedissonClient redissonClient;

	public QueueEventCache(GatewayProperties gatewayProperties, RedissonClient redissonClient) {
		this.queueCache = gatewayProperties.getQueueCache();
		this.webClient = WebClient.builder()
			.baseUrl(queueCache.getServiceUri())
			.defaultHeader("X-Gateway-Token", gatewayProperties.getInternal().getSecret())
			.build();
		this.redissonClient = redissonClient;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void init() {
		refresh().doFinally(signal -> subscribeToQueueChanges()).subscribe();
	}

	public Mono<Void> refresh() {
		return webClient.get()
			.uri(queueCache.getEnabledEventsPath())
			.retrieve()
			.bodyToMono(new ParameterizedTypeReference<EnabledEventsResponse>() {})
			.doOnNext(response -> {
				Set<Long> ids = (response.data() != null) ? response.data() : Set.of();
				this.enabledEventIds = ids;
			})
			.doOnError(e -> log.warn("대기열 캐시 갱신 실패 - 기존 캐시 유지: {}", e.getMessage())).then();
	}

	private void subscribeToQueueChanges() {
		RTopic topic = redissonClient.getTopic(RedisKeyGenerator.queueEnableChange(), StringCodec.INSTANCE);
		topic.addListener(String.class, (channel, message) -> {
			Set<Long> ids = parseEventIds(message);
			this.enabledEventIds = ids;
		});
	}

	private Set<Long> parseEventIds(String message) {
		if(message == null || message.isBlank()) {
			return Set.of();
		}

		return Arrays.stream(message.split(","))
			.map(s -> {
				try {
					return Long.parseLong(s);
				} catch (NumberFormatException e) {
					return null;
				}
			})
			.filter(java.util.Objects::nonNull)
			.collect(Collectors.toSet());
	}

	public boolean isQueueEnabled(Long eventId) {
		return enabledEventIds.contains(eventId);
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record EnabledEventsResponse(boolean success, Set<Long> data) {}
}
