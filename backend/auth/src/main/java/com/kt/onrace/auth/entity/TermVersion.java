package com.kt.onrace.auth.entity;

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
@Table(name = "term_version")
public class TermVersion extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "term_master_id", nullable = false)
	private TermMaster termMaster;

	@Column(nullable = false, length = 10)
	private String version;

	@Column(columnDefinition = "TEXT")
	private String content;

	@Column(nullable = false)
	private boolean active;

	private TermVersion(TermMaster termMaster, String version, String content, boolean active) {
		this.termMaster = termMaster;
		this.version = version;
		this.content = content;
		this.active = active;
	}

	public static TermVersion create(TermMaster termMaster, String version, String content, boolean active) {
		return new TermVersion(termMaster, version, content, active);
	}
}
