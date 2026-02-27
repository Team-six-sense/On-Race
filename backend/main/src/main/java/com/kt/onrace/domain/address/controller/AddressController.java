package com.kt.onrace.domain.address.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kt.onrace.common.response.ApiResponse;
import com.kt.onrace.domain.address.dto.AddressDto;
import com.kt.onrace.domain.address.service.AddressService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/addresses")
public class AddressController {

	private final AddressService addressService;

	@GetMapping
	public ApiResponse<List<AddressDto.Response>> list(
		@RequestHeader("X-User-Id") Long userId
	) {
		return ApiResponse.success(addressService.list(userId));
	}

	@GetMapping("/{id}")
	public ApiResponse<AddressDto.Response> get(
		@RequestHeader("X-User-Id") Long userId,
		@PathVariable Long id
	) {
		return ApiResponse.success(addressService.get(userId, id));
	}

	@PostMapping
	public ApiResponse<AddressDto.Response> create(
		@RequestHeader("X-User-Id") Long userId,
		@Valid @RequestBody AddressDto.SaveRequest request
	) {
		return ApiResponse.success(addressService.create(userId, request));
	}

	@PutMapping("/{id}")
	public ApiResponse<AddressDto.Response> update(
		@RequestHeader("X-User-Id") Long userId,
		@PathVariable Long id,
		@Valid @RequestBody AddressDto.SaveRequest request
	) {
		return ApiResponse.success(addressService.update(userId, id, request));
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(
		@RequestHeader("X-User-Id") Long userId,
		@PathVariable Long id
	) {
		addressService.delete(userId, id);
		return ApiResponse.success();
	}

	@PatchMapping("/{id}/default")
	public ApiResponse<Void> setDefault(
		@RequestHeader("X-User-Id") Long userId,
		@PathVariable Long id
	) {
		addressService.setDefault(userId, id);
		return ApiResponse.success();
	}
}
