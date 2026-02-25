package com.kt.onrace.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {

	@NotBlank(message = "로그인 ID를 입력해 주세요.")
	@Size(max = 100)
	private String loginId;

	@NotBlank(message = "비밀번호를 입력해 주세요.")
	private String password;
}
