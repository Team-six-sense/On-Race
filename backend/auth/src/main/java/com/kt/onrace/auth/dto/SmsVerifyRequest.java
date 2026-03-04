package com.kt.onrace.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SmsVerifyRequest(
        @NotBlank(message = "핸드폰 번호를 입력해 주세요.") @Pattern(regexp = "^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$", message = "올바른 핸드폰 번호 형식이 아닙니다. (- 제외)") String phoneNumber,

        @NotBlank(message = "인증 코드를 입력해 주세요.") @Size(min = 6, max = 6, message = "인증 코드는 6자리여야 합니다.") String code) {
}
