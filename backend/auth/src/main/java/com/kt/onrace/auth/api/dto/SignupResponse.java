package com.kt.onrace.auth.api.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public record SignupResponse(Long id, String email, LocalDateTime createdAt) {}
