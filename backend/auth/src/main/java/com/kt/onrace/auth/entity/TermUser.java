package com.kt.onrace.auth.entity;

import java.time.LocalDateTime;

import com.kt.onrace.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "term_user")
public class TermUser extends BaseEntity {

	@Column(nullable = false)
	private Long userId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "term_version_id", nullable = false)
	private TermVersion termVersion;

	@Column(nullable = false)
	private boolean agreed;

	@Column(nullable = false)
	private LocalDateTime agreedAt;

	private TermUser(Long userId, TermVersion termVersion, boolean agreed, LocalDateTime agreedAt) {
		this.userId = userId;
		this.termVersion = termVersion;
		this.agreed = agreed;
		this.agreedAt = agreedAt;
	}

	public static TermUser create(Long userId, TermVersion termVersion, boolean agreed, LocalDateTime agreedAt) {
		return new TermUser(userId, termVersion, agreed, agreedAt);
	}
}
