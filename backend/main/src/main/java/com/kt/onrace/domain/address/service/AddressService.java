package com.kt.onrace.domain.address.service;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.common.exception.BusinessErrorCode;
import com.kt.onrace.common.exception.BusinessException;
import com.kt.onrace.domain.address.dto.AddressDto;
import com.kt.onrace.domain.address.entity.Address;
import com.kt.onrace.domain.address.repository.AddressLabelProjection;
import com.kt.onrace.domain.address.repository.AddressRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressService {

	private static final String AUTO_LABEL_PREFIX = "배송지";
	private static final Pattern AUTO_LABEL_PATTERN = Pattern.compile("^배송지(\\d+)$");

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
		String resolvedLabel = resolveCreateLabel(userId, request.label());
		boolean hasAnyAddress = addressRepository.existsByUserId(userId);
		boolean shouldBeDefault = !hasAnyAddress || Boolean.TRUE.equals(request.isDefault());

		if (shouldBeDefault && hasAnyAddress) {
			unsetDefault(userId);
		}

		Address address = Address.builder()
			.userId(userId)
			.receiverName(request.receiverName())
			.label(resolvedLabel)
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
		Address address = findAddress(userId, addressId);
		String resolvedLabel = resolveUpdateLabel(userId, address, request.label());

		handleDefaultStatus(userId, address, request.isDefault());

		address.update(
			request.receiverName(),
			resolvedLabel,
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

	private String resolveCreateLabel(Long userId, String requestedLabel) {
		String normalizedLabel = normalizeLabel(requestedLabel);
		if (normalizedLabel == null) {
			List<AddressLabelProjection> userAddressLabels = addressRepository.findLabelProjectionsByUserId(userId);
			return generateAutoLabel(userAddressLabels);
		}

		validateDuplicateLabel(userId, normalizedLabel, null);
		return normalizedLabel;
	}

	private String resolveUpdateLabel(Long userId, Address address, String requestedLabel) {
		String normalizedLabel = normalizeLabel(requestedLabel);
		if (normalizedLabel == null) {
			return address.getLabel();
		}

		validateDuplicateLabel(userId, normalizedLabel, address.getId());
		return normalizedLabel;
	}

	private void validateDuplicateLabel(Long userId, String label, Long excludedAddressId) {
		String normalizedForComparison = label.toLowerCase(Locale.ROOT);
		boolean duplicated = excludedAddressId == null
			? addressRepository.existsByUserIdAndNormalizedLabel(userId, normalizedForComparison)
			: addressRepository.existsByUserIdAndNormalizedLabelExcludingId(userId, excludedAddressId, normalizedForComparison);

		if (duplicated) {
			throw new BusinessException(BusinessErrorCode.ADDRESS_DUPLICATE_LABEL);
		}
	}

	private String generateAutoLabel(List<AddressLabelProjection> userAddressLabels) {
		Set<Integer> usedNumbers = userAddressLabels.stream()
			.map(AddressLabelProjection::getLabel)
			.map(this::normalizeLabel)
			.filter(Objects::nonNull)
			.map(AUTO_LABEL_PATTERN::matcher)
			.filter(Matcher::matches)
			.map(matcher -> Integer.parseInt(matcher.group(1)))
			.collect(Collectors.toSet());

		int nextNumber = 1;
		while (usedNumbers.contains(nextNumber)) {
			nextNumber++;
		}

		return AUTO_LABEL_PREFIX + nextNumber;
	}

	private String normalizeLabel(String label) {
		if (label == null || label.isBlank()) {
			return null;
		}

		return label.trim();
	}
}
