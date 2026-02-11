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
		log.error("Internal Server Error ", e);

		return ResponseEntity
			.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(ApiResponse.error(BusinessErrorCode.COMMON_SYSTEM_ERROR));
	}

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> customBusinessException(BusinessException e,
		HttpServletRequest request) {
		log.warn("[BusinessException] status={}, code={}, message={}, path={}",
			e.getErrorCode().getStatus(),
			e.getErrorCode().getCode(),
			e.getErrorCode().getMessage(),
			request.getRequestURI());

		return ResponseEntity
			.status(e.getErrorCode().getStatus())
			.body(ApiResponse.error(e.getErrorCode()));
	}

	@ExceptionHandler(InfraException.class)
	public ResponseEntity<ApiResponse<Void>> customInfraException(InfraException e, HttpServletRequest request) {
		log.warn("[InfraException] status={}, code={}, message={}, path={}",
			e.getErrorCode().getStatus(),
			e.getErrorCode().getCode(),
			e.getErrorCode().getMessage(),
			request.getRequestURI(),
			e);

		return ResponseEntity
			.status(e.getErrorCode().getStatus())
			.body(ApiResponse.error(e.getErrorCode()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> methodArgumentNotValidException(MethodArgumentNotValidException e) {
		log.warn("Validation Exception ", e);

		String message = e.getBindingResult()
			.getFieldErrors()
			.stream()
			.findFirst()
			.map(DefaultMessageSourceResolvable::getDefaultMessage)
			.orElse("유효하지 않은 요청입니다");

		return ResponseEntity
			.status(HttpStatus.BAD_REQUEST)
			.body(ApiResponse.error(BusinessErrorCode.COMMON_INVALID_PARAMETER, message));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
		log.warn("IllegalArgumentException: {}", e.getMessage());

		return ResponseEntity
			.status(HttpStatus.BAD_REQUEST)
			.body(ApiResponse.error(BusinessErrorCode.COMMON_INVALID_PARAMETER, e.getMessage()));
	}

	@ExceptionHandler(DataAccessException.class)
	public ResponseEntity<ApiResponse<Void>> handleDataAccess(DataAccessException e) {
		log.error("DataAccessException: {}", e.getMessage(), e);

		return ResponseEntity
			.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(ApiResponse.error(InfraErrorCode.DB_ERROR_ACCESS));
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
		log.warn("HttpRequestMethodNotSupportedException: {}", e.getMessage());

		return ResponseEntity
			.status(HttpStatus.METHOD_NOT_ALLOWED)
			.body(ApiResponse.error(BusinessErrorCode.COMMON_HTTP_METHOD_NOT_SUPPORTED));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
		log.warn("HttpMessageNotReadableException: {}", e.getMessage());

		return ResponseEntity
			.status(HttpStatus.BAD_REQUEST)
			.body(ApiResponse.error(
				BusinessErrorCode.COMMON_INVALID_REQUEST,  // 또는 적절한 ErrorCode
				"유효하지 않은 BODY입니다"
			));
	}
}
