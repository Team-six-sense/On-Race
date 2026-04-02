package com.kt.onrace.domain.payment.client;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.domain.payment.config.TossPaymentProperties;
import com.kt.onrace.domain.payment.dto.PaymentConfirmRequestDto;
import com.kt.onrace.domain.payment.dto.TossPaymentResponseDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TossPaymentClient {

	private final RestClient restClient;

	public TossPaymentClient(TossPaymentProperties properties) {
		String encodedAuthKey = Base64.getEncoder().encodeToString((properties.getSecretKey() + ":").getBytes(StandardCharsets.UTF_8));
		
		this.restClient = RestClient.builder()
			.baseUrl(properties.getUrl())
			.defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuthKey)
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.build();
	}

	public TossPaymentResponseDto confirmPayment(PaymentConfirmRequestDto requestDto) {
		try {
			return restClient.post()
				.body(requestDto)
				.retrieve()
				.body(TossPaymentResponseDto.class);
		} catch (HttpClientErrorException e) {
			log.error("Toss Payment Confirm Error: {}", e.getResponseBodyAsString());
			throw new BusinessException(BusinessErrorCode.PAYMENT_CONFIRM_FAILED);
		} catch (Exception e) {
			log.error("Toss Payment Confirm Unknown Error", e);
			throw new BusinessException(BusinessErrorCode.COMMON_SYSTEM_ERROR);
		}
	}
}
