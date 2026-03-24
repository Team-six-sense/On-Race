package com.kt.onrace.auth.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateMarketingConsentRequest(
	@NotNull Boolean marketingConsent
) {
}
