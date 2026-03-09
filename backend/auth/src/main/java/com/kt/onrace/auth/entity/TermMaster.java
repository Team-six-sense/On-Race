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
@Table(name = "term_master")
public class TermMaster extends BaseEntity {

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false)
	private boolean required;

	private TermMaster(String name, boolean required) {
		this.name = name;
		this.required = required;
	}

	public static TermMaster create(String name, boolean required) {
		return new TermMaster(name, required);
	}
}
