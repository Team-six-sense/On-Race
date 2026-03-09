package com.kt.onrace.domain.address.service;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		Address address = addressRepository.findByIdAndUserId(addressId, userId)
			.orElseThrow(() -> new BusinessException(BusinessErrorCode.ADDRESS_NOT_FOUND));

		return AddressDto.Response.from(address);
	}

	@Transactional
	public AddressDto.Response create(Long userId, AddressDto.SaveRequest request) {
		String normalizedLabel = normalizeLabel(request.label());
		boolean hasAny;
		String resolvedLabel;

		if (normalizedLabel == null) {
			List<AddressLabelProjection> userAddressLabels = addressRepository.findLabelProjectionsByUserId(userId);
			hasAny = !userAddressLabels.isEmpty();
			resolvedLabel = generateAutoLabel(userAddressLabels);
		} else {
			hasAny = addressRepository.existsByUserId(userId);
			validateDuplicateLabel(userId, normalizedLabel, null);
			resolvedLabel = normalizedLabel;
		}

		boolean shouldBeDefault = !hasAny || Boolean.TRUE.equals(request.isDefault());

		if (shouldBeDefault && hasAny) {
			unsetDefaultIfExists(userId);
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
		Address address = addressRepository.findByIdAndUserId(addressId, userId)
			.orElseThrow(() -> new BusinessException(BusinessErrorCode.ADDRESS_NOT_FOUND));
		String resolvedLabel = resolveUpdateLabel(userId, address, request.label());

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
		addressRepository.findFirstByUserIdOrderByCreatedAtDesc(userId)
			.ifPresent(Address::markDefault);
	}

	private void promoteDefaultIfNeededExcluding(Long userId, Long excludedAddressId) {
		addressRepository.findFirstByUserIdAndIdNotOrderByCreatedAtDesc(userId, excludedAddressId)
			.ifPresent(Address::markDefault);
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
		Set<Integer> usedNumbers = new HashSet<>();

		for (AddressLabelProjection address : userAddressLabels) {
			String label = normalizeLabel(address.getLabel());
			if (label == null) {
				continue;
			}

			Matcher matcher = AUTO_LABEL_PATTERN.matcher(label);
			if (matcher.matches()) {
				usedNumbers.add(Integer.parseInt(matcher.group(1)));
			}
		}

		int nextNumber = 1;
		while (usedNumbers.contains(nextNumber)) {
			nextNumber++;
		}

		return AUTO_LABEL_PREFIX + nextNumber;
	}

	private String normalizeLabel(String label) {
		if (label == null) {
			return null;
		}

		String trimmedLabel = label.trim();
		if (trimmedLabel.isEmpty()) {
			return null;
		}

		return trimmedLabel;
	}
}
