package com.kt.onrace.common.logging.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 이 어노테이션이 붙은 파라미터는 API 로그에서 완전히 제외됩니다.
 * 비밀번호, 토큰 등 민감정보가 포함된 DTO에 적용하세요.
 */
@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SensitiveLog {
}
