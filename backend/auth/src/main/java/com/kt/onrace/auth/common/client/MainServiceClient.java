package com.kt.onrace.auth.common.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "feign-main-service", url = "${api.url.main}")
public interface MainServiceClient {

	@PostMapping("/internal/members/{userId}/sync-create")
	void syncUserCreated(@PathVariable("userId") Long userId);

	@PostMapping("/internal/members/{userId}/sync-delete")
	void syncUserDeleted(@PathVariable("userId") Long userId);
	
}
