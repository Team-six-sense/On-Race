package com.kt.onrace.common.exception;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.kt.onrace.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Hidden
@RestControllerAdvice
@RequiredArgsConstructor
public class ApiAdvice {

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> internalServerError(Exception e) {
		ErrorCode errorCode = BusinessErrorCode.COMMON_SYSTEM_ERROR;

		log.error("Internal Server Error ", e);

		return result(errorCode);
	}

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> customBusinessException(BusinessException e, HttpServletRequest request) {
		ErrorCode errorCode = e.getErrorCode();

		log.warn("[BusinessException] status={}, code={}, message={}, path={}",
			errorCode.getStatus(),
			errorCode.getCode(),
			errorCode.getMessage(),
			request.getRequestURI(),
			e
		);

		return result(e.getErrorCode());
	}

	@ExceptionHandler(InfraException.class)
	public ResponseEntity<ApiResponse<Void>> customInfraException(InfraException e, HttpServletRequest request) {
		ErrorCode errorCode = BusinessErrorCode.COMMON_SYSTEM_ERROR; // 내부 인프라 예외는 클라이언트에 일반 시스템 오류로 응답

		log.error("[InfraException] status={}, code={}, message={}, path={}",
			errorCode.getStatus(),
			errorCode.getCode(),
			errorCode.getMessage(),
			request.getRequestURI(),
			e
		);

		return result(errorCode);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> methodArgumentNotValidException(MethodArgumentNotValidException e) {
		ErrorCode errorCode = BusinessErrorCode.COMMON_INVALID_PARAMETER;

		log.warn("Validation Exception ", e);

		String message = e.getBindingResult()
			.getFieldErrors()
			.stream()
			.findFirst()
			.map(DefaultMessageSourceResolvable::getDefaultMessage)
			.orElse("유효하지 않은 요청입니다");

		return ResponseEntity
			.status(errorCode.getStatus())
			.body(ApiResponse.error(errorCode, message));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
		ErrorCode errorCode = BusinessErrorCode.COMMON_INVALID_PARAMETER;

		log.warn("IllegalArgumentException: {}", e.getMessage());

		return result(errorCode);
	}

	@ExceptionHandler(DataAccessException.class)
	public ResponseEntity<ApiResponse<Void>> handleDataAccess(DataAccessException e) {
		ErrorCode errorCode = BusinessErrorCode.COMMON_SYSTEM_ERROR;

		log.error("DataAccessException: {}", e.getMessage(), e);

		return result(errorCode);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
		ErrorCode errorCode = BusinessErrorCode.COMMON_HTTP_METHOD_NOT_SUPPORTED;

		log.warn("HttpRequestMethodNotSupportedException: {}", e.getMessage());

		return result(errorCode);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
		ErrorCode errorCode = BusinessErrorCode.COMMON_INVALID_REQUEST;

		log.warn("HttpMessageNotReadableException: {}", e.getMessage());

		return result(errorCode);
	}

	private ResponseEntity<ApiResponse<Void>> result(ErrorCode errorCode) {
		return ResponseEntity
			.status(errorCode.getStatus())
			.body(ApiResponse.error(errorCode));
	}
}
