package com.kt.onrace.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SmsSendRequest(
                @NotBlank(message = "핸드폰 번호를 입력해 주세요.") @Pattern(regexp = "^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$", message = "올바른 핸드폰 번호 형식이 아닙니다. (- 제외)") String phoneNumber) {
}
