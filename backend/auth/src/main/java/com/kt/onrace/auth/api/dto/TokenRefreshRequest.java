package com.kt.onrace.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TokenRefreshRequest {

	@NotBlank(message = "리프레시 토큰을 입력해 주세요.")
	private String refreshToken;
}
