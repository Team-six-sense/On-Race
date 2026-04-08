package com.kt.onrace.domain.mypage.client;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;

@Component
public class HttpAuthAccountClient implements AuthAccountClient {

	private static final ParameterizedTypeReference<AuthApiResponse<AuthAccountPayload>> ACCOUNT_ME_RESPONSE_TYPE =
		new ParameterizedTypeReference<>() {
		};

	private final RestClient restClient;

	public HttpAuthAccountClient(
		RestClient.Builder restClientBuilder,
		@Value("${api.url.auth}") String authServiceUrl,
		@Value("${gateway.internal.secret}") String gatewaySecret
	) {
		this.restClient = restClientBuilder
			.baseUrl(authServiceUrl)
			.defaultHeader("X-Gateway-Token", gatewaySecret)
			.build();
	}

	@Override
	public AccountSummary getMyInfo(Long userId) {
		try {
			AuthApiResponse<AuthAccountPayload> response = restClient.get()
				.uri("/account/me")
				.header("X-User-Id", String.valueOf(userId))
				.retrieve()
				.onStatus(
					status -> status.value() == HttpStatus.UNAUTHORIZED.value(),
					(request, clientResponse) -> {
						throw new BusinessException(BusinessErrorCode.AUTH_NOT_FOUND_USER);
					}
				)
				.onStatus(
					status -> status.value() == HttpStatus.FORBIDDEN.value(),
					(request, clientResponse) -> {
						throw new BusinessException(BusinessErrorCode.AUTH_FORBIDDEN_USER);
					}
				)
				.onStatus(
					HttpStatusCode::isError,
					(request, clientResponse) -> {
						throw new BusinessException(BusinessErrorCode.COMMON_SYSTEM_ERROR);
					}
				)
				.body(ACCOUNT_ME_RESPONSE_TYPE);

			if (response == null || !response.success() || response.data() == null) {
				throw new BusinessException(BusinessErrorCode.COMMON_SYSTEM_ERROR);
			}

			AuthAccountPayload data = response.data();
			return new AccountSummary(
				data.email(),
				data.name(),
				data.phone(),
				data.authProvider(),
				data.canChangePassword(),
				data.verificationStatus(),
				data.marketingConsent()
			);
		} catch (RestClientException exception) {
			throw new BusinessException(BusinessErrorCode.COMMON_SYSTEM_ERROR);
		}
	}

	private record AuthApiResponse<T>(
		boolean success,
		String code,
		String message,
		T data,
		LocalDateTime timestamp
	) {
	}

	private record AuthAccountPayload(
		String email,
		String name,
		String phone,
		String authProvider,
		boolean canChangePassword,
		String verificationStatus,
		boolean marketingConsent
	) {
	}
}
