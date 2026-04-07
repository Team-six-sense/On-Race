package com.kt.onrace.common.logging.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import com.kt.onrace.common.logging.annotation.ServiceLog;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * [AOP-SERVICE]  Service 총 소요시간: 32ms
 * [AOP-SERVICE]  SLOW!: 5003ms (적정 실행 시간: 1000ms)
 * [AOP-SERVICE]  실패 응답: 5008ms
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ServiceLogAspect {

	private static final String PREFIX = "[AOP-SERVICE] ";
	private static final long DEFAULT_SLOW_MS = 1000;
	private final ObjectProvider<MeterRegistry> meterRegistryProvider;

	@Around("@within(serviceLog) || @annotation(serviceLog)")
	Object logging(ProceedingJoinPoint joinPoint, ServiceLog serviceLog) throws Throwable {

		MethodSignature signature = (MethodSignature)joinPoint.getSignature();

		long startTime = System.currentTimeMillis();

		MeterRegistry meterRegistry = meterRegistryProvider.getIfAvailable();
		Timer.Sample sample = meterRegistry != null ? Timer.start(meterRegistry) : null;

		try {
			Object result = joinPoint.proceed();
			long executionTime = System.currentTimeMillis() - startTime;
			long slowMs = getSlowMs(serviceLog);

			logSuccess(executionTime, slowMs);

			return result;

		} catch (Exception e) {
			long executionTime = System.currentTimeMillis() - startTime;

			logFailure(executionTime);

			throw e;
		} finally {
			if (sample != null && meterRegistry != null) {
				sample.stop(Timer.builder("service.execution")
					.tag("class", joinPoint.getTarget().getClass().getSimpleName())
					.tag("method", signature.getName())
					.register(meterRegistry));
			}
		}
	}

	private void logSuccess(long executionTime, long slowMs) {
		log.info("{} Service 총 소요시간: {}ms", PREFIX, executionTime);

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

	private long getSlowMs(ServiceLog serviceLog) {
		return serviceLog != null
			? serviceLog.slowMs()
			: DEFAULT_SLOW_MS;
	}
}
