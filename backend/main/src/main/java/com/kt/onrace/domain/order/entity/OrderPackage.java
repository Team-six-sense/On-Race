package com.kt.onrace.domain.order.entity;

import com.kt.onrace.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "order_package")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderPackage extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@Column(nullable = false)
	private Long eventItemId;

	@Column(nullable = false)
	private Long price;

	@Column(nullable = false)
	private String name;

	@Builder
	public OrderPackage(Long eventItemId, Long price, String name) {
		this.eventItemId = eventItemId;
		this.price = price;
		this.name = name;
	}

	void setOrder(Order order) {
		this.order = order;
	}
}
