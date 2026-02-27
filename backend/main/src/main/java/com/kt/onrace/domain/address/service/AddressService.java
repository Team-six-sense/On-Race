package com.kt.onrace.domain.address.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.domain.address.dto.AddressDto;
import com.kt.onrace.domain.address.entity.Address;
import com.kt.onrace.domain.address.repository.AddressRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressService {

	private final AddressRepository addressRepository;

	public List<AddressDto.Response> list(Long userId) {
		return addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
			.stream()
			.map(AddressDto.Response::from)
			.toList();
	}

	public AddressDto.Response get(Long userId, Long addressId) {
		Address address = addressRepository.findByIdAndUserId(addressId, userId)
			.orElseThrow(() -> new BusinessException(BusinessErrorCode.ADDRESS_NOT_FOUND));

		return AddressDto.Response.from(address);
	}

	@Transactional
	public AddressDto.Response create(Long userId, AddressDto.SaveRequest request) {
		boolean hasAny = addressRepository.existsByUserId(userId);

		boolean shouldBeDefault = !hasAny || Boolean.TRUE.equals(request.isDefault());

		if (shouldBeDefault && hasAny) {
			unsetDefaultIfExists(userId);
		}

		Address address = Address.builder()
			.userId(userId)
			.receiverName(request.receiverName())
			.phone(request.phone())
			.zipcode(request.zipcode())
			.address1(request.address1())
			.address2(request.address2())
			.memo(request.memo())
			.isDefault(shouldBeDefault)
			.build();

		return AddressDto.Response.from(addressRepository.save(address));
	}

	@Transactional
	public AddressDto.Response update(Long userId, Long addressId, AddressDto.SaveRequest request) {
		Address address = addressRepository.findByIdAndUserId(addressId, userId)
			.orElseThrow(() -> new BusinessException(BusinessErrorCode.ADDRESS_NOT_FOUND));

		Boolean wantDefault = request.isDefault();

		if (Boolean.TRUE.equals(wantDefault) && !address.isDefault()) {
			unsetDefaultIfExists(userId);
			address.markDefault();
		} else if (Boolean.FALSE.equals(wantDefault) && address.isDefault()) {
			address.unmarkDefault();
			promoteDefaultIfNeededExcluding(userId, addressId);
		}

		address.update(
			request.receiverName(),
			request.phone(),
			request.zipcode(),
			request.address1(),
			request.address2(),
			request.memo()
		);

		return AddressDto.Response.from(address);
	}

	@Transactional
	public void delete(Long userId, Long addressId) {
		Address address = addressRepository.findByIdAndUserId(addressId, userId)
			.orElseThrow(() -> new BusinessException(BusinessErrorCode.ADDRESS_NOT_FOUND));

		boolean wasDefault = address.isDefault();

		addressRepository.delete(address);

		if (wasDefault) {
			promoteDefaultIfNeeded(userId);
		}
	}

	@Transactional
	public void setDefault(Long userId, Long addressId) {
		Address address = addressRepository.findByIdAndUserId(addressId, userId)
			.orElseThrow(() -> new BusinessException(BusinessErrorCode.ADDRESS_NOT_FOUND));

		if (address.isDefault()) {
			return;
		}

		unsetDefaultIfExists(userId);
		address.markDefault();
	}

	private void unsetDefaultIfExists(Long userId) {
		addressRepository.findFirstByUserIdAndIsDefaultTrue(userId)
			.ifPresent(Address::unmarkDefault);
	}

	private void promoteDefaultIfNeeded(Long userId) {
		List<Address> remaining = addressRepository.findByUserIdOrderByCreatedAtDesc(userId);
		if (remaining.isEmpty()) {
			return;
		}
		remaining.get(0).markDefault();
	}

	private void promoteDefaultIfNeededExcluding(Long userId, Long excludedAddressId) {
		List<Address> remaining = addressRepository.findByUserIdOrderByCreatedAtDesc(userId);
		if (remaining.isEmpty()) {
			return;
		}
		remaining.stream()
			.filter(address -> !address.getId().equals(excludedAddressId))
			.findFirst()
			.ifPresent(Address::markDefault);
	}
}
