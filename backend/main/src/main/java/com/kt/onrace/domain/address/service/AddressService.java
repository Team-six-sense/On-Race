package com.kt.onrace.domain.address.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.dao.DataIntegrityViolationException;
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
/**
 * Address는 사용자의 활성 배송지 목록을 관리한다.
 * 라벨 정책, 기본배송지 단일성, soft delete, 주문에 넘길 기본 배송지 조회를 이 서비스가 맡는다.
 */
public class AddressService {

	private static final int MAX_ADDRESS_COUNT = 10;
	private static final int MAX_RECEIVER_NAME_LENGTH = 50;
	private static final int MAX_ZIPCODE_LENGTH = 10;
	private static final int MAX_ADDRESS_LENGTH = 255;
	private static final int MAX_MEMO_LENGTH = 255;
	private static final String AUTO_LABEL_PREFIX = "배송지";
	private static final Pattern LABEL_PATTERN = Pattern.compile("^[가-힣A-Za-z0-9 ]{1,20}$");
	private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10,11}$");
	private static final String DUPLICATE_LABEL_CONSTRAINT = "uk_address_user_normalized_label";
	private static final String ACTIVE_DEFAULT_CONSTRAINT = "uk_address_active_default_owner";

	private final AddressRepository addressRepository;

	public List<AddressDto.Response> list(Long userId) {
		return addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
			.stream()
			.map(AddressDto.Response::from)
			.toList();
	}

	@Transactional
	public AddressDto.DefaultResponse getDefault(Long userId) {
		Optional<Address> defaultAddress = addressRepository.findFirstByUserIdAndIsDefaultTrue(userId);

		if (defaultAddress.isPresent()) {
			return AddressDto.DefaultResponse.from(defaultAddress.get());
		}

		List<Address> activeAddresses = lockActiveAddresses(userId);
		if (activeAddresses.isEmpty()) {
			return AddressDto.DefaultResponse.empty();
		}

		Address latestAddress = activeAddresses.get(0);
		activeAddresses.stream()
			.filter(address -> !address.getId().equals(latestAddress.getId()))
			.filter(Address::isDefault)
			.forEach(Address::unmarkDefault);
		latestAddress.markDefault();
		flushWithConstraintTranslation();

		return AddressDto.DefaultResponse.from(latestAddress);
	}

	public AddressDto.Response get(Long userId, Long addressId) {
		Address address = findOwnedAddressOrThrow(userId, addressId);

		return AddressDto.Response.from(address);
	}

	@Transactional
	public AddressDto.Response create(Long userId, AddressDto.SaveRequest request) {
		List<Address> activeAddresses = lockActiveAddresses(userId);
		long addressCount = activeAddresses.size();
		if (addressCount >= MAX_ADDRESS_COUNT) {
			throw new BusinessException(BusinessErrorCode.ADDRESS_LIMIT_EXCEEDED);
		}

		validateRequest(request);

		boolean hasAny = addressCount > 0;
		boolean hasDefault = findCurrentDefault(activeAddresses).isPresent();
		String label = resolveLabelForCreate(userId, request.label());

		boolean shouldBeDefault = !hasAny || !hasDefault || Boolean.TRUE.equals(request.isDefault());

		if (shouldBeDefault && hasDefault) {
			unsetDefaultIfExists(activeAddresses);
			flushWithConstraintTranslation();
		}

		Address address = Address.builder()
			.userId(userId)
			.label(label)
			.normalizedLabel(normalizeLabelForComparison(label))
			.receiverName(request.receiverName())
			.phone(request.phone())
			.zipcode(request.zipcode())
			.address1(request.address1())
			.address2(request.address2())
			.memo(request.memo())
			.isDefault(shouldBeDefault)
			.activeDefaultOwnerId(shouldBeDefault ? userId : null)
			.build();

		return AddressDto.Response.from(saveWithConstraintTranslation(address));
	}

	@Transactional
	public AddressDto.Response update(Long userId, Long addressId, AddressDto.SaveRequest request) {
		List<Address> activeAddresses = lockActiveAddresses(userId);
		Address address = findOwnedAddressOrThrow(activeAddresses, addressId);
		validateRequest(request);
		String label = resolveLabelForUpdate(userId, addressId, address.getLabel(), request.label());

		Boolean wantDefault = request.isDefault();

		if (Boolean.TRUE.equals(wantDefault) && !address.isDefault()) {
			unsetDefaultIfExists(activeAddresses);
			flushWithConstraintTranslation();
			address.markDefault();
		} else if (Boolean.FALSE.equals(wantDefault) && address.isDefault()) {
			Address replacement = findReplacementDefault(activeAddresses, addressId)
				.orElseThrow(() -> new BusinessException(BusinessErrorCode.ADDRESS_DEFAULT_CONFLICT));
			address.unmarkDefault();
			flushWithConstraintTranslation();
			replacement.markDefault();
		}

		address.update(
			label,
			normalizeLabelForComparison(label),
			request.receiverName(),
			request.phone(),
			request.zipcode(),
			request.address1(),
			request.address2(),
			request.memo()
		);

		flushWithConstraintTranslation();
		return AddressDto.Response.from(address);
	}

	@Transactional
	public void delete(Long userId, Long addressId) {
		List<Address> activeAddresses = lockActiveAddresses(userId);
		Address address = findOwnedAddressOrThrow(activeAddresses, addressId);

		boolean wasDefault = address.isDefault();

		address.softDelete();
		flushWithConstraintTranslation();

		if (wasDefault) {
			findReplacementDefault(activeAddresses, addressId)
				.ifPresent(Address::markDefault);
			flushWithConstraintTranslation();
		}
	}

	@Transactional
	public void setDefault(Long userId, Long addressId) {
		List<Address> activeAddresses = lockActiveAddresses(userId);
		Address address = findOwnedAddressOrThrow(activeAddresses, addressId);

		if (address.isDefault()) {
			return;
		}

		unsetDefaultIfExists(activeAddresses);
		flushWithConstraintTranslation();
		address.markDefault();
		flushWithConstraintTranslation();
	}

	private List<Address> lockActiveAddresses(Long userId) {
		return addressRepository.findByUserIdOrderByCreatedAtDescForUpdate(userId);
	}

	private Address findOwnedAddressOrThrow(Long userId, Long addressId) {
		return addressRepository.findByIdAndUserId(addressId, userId)
			.orElseThrow(() -> new BusinessException(BusinessErrorCode.ADDRESS_NOT_FOUND));
	}

	private Address findOwnedAddressOrThrow(List<Address> activeAddresses, Long addressId) {
		return activeAddresses.stream()
			.filter(address -> address.getId().equals(addressId))
			.findFirst()
			.orElseThrow(() -> new BusinessException(BusinessErrorCode.ADDRESS_NOT_FOUND));
	}

	private Optional<Address> findCurrentDefault(List<Address> activeAddresses) {
		return activeAddresses.stream()
			.filter(Address::isDefault)
			.findFirst();
	}

	private Optional<Address> findReplacementDefault(List<Address> activeAddresses, Long excludedAddressId) {
		return activeAddresses.stream()
			.filter(address -> !address.getId().equals(excludedAddressId))
			.findFirst();
	}

	private void unsetDefaultIfExists(List<Address> activeAddresses) {
		findCurrentDefault(activeAddresses)
			.ifPresent(Address::unmarkDefault);
	}

	private String resolveLabelForCreate(Long userId, String rawLabel) {
		String normalizedLabel = normalizeLabel(rawLabel);
		if (normalizedLabel == null) {
			return generateAutoLabel(userId);
		}

		validateLabelFormat(normalizedLabel);
		validateDuplicateLabel(userId, normalizedLabel, null);
		return normalizedLabel;
	}

	private String resolveLabelForUpdate(Long userId, Long addressId, String currentLabel, String rawLabel) {
		String normalizedLabel = normalizeLabel(rawLabel);
		if (normalizedLabel == null) {
			String existingLabel = normalizeLabel(currentLabel);
			if (existingLabel != null) {
				return existingLabel;
			}
			return generateAutoLabel(userId);
		}

		validateLabelFormat(normalizedLabel);

		String existingLabel = normalizeLabel(currentLabel);
		if (existingLabel != null && normalizeLabelForComparison(existingLabel).equals(normalizeLabelForComparison(normalizedLabel))) {
			return normalizedLabel;
		}

		validateDuplicateLabel(userId, normalizedLabel, addressId);
		return normalizedLabel;
	}

	private void validateLabelFormat(String label) {
		if (!LABEL_PATTERN.matcher(label).matches()) {
			throw new BusinessException(BusinessErrorCode.ADDRESS_INVALID_LABEL);
		}
	}

	private void validateRequest(AddressDto.SaveRequest request) {
		validateRequiredText(request.receiverName());
		validateRequiredText(request.zipcode());
		validateRequiredText(request.address1());
		validatePhone(request.phone());
		validateMaxLength(request.receiverName(), MAX_RECEIVER_NAME_LENGTH);
		validateMaxLength(request.zipcode(), MAX_ZIPCODE_LENGTH);
		validateMaxLength(request.address1(), MAX_ADDRESS_LENGTH);
		validateMaxLength(request.address2(), MAX_ADDRESS_LENGTH);
		validateMaxLength(request.memo(), MAX_MEMO_LENGTH);
	}

	private void validatePhone(String phone) {
		if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
			throw new BusinessException(BusinessErrorCode.ADDRESS_INVALID_PHONE);
		}
	}

	private void validateRequiredText(String value) {
		if (value == null || value.isBlank()) {
			throw new BusinessException(BusinessErrorCode.COMMON_INVALID_PARAMETER);
		}
	}

	private void validateMaxLength(String value, int maxLength) {
		if (value != null && value.length() > maxLength) {
			throw new BusinessException(BusinessErrorCode.COMMON_INVALID_PARAMETER);
		}
	}

	private void validateDuplicateLabel(Long userId, String label, Long addressId) {
		String normalizedLabel = normalizeLabelForComparison(label);
		long duplicateCount = addressId == null
			? addressRepository.countByUserIdAndIsDeletedFalseAndNormalizedLabel(userId, normalizedLabel)
			: addressRepository.countByUserIdAndIdNotAndIsDeletedFalseAndNormalizedLabel(userId, addressId, normalizedLabel);

		if (duplicateCount > 0) {
			throw new BusinessException(BusinessErrorCode.ADDRESS_DUPLICATE_LABEL);
		}
	}

	private String generateAutoLabel(Long userId) {
		Set<String> existingLabels = addressRepository.findLabelsByUserId(userId).stream()
			.map(this::normalizeLabel)
			.filter(label -> label != null)
			.map(this::normalizeLabelForComparison)
			.collect(java.util.stream.Collectors.toSet());

		int index = 1;
		while (true) {
			String candidate = AUTO_LABEL_PREFIX + index;
			if (!existingLabels.contains(normalizeLabelForComparison(candidate))) {
				return candidate;
			}
			index++;
		}
	}

	private String normalizeLabel(String label) {
		if (label == null) {
			return null;
		}

		String trimmed = label.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private String normalizeLabelForComparison(String label) {
		return label.toLowerCase(Locale.ROOT);
	}

	private Address saveWithConstraintTranslation(Address address) {
		try {
			return addressRepository.saveAndFlush(address);
		} catch (DataIntegrityViolationException exception) {
			throw translateIntegrityViolation(exception);
		}
	}

	private void flushWithConstraintTranslation() {
		try {
			addressRepository.flush();
		} catch (DataIntegrityViolationException exception) {
			throw translateIntegrityViolation(exception);
		}
	}

	private BusinessException translateIntegrityViolation(DataIntegrityViolationException exception) {
		String message = exception.getMostSpecificCause() != null
			? exception.getMostSpecificCause().getMessage()
			: exception.getMessage();

		if (message != null) {
			String lowerMessage = message.toLowerCase(Locale.ROOT);
			if (lowerMessage.contains(DUPLICATE_LABEL_CONSTRAINT.toLowerCase(Locale.ROOT))) {
				return new BusinessException(BusinessErrorCode.ADDRESS_DUPLICATE_LABEL);
			}
			if (lowerMessage.contains(ACTIVE_DEFAULT_CONSTRAINT.toLowerCase(Locale.ROOT))) {
				return new BusinessException(BusinessErrorCode.ADDRESS_DEFAULT_CONFLICT);
			}
		}

		throw exception;
	}
}
