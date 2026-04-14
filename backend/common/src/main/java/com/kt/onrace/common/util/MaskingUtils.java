package com.kt.onrace.common.util;

public class MaskingUtils {

	public static String mask(String value, MaskingType type) {
		return switch (type) {
			case EMAIL -> maskEmail(value);
			case PHONE -> maskPhone(value);
			case FULL -> "*".repeat(Math.min(value.length(), 10));
		};
	}

	private static String maskEmail(String email) {
		int atIndex = email.indexOf('@');
		if (atIndex < 1) {
			return "***@***.***";
		}
		if (atIndex <= 2) {
			return "*".repeat(atIndex) + email.substring(atIndex);
		}
		return email.substring(0, 2) + "*".repeat(atIndex - 2) + email.substring(atIndex);
	}

	private static String maskPhone(String phone) {
		// 숫자만 추출
		String digits = phone.replaceAll("[^0-9]", "");
		if (digits.length() < 7) {
			return "*".repeat(phone.length());
		}
		// 앞 3자리, 중간 마스킹, 뒤 4자리 유지 (예: 010****5678)
		int tailLength = 4;
		int headLength = digits.length() - tailLength - 4;
		if (headLength < 0) headLength = 0;
		int maskLength = digits.length() - headLength - tailLength;
		return digits.substring(0, headLength)
				+ "*".repeat(maskLength)
				+ digits.substring(digits.length() - tailLength);
	}
}
