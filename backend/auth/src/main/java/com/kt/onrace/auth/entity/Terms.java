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
	private boolean isAgreed1;

	@Column(nullable = false)
	private boolean isAgreed2;

	@Column(nullable = false)
	private boolean isAgreed3;

	@Column(nullable = false)
	private boolean isAgreed4;

	@Column(nullable = false, length = 10)
	private String termVersion;

	private Terms(Long userId, boolean isAgreed1, boolean isAgreed2, boolean isAgreed3, boolean isAgreed4,
			String termVersion) {
		this.userId = userId;
		this.isAgreed1 = isAgreed1;
		this.isAgreed2 = isAgreed2;
		this.isAgreed3 = isAgreed3;
		this.isAgreed4 = isAgreed4;
		this.termVersion = termVersion;
	}

	public static Terms create(Long userId, boolean isAgreed1, boolean isAgreed2, boolean isAgreed3, boolean isAgreed4,
			String termVersion) {
		return new Terms(userId, isAgreed1, isAgreed2, isAgreed3, isAgreed4, termVersion);
	}
}
