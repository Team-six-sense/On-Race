package com.kt.onrace.api.auth.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupResponse {

	private Long id;
	private String email;
	private LocalDateTime createdAt;
}
