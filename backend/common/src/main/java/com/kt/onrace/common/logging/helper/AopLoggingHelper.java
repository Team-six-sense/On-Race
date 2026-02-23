package com.kt.onrace.common.logging.helper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AopLoggingHelper {

	private final ObjectMapper objectMapper;

	public String extractParameters(ProceedingJoinPoint joinPoint, HttpServletRequest request) {

		MethodSignature signature = (MethodSignature)joinPoint.getSignature();
		String[] paramNames = signature.getParameterNames();
		Object[] args = joinPoint.getArgs();

		Set<String> declaredNames = new HashSet<>();
		List<String> allParams = new ArrayList<>();

		if (paramNames != null && paramNames.length > 0) {
			for (int i = 0; i < paramNames.length; i++) {
				String name = paramNames[i];
				Object value = args[i];

				declaredNames.add(name);

				if (!isHttpRelated(value)) {
					allParams.add(name + "=" + formatValue(value));
				}
			}
		}

		if (request != null) {
			Map<String, String[]> paramMap = request.getParameterMap();

			paramMap.forEach((key, values) -> {
				if (!declaredNames.contains(key)) {
					allParams.add(key + "=" + values[0]);
				}
			});
		}

		return allParams.isEmpty() ? "no" : String.join(", ", allParams);
	}

	private String formatValue(Object value) {
		if (value == null) {
			return "null";
		}

		if (isPrimitiveOrWrapper(value) || value instanceof String) {
			return String.valueOf(value);
		}

		try {
			return objectMapper.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			// JSON 변환 실패 시 클래스명@해시코드
			return value.getClass().getSimpleName() + "@" + Integer.toHexString(value.hashCode());
		}
	}

	private boolean isHttpRelated(Object value) {
		return value instanceof ServletRequest || value instanceof ServletResponse;
	}

	private boolean isPrimitiveOrWrapper(Object value) {
		Class<?> clazz = value.getClass();

		return clazz.isPrimitive()
			|| clazz == Boolean.class
			|| clazz == Integer.class
			|| clazz == Long.class
			|| clazz == Double.class
			|| clazz == Float.class
			|| Number.class.isAssignableFrom(clazz);
	}

}
