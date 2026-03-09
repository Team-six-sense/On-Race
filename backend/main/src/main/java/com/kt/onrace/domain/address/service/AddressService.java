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
		List<AddressLabelProjection> userAddressLabels = addressRepository.findLabelProjectionsByUserId(userId);
		boolean hasAny = !userAddressLabels.isEmpty();
		String resolvedLabel = resolveCreateLabel(userAddressLabels, request.label());

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
		List<AddressLabelProjection> userAddressLabels = addressRepository.findLabelProjectionsByUserId(userId);
		String resolvedLabel = resolveUpdateLabel(address, userAddressLabels, request.label());

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

	private String resolveCreateLabel(List<AddressLabelProjection> userAddressLabels, String requestedLabel) {
		String normalizedLabel = normalizeLabel(requestedLabel);
		if (normalizedLabel == null) {
			return generateAutoLabel(userAddressLabels);
		}

		validateDuplicateLabel(userAddressLabels, normalizedLabel, null);
		return normalizedLabel;
	}

	private String resolveUpdateLabel(Address address, List<AddressLabelProjection> userAddressLabels, String requestedLabel) {
		String normalizedLabel = normalizeLabel(requestedLabel);
		if (normalizedLabel == null) {
			return address.getLabel();
		}

		validateDuplicateLabel(userAddressLabels, normalizedLabel, address.getId());
		return normalizedLabel;
	}

	private void validateDuplicateLabel(List<AddressLabelProjection> userAddressLabels, String label, Long excludedAddressId) {
		String normalizedForComparison = label.toLowerCase(Locale.ROOT);

		boolean duplicated = userAddressLabels.stream()
			.filter(address -> excludedAddressId == null || !address.getId().equals(excludedAddressId))
			.anyMatch(address -> normalizedForComparison.equals(normalizeLabelForComparison(address.getLabel())));

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

	private String normalizeLabelForComparison(String label) {
		String normalizedLabel = normalizeLabel(label);
		if (normalizedLabel == null) {
			return "";
		}

		return normalizedLabel.toLowerCase(Locale.ROOT);
	}
}
