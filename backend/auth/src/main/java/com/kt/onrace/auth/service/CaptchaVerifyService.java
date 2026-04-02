package com.kt.onrace.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CaptchaVerifyService {

	private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

	@Value("${recaptcha.secret-key}")
	private String secretKey;

	private final RestClient restClient;

	public boolean verify(String token) {
		if (token == null || token.isBlank()) {
			return false;
		}

		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("secret", secretKey);
		body.add("response", token);

		RecaptchaResponse response = restClient.post()
				.uri(VERIFY_URL)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(body)
				.retrieve()
				.body(RecaptchaResponse.class);

		return response != null && response.success();
	}

	private record RecaptchaResponse(boolean success) {}
}
