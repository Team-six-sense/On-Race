package com.kt.onrace.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WithdrawRequest {

	@NotBlank(message = "비밀번호를 입력해 주세요.")
	private String password;
}
