package com.kt.onrace.domain.event.entity;

import com.kt.onrace.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "event_package")
public class EventPackage extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "event_id", nullable = false)
	private Event event;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "item_id", nullable = false)
	private EventItem item;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EventItemType itemType;

	@Builder
	public EventPackage(Event event, EventItem item, EventItemType itemType) {
		this.event = event;
		this.item = item;
		this.itemType = itemType;
	}

	/*
	* 추후 주문/결제 진행하시는 분 제거 부탁드립니다 -> 패키지/아이템 구조 변경으로 인한 필드 제거로 인해 컴파일안되서 되어있는 코드입니다
	* */
	public String getName() {
		return item.getName();
	}

	public long getPrice() {
		return item.getPrice();
	}

	public String getDescription() {
		return item.getDescription();
	}
}
