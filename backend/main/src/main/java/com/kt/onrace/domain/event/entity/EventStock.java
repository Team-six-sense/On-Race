package com.kt.onrace.domain.event.entity;

import com.kt.onrace.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 *  일단 예상 플로우(대기열 진입 시 Redis에서 선점 → 결제 → DB confirmedStock += 1)
 * Redis  → 실시간 선점/반환 (대기열 구간, DB 접근 없음)
 * DB     → 확정 기록만 (결제 완료/환불 시점)
 * 복구   → available = totalStock - confirmedStock (Redis 재기동 시)
 */

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "event_stock")
public class EventStock extends BaseEntity {

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "event_pace_id", nullable = false, unique = true)
	private EventPace eventPace;

	@Column(nullable = false)
	private int totalStock;

	@Column(nullable = false)
	private int confirmedStock;

	@Version
	private Long version;

	@Builder
	private EventStock(EventPace eventPace, Integer totalStock) {
		this.eventPace = eventPace;
		this.totalStock = totalStock != null ? totalStock : 0;
		this.confirmedStock = 0;
	}
}
