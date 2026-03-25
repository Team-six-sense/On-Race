package com.kt.onrace.domain.event.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventItemType {
	BASIC("기본품목"),
	ADDITIONAL("추가품목");

	private final String description;
}
