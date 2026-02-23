package com.kt.onrace.common.exception;

import org.springframework.beans.TypeMismatchException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.kt.onrace.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Hidden
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> internalServerError(Exception e, HttpServletRequest request) {
		ErrorCode errorCode = BusinessErrorCode.COMMON_SYSTEM_ERROR;

		log.error("[Exception] code={}, message={}, path={} ",
			errorCode.getCode(),
			errorCode.getMessage(),
			request.getRequestURI(),
			e);

		return response(errorCode);
	}

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> customBusinessException(BusinessException e, HttpServletRequest request) {
		ErrorCode errorCode = e.getErrorCode();

		log.warn("[BusinessException] status={}, code={}, message={}, path={}",
			errorCode.getStatus(),
			errorCode.getCode(),
			errorCode.getMessage(),
			request.getRequestURI()
		);

		return response(e.getErrorCode());
	}

	@ExceptionHandler(InfraException.class)
	public ResponseEntity<ApiResponse<Void>> customInfraException(InfraException e, HttpServletRequest request) {
		ErrorCode infraError = e.getErrorCode();
		ErrorCode resError = BusinessErrorCode.COMMON_SYSTEM_ERROR; // 내부 인프라 예외는 클라이언트에 일반 시스템 오류로 응답

		log.error("[InfraException] status={}, code={}, message={}, path={}",
			infraError.getStatus(),
			infraError.getCode(),
			infraError.getMessage(),
			request.getRequestURI(),
			e
		);

		return response(resError);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> methodArgumentNotValidException(MethodArgumentNotValidException e,
		HttpServletRequest request) {
		ErrorCode errorCode = BusinessErrorCode.COMMON_INVALID_PARAMETER;

		String message = e.getBindingResult()
			.getFieldErrors()
			.stream()
			.findFirst()
			.map(DefaultMessageSourceResolvable::getDefaultMessage)
			.orElse("유효하지 않은 요청입니다");

		log.warn("[MethodArgumentNotValidException] code={}, message={}, path={}",
			errorCode.getCode(),
			errorCode.getMessage(),
			request.getRequestURI()
		);

		return ResponseEntity
			.status(errorCode.getStatus())
			.body(ApiResponse.error(errorCode, message));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e,
		HttpServletRequest request) {
		ErrorCode errorCode = BusinessErrorCode.COMMON_INVALID_PARAMETER;

		log.warn("[IllegalArgumentException] code={}, message={}, path={}",
			errorCode.getCode(),
			e.getMessage(),
			request.getRequestURI());

		return response(errorCode);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e,
		HttpServletRequest request) {
		ErrorCode errorCode = BusinessErrorCode.COMMON_HTTP_METHOD_NOT_SUPPORTED;

		log.warn("[HttpRequestMethodNotSupportedException] code={}, message={}, path={}, method={}",
			errorCode.getCode(),
			errorCode.getMessage(),
			request.getRequestURI(),
			e.getMethod()
		);

		return response(errorCode);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException e,
		HttpServletRequest request) {
		ErrorCode errorCode = BusinessErrorCode.COMMON_INVALID_REQUEST;

		log.warn("[HttpMessageNotReadableException] code={}, message={}, path={}",
			errorCode.getCode(),
			errorCode.getMessage(),
			request.getRequestURI());

		return response(errorCode);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException e,
		HttpServletRequest request) {
		ErrorCode errorCode = BusinessErrorCode.COMMON_INVALID_PARAMETER;

		String violationMessage = e.getConstraintViolations()
			.stream()
			.findFirst()
			.map(v -> v.getPropertyPath() + " " + v.getMessage())
			.orElse("");

		log.warn("[ConstraintViolationException] code={}, message={}, path={}, violation={}",
			errorCode.getCode(),
			errorCode.getMessage(),
			request.getRequestURI(),
			violationMessage
		);

		return response(errorCode);
	}

	@ExceptionHandler(TypeMismatchException.class)
	public ResponseEntity<ApiResponse<Void>> handleTypeMismatchException(TypeMismatchException e,
		HttpServletRequest request) {
		ErrorCode errorCode = BusinessErrorCode.COMMON_INVALID_FORMAT;

		log.warn("[TypeMismatchException] code={}, message={}, path={}",
			errorCode.getCode(),
			errorCode.getMessage(),
			request.getRequestURI()
		);

		return response(errorCode);
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameterException(
		MissingServletRequestParameterException e, HttpServletRequest request) {
		ErrorCode errorCode = BusinessErrorCode.COMMON_INVALID_PARAMETER;

		log.warn("[MissingServletRequestParameterException] code={}, message={}, path={}, param={}",
			errorCode.getCode(),
			errorCode.getMessage(),
			request.getRequestURI(),
			e.getParameterName()
		);

		return response(errorCode);
	}

	private ResponseEntity<ApiResponse<Void>> response(ErrorCode errorCode) {
		return ResponseEntity
			.status(errorCode.getStatus())
			.body(ApiResponse.error(errorCode));
	}
}
