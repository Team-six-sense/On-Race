package com.kt.onrace.common.logging.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 에러 발생 원인 등을 추적할 목적의 로그입니다(ApiAdvice는 결과만 반환)
 * 비즈니스 로직 실행 추적 -> 서비스 메서드 호출 흐름 파악 -> 실행 시간 측정 -> 로깅
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceLog {

	long slowMs() default 1000;

}
