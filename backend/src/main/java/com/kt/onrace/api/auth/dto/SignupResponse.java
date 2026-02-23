package com.kt.onrace.api.auth.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public record SignupResponse(Long id, String email, LocalDateTime createdAt) {}
