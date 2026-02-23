package com.kt.onrace.common.util;

public class ObjectUtils {

	public static <T> T getOrDefault(T value, T defaultValue) {
		if (value == null)
			return defaultValue;
		if (value instanceof String str && str.isBlank())
			return defaultValue;
		return value;
	}
}
