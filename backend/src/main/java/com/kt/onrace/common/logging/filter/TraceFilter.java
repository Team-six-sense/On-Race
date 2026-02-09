package com.kt.onrace.common.logging.filter;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.kt.onrace.common.logging.helper.TraceIdGenerator;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * 문제 발생
 * 비동기시 mdc에 저장할 traceId가 api 리턴시에 clear를 해버려 비동기 쓰레드에서 id복사를 못하는 문제
 * -> filter단을 앞에 세운 후에 filter에서 id관리하는 방식 변경
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceFilter implements Filter {
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
		throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;

		try {
			TraceIdGenerator.getOrCreateTraceId(httpRequest);
			filterChain.doFilter(request, response);

		} finally {
			TraceIdGenerator.clear();
		}
	}
}
