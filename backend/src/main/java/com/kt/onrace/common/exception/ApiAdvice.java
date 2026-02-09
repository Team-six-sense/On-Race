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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Hidden
@RestControllerAdvice
@RequiredArgsConstructor
public class ApiAdvice {

	/**
	 * 내부 서버 오류 처리
	 * @param e
	 * @return 에러 코드 및 메세지
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> internalServerError(Exception e) {
		log.error("Internal Server Error ", e);

		return ResponseEntity
			.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(ApiResponse.error(ErrorCode.COMMON_SYSTEM_ERROR));
	}

	/**
	 * 커스텀 예외 처리
	 * @param e
	 * @return 에러 코드 및 메세지
	 */
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> customException(BusinessException e) {
		log.warn("Custom Exception: {}", e.getErrorCode(), e);

		return ResponseEntity
			.status(e.getErrorCode().getStatus())
			.body(ApiResponse.error(e.getErrorCode()));
	}

	/**
	 * 유효성 검사 예외 처리
	 * @param e
	 * @return 에러 코드 및 메세지
	 */
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
			.body(ApiResponse.error(ErrorCode.COMMON_INVALID_PARAMETER, message));
	}

	/**
	 * IllegalArgumentException 에러
	 * @param e
	 * @return 에러 코드 및 메세지
	 */
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
		log.warn("IllegalArgumentException: {}", e.getMessage());

		return ResponseEntity
			.status(HttpStatus.BAD_REQUEST)
			.body(ApiResponse.error(ErrorCode.COMMON_INVALID_PARAMETER, e.getMessage()));
	}

	/**
	 * DB 에러
	 * @param e
	 * @return 에러 코드 및 메세지
	 */
	@ExceptionHandler(DataAccessException.class)
	public ResponseEntity<ApiResponse<Void>> handleDataAccess(DataAccessException e) {
		log.error("DataAccessException: {}", e.getMessage(), e);

		return ResponseEntity
			.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(ApiResponse.error(ErrorCode.DB_ACCESS_ERROR));
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
		log.warn("HttpRequestMethodNotSupportedException: {}", e.getMessage());

		return ResponseEntity
			.status(HttpStatus.METHOD_NOT_ALLOWED)
			.body(ApiResponse.error(ErrorCode.COMMON_HTTP_METHOD_NOT_SUPPORTED));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
		log.warn("HttpMessageNotReadableException: {}", e.getMessage());

		return ResponseEntity
			.status(HttpStatus.BAD_REQUEST)
			.body(ApiResponse.error(
				ErrorCode.COMMON_INVALID_REQUEST,  // 또는 적절한 ErrorCode
				"유효하지 않은 BODY입니다"
			));
	}
}
