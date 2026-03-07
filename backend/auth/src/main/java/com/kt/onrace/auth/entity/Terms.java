package com.kt.onrace.auth.entity;

import com.kt.onrace.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "terms")
public class Terms extends BaseEntity {

	@Column(nullable = false)
	private Long userId;

	@Column(nullable = false)
	private boolean serviceTermsAgreed;

	@Column(nullable = false)
	private boolean privacyPolicyAgreed ;

	@Column(nullable = false)
	private boolean isAgreed3;

	@Column(nullable = false)
	private boolean isAgreed4;

	@Column(nullable = false, length = 10)
	private String termVersion;

	private Terms(Long userId, boolean serviceTermsAgreed, boolean privacyPolicyAgreed, boolean isAgreed3, boolean isAgreed4,
			String termVersion) {
		this.userId = userId;
		this.serviceTermsAgreed = serviceTermsAgreed;
		this.privacyPolicyAgreed = privacyPolicyAgreed;
		this.isAgreed3 = isAgreed3;
		this.isAgreed4 = isAgreed4;
		this.termVersion = termVersion;
	}

	public static Terms create(Long userId, boolean serviceTermsAgreed, boolean privacyPolicyAgreed, boolean isAgreed3, boolean isAgreed4,
			String termVersion) {
		return new Terms(userId, serviceTermsAgreed, privacyPolicyAgreed, isAgreed3, isAgreed4, termVersion);
	}
}
