package com.kt.onrace.auth.api.dto;


import com.kt.onrace.auth.domain.entity.Gender;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SignupRequest {

	@NotBlank(message = "로그인 ID를 입력해 주세요.")
	@Size(max = 100)
	private String loginId;

	@NotBlank(message = "이메일을 입력해 주세요.")
	@Email(message = "올바른 이메일 형식이 아닙니다.")
	@Size(max = 100)
	private String email;

	@NotBlank(message = "비밀번호를 입력해 주세요.")
	@Pattern(
		regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^])[A-Za-z\\d!@#$%^]{8,}$",
		message = "비밀번호는 대소문자, 숫자, 특수문자(!@#$%^)를 포함한 8자 이상이어야 합니다."
	)
	private String password;

	@NotBlank(message = "이름을 입력해 주세요.")
	@Size(max = 50)
	private String name;

	@NotBlank(message = "휴대폰 번호를 입력해 주세요.")
	@Size(max = 20)
	private String mobile;

	@NotNull(message = "성별을 선택해 주세요.")
	private Gender gender;
}