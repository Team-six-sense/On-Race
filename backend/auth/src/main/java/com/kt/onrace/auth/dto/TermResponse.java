package com.kt.onrace.auth.dto;

import com.kt.onrace.auth.entity.TermVersion;

public record TermResponse(
	Long termVersionId,
	String termName,
	boolean required,
	String version,
	String content
) {

	public static TermResponse from(TermVersion tv) {
		return new TermResponse(
			tv.getId(),
			tv.getTermMaster().getName(),
			tv.getTermMaster().isRequired(),
			tv.getVersion(),
			tv.getContent()
		);
	}
}
