package com.kt.onrace.auth.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Gender {
	MALE("남자"),
	FEMALE("여자");

	private final String description;
}
