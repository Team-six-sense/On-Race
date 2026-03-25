package com.kt.onrace.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateNameRequest(
	@NotBlank @Size(max = 50) String name
) {
}
