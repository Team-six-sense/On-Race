package com.kt.onrace.auth.dto;

import java.time.LocalDate;
import java.util.List;

import com.kt.onrace.auth.entity.Gender;
import com.kt.onrace.common.logging.annotation.SensitiveLog;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@SensitiveLog
public record SignupRequest(
		@NotBlank(message = "이메일을 입력해 주세요.") @Email(message = "올바른 이메일 형식이 아닙니다.") @Size(max = 100) String email,

		@Size(max = 50, message = "이름은 50자 이하이어야 합니다.") String name,

		Gender gender,

		LocalDate birthdate,

		@NotBlank(message = "비밀번호를 입력해 주세요.") @Pattern(regexp = "^(?:(?=.*[a-zA-Z])(?=.*\\d)|(?=.*[a-zA-Z])(?=.*[!@#$%^&*])|(?=.*\\d)(?=.*[!@#$%^&*]))[A-Za-z\\d!@#$%^&*]{10,16}$", message = "비밀번호는 10~16자이며, 영문/숫자/특수문자(!@#$%^&*) 중 2가지 이상 조합이어야 합니다.") String password,

		@NotBlank(message = "핸드폰 번호를 입력해 주세요.") @Pattern(regexp = "^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$", message = "올바른 핸드폰 번호 형식이 아닙니다. (- 제외)") String phoneNumber,

		@NotEmpty(message = "약관 동의 정보를 입력해 주세요.") List<@Valid TermAgreement> termAgreements) {
}
