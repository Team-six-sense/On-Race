package com.kt.onrace.common.logging.aspect;

import java.lang.reflect.Method;
import java.util.Collection;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kt.onrace.common.logging.annotation.ServiceLog;
import com.kt.onrace.common.logging.helper.AopLoggingHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 [AOP-SERVICE]  요청 서비스: MainService.getTestMessage()
 [AOP-SERVICE]  파라미터: [message=123, request={"name":"이름","age":10,"message":"dto테스트"}, number=111]
 [AOP-SERVICE]  성공 응답: ㅂㄷㅂㄷㅂㄷㅂㄷㅂㄷ
 [AOP-SERVICE]  SLOW!: 5003ms (적정 실행 시간: 4000ms)
 [AOP-SERVICE]  실패 응답: 5008ms
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ServiceLogAspect {

	private static final String PREFIX = "[AOP-SERVICE] ";
	private static final long DEFAULT_SLOW_MS = 1000;
	private final AopLoggingHelper loggingHelper;
	private final ObjectMapper objectMapper;

	@Around("@within(serviceLog) || @annotation(serviceLog)")
	Object logging(ProceedingJoinPoint joinPoint, ServiceLog serviceLog) throws Throwable {

		MethodSignature signature = (MethodSignature)joinPoint.getSignature();

		// 서비스 시작 로그
		logServiceStart(signature);
		
		// 파라미터 로그
		logParameters(joinPoint);

		long startTime = System.currentTimeMillis();

		try {
			Object result = joinPoint.proceed();
			long executionTime = System.currentTimeMillis() - startTime;
			long slowMs = getSlowMs(serviceLog);

			// 성공 로그
			logSuccess(result, executionTime, slowMs);

			return result;

		} catch (Exception e) {
			long executionTime = System.currentTimeMillis() - startTime;

			// 실패 로그
			logFailure(executionTime);

			throw e;
		}
	}

	private void logServiceStart(MethodSignature signature) {
		log.debug("{} 요청 서비스: {}.{}()",
			PREFIX,
			signature.getDeclaringType().getSimpleName(),
			signature.getName()
		);
	}

	private void logParameters(ProceedingJoinPoint joinPoint) {
		String params = loggingHelper.extractParameters(joinPoint, null);

		if (!"no".equals(params)) {
			log.debug("{} 파라미터: [{}]", PREFIX, params);
		}
	}

	private void logSuccess(Object result, long executionTime, long slowMs) {
		log.info("{} Service 총 소요시간: {}ms", PREFIX, executionTime);

		if (result != null) {
			String resultSummary = summarizeResult(result);
			log.debug("{} 성공 응답: {}", PREFIX, resultSummary);
		}

		if (slowMs > 0 && executionTime > slowMs) {
			log.warn("{} SLOW!: {}ms (적정 실행 시간: {}ms)",
				PREFIX,
				executionTime,
				slowMs
			);
		}
	}

	private void logFailure(long executionTime) {
		log.error("{} 실패 응답: {}ms",
			PREFIX,
			executionTime
		);
	}

	private String summarizeResult(Object result) {

		if (result instanceof Collection) {
			return "Collection[size=" + ((Collection<?>) result).size() + "]";
		}

		if (result instanceof Boolean) {
			return result.toString();
		}

		if (result instanceof String || result instanceof Number) {
			return result.toString();
		}

		try {
			Method getIdMethod = result.getClass().getMethod("getId");
			Object id = getIdMethod.invoke(result);
			return result.getClass().getSimpleName() + "(id=" + id + ")";
		} catch (Exception e) {
			//
		}

		try {
			String json = objectMapper.writeValueAsString(result);

			// 너무 길면 자르기
			if (json.length() > 200) {
				return result.getClass().getSimpleName() + "=" +
					json.substring(0, 200) + "...";
			}

			return result.getClass().getSimpleName() + "=" + json;

		} catch (JsonProcessingException ex) {
			return result.getClass().getSimpleName();
		}
	}

	private long getSlowMs(ServiceLog serviceLog) {
		return serviceLog != null
			? serviceLog.slowMs()
			: DEFAULT_SLOW_MS;
	}
}
