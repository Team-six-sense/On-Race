package com.kt.onrace.common.filter;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class GatewayAccessFilter extends OncePerRequestFilter {

	@Value("${gateway.internal.secret}")
	private String gatewaySecret;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {

		String requestURI = request.getRequestURI();

		if (requestURI.startsWith("/actuator") || requestURI.startsWith("/api/health")) {
			filterChain.doFilter(request, response);
			return;
		}

		String gatewayToken = request.getHeader("X-Gateway-Token");

		if (gatewayToken == null || !gatewayToken.equals(gatewaySecret)) {
			response.setStatus(HttpStatus.FORBIDDEN.value());
			response.setContentType("application/json;charset=UTF-8");
			response.getWriter().write(
				"{\"error\": \"Access Denied: Direct access is not allowed. Please enter through the API Gateway.\"}");
			return;
		}

		filterChain.doFilter(request, response);
	}
}
