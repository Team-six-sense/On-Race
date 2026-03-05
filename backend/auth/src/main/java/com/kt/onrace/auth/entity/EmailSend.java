package com.kt.onrace.auth.entity;

import com.kt.onrace.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "email_sends")
public class EmailSend extends BaseEntity {

	private Long userId;

	@Column(nullable = false, length = 100)
	private String email;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EmailSendType type;

	@Column(nullable = false)
	private boolean isSuccess;

	@Column(length = 255)
	private String failReason;

	private EmailSend(Long userId, String email, EmailSendType type, boolean isSuccess, String failReason) {
		this.userId = userId;
		this.email = email;
		this.type = type;
		this.isSuccess = isSuccess;
		this.failReason = failReason;
	}

	public static EmailSend success(String email, EmailSendType type) {
		return new EmailSend(null, email, type, true, null);
	}

	public static EmailSend fail(String email, EmailSendType type, String failReason) {
		return new EmailSend(null, email, type, false, failReason);
	}
}
