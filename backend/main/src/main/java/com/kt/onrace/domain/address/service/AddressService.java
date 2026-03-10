package com.kt.onrace.domain.address.service;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.domain.address.dto.AddressDto;
import com.kt.onrace.domain.address.entity.Address;
import com.kt.onrace.domain.address.repository.AddressLabelProjection;
import com.kt.onrace.domain.address.repository.AddressRepository;

import org.hibernate.exception.ConstraintViolationException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressService {

	private static final String AUTO_LABEL_PREFIX = "배송지";
	private static final Pattern AUTO_LABEL_PATTERN = Pattern.compile("^배송지(\\d+)$");
	private static final String NORMALIZED_LABEL_UNIQUE_CONSTRAINT = "uk_address_user_normalized_label";

	private final AddressRepository addressRepository;

	public List<AddressDto.Response> list(Long userId) {
		return addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
			.stream()
			.map(AddressDto.Response::from)
			.toList();
	}

	public AddressDto.Response get(Long userId, Long addressId) {
		Address address = findAddress(userId, addressId);
		return AddressDto.Response.from(address);
	}

	@Transactional
	public AddressDto.Response create(Long userId, AddressDto.SaveRequest request) {
		try {
			String label = request.label();
			if (label == null || label.isBlank()) {
				List<AddressLabelProjection> userAddressLabels = addressRepository.findLabelProjectionsByUserId(userId);
				label = generateAutoLabel(userAddressLabels);
			}

			boolean hasAnyAddress = addressRepository.existsByUserId(userId);
			boolean shouldBeDefault = !hasAnyAddress || Boolean.TRUE.equals(request.isDefault());

			if (shouldBeDefault && hasAnyAddress) {
				unsetDefault(userId);
			}

			Address address = Address.builder()
				.userId(userId)
				.receiverName(request.receiverName())
				.label(label)
				.phone(request.phone())
				.zipcode(request.zipcode())
				.address1(request.address1())
				.address2(request.address2())
				.memo(request.memo())
				.isDefault(shouldBeDefault)
				.build();

			return AddressDto.Response.from(addressRepository.saveAndFlush(address));
		} catch (DataIntegrityViolationException e) {
			throw translateDuplicateLabelException(e);
		}
	}

	@Transactional
	public AddressDto.Response update(Long userId, Long addressId, AddressDto.SaveRequest request) {
		try {
			Address address = findAddress(userId, addressId);

			String label = request.label();
			if (label != null && !label.isBlank()) {
				address.applyLabel(label);
			}

			handleDefaultStatus(userId, address, request.isDefault());

			address.update(
				request.receiverName(),
				request.phone(),
				request.zipcode(),
				request.address1(),
				request.address2(),
				request.memo()
			);
			addressRepository.flush();

			return AddressDto.Response.from(address);
		} catch (DataIntegrityViolationException e) {
			throw translateDuplicateLabelException(e);
		}
	}

	@Transactional
	public void delete(Long userId, Long addressId) {
		Address address = findAddress(userId, addressId);
		boolean wasDefault = address.isDefault();

		addressRepository.delete(address);

		if (wasDefault) {
			promoteDefaultToLatest(userId);
		}
	}

	@Transactional
	public void setDefault(Long userId, Long addressId) {
		Address address = findAddress(userId, addressId);
		if (address.isDefault()) {
			return;
		}

		unsetDefault(userId);
		address.markDefault();
	}

	private Address findAddress(Long userId, Long addressId) {
		return addressRepository.findByIdAndUserId(addressId, userId)
			.orElseThrow(() -> new BusinessException(BusinessErrorCode.ADDRESS_NOT_FOUND));
	}

	private void handleDefaultStatus(Long userId, Address address, Boolean wantDefault) {
		if (wantDefault == null || wantDefault == address.isDefault()) {
			return;
		}

		if (wantDefault) {
			unsetDefault(userId);
			address.markDefault();
		} else {
			address.unmarkDefault();
			promoteDefaultToLatestExcluding(userId, address.getId());
		}
	}

	private void unsetDefault(Long userId) {
		addressRepository.findFirstByUserIdAndIsDefaultTrue(userId)
			.ifPresent(Address::unmarkDefault);
	}

	private void promoteDefaultToLatest(Long userId) {
		addressRepository.findFirstByUserIdOrderByCreatedAtDesc(userId)
			.ifPresent(Address::markDefault);
	}

	private void promoteDefaultToLatestExcluding(Long userId, Long excludedAddressId) {
		addressRepository.findFirstByUserIdAndIdNotOrderByCreatedAtDesc(userId, excludedAddressId)
			.ifPresent(Address::markDefault);
	}

	private String generateAutoLabel(List<AddressLabelProjection> userAddressLabels) {
		Set<Long> usedNumbers = userAddressLabels.stream()
			.map(AddressLabelProjection::getLabel)
			.filter(Objects::nonNull)
			.map(String::trim)
			.filter(label -> !label.isEmpty())
			.map(AUTO_LABEL_PATTERN::matcher)
			.filter(Matcher::matches)
			.map(matcher -> Long.parseLong(matcher.group(1)))
			.collect(Collectors.toSet());

		long nextNumber = 1L;
		while (usedNumbers.contains(nextNumber)) {
			nextNumber++;
		}

		return AUTO_LABEL_PREFIX + nextNumber;
	}

	private BusinessException translateDuplicateLabelException(DataIntegrityViolationException e) {
		if (isDuplicateLabelViolation(e)) {
			return new BusinessException(BusinessErrorCode.ADDRESS_DUPLICATE_LABEL);
		}

		throw e;
	}

	private boolean isDuplicateLabelViolation(Throwable throwable) {
		Throwable current = throwable;
		while (current != null) {
			if (current instanceof ConstraintViolationException constraintViolationException) {
				String constraintName = constraintViolationException.getConstraintName();
				if (constraintName != null
					&& constraintName.toLowerCase(Locale.ROOT).contains(NORMALIZED_LABEL_UNIQUE_CONSTRAINT)) {
					return true;
				}
			}
			current = current.getCause();
		}
		return false;
	}
}
