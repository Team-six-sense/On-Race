package com.kt.onrace.auth.dto;

import java.time.LocalDateTime;

public record SignupResponse(Long id, String email, LocalDateTime createdAt) {
}
