package com.kt.onrace.auth.dto;

import com.kt.onrace.auth.entity.TermVersion;

public record TermDetailResponse(
	Long termVersionId,
	String termName,
	boolean required,
	String version,
	String content
) {

	public static TermDetailResponse from(TermVersion tv) {
		return new TermDetailResponse(
			tv.getId(),
			tv.getTermMaster().getName(),
			tv.getTermMaster().isRequired(),
			tv.getVersion(),
			tv.getContent()
		);
	}
}
