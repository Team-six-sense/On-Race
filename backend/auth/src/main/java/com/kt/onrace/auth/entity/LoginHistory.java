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
@Table(name = "login_histories")
public class LoginHistory extends BaseEntity {

	private Long userId;

	@Column(length = 45)
	private String loginIp;

	@Column(length = 512)
	private String loginAgent;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private LoginMethod loginMethod;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AuthProvider authProvider;

	@Column(nullable = false)
	private boolean isSuccess;

	@Column(length = 255)
	private String failReason;

	private LoginHistory(Long userId, String loginIp, String loginAgent, LoginMethod loginMethod,
			AuthProvider authProvider, boolean isSuccess, String failReason) {
		this.userId = userId;
		this.loginIp = loginIp;
		this.loginAgent = loginAgent;
		this.loginMethod = loginMethod;
		this.authProvider = authProvider;
		this.isSuccess = isSuccess;
		this.failReason = failReason;
	}

	public static LoginHistory success(Long userId, String loginIp, String loginAgent) {
		return new LoginHistory(userId, loginIp, loginAgent, LoginMethod.EMAIL, AuthProvider.LOCAL, true, null);
	}

	public static LoginHistory fail(String loginIp, String loginAgent, String failReason) {
		return new LoginHistory(null, loginIp, loginAgent, LoginMethod.EMAIL, AuthProvider.LOCAL, false, failReason);
	}
}
