package com.kt.onrace.domain.event.entity;

import java.util.ArrayList;
import java.util.List;

import com.kt.onrace.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "event_item")
public class EventItem extends BaseEntity {

	@Column(nullable = false, length = 50)
	private String name;

	@Column(nullable = false)
	private long price;

	@Column(length = 500)
	private String description;

	@OneToMany(mappedBy = "item")
	private List<EventItemOption> sizes = new ArrayList<>();

	@Builder
	private EventItem(String name, long price, String description) {
		this.name = name;
		this.price = price;
		this.description = description;
	}
}
