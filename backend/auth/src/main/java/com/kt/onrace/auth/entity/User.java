package com.kt.onrace.auth.entity;

import static com.kt.onrace.auth.entity.Role.*;

import com.kt.onrace.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

	@Column(nullable = false, length = 100)
	private String email;

	@Column(nullable = false, length = 50)
	private String name;

	@Column(length = 20)
	private String phoneNumber;

	@Column(length = 20)
	private String mobile;

	@Column(length = 255)
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AuthProvider authProvider;

	@Column(length = 100)
	private String providerId;

	@Column(nullable = false)
	private boolean isDeleted;

	private User(String email, String name, String password, String phoneNumber) {
		this.email = email;
		this.name = name;
		this.password = password;
		this.phoneNumber = phoneNumber;
		this.authProvider = AuthProvider.LOCAL;
		this.role = USER;
		this.isDeleted = false;
	}

	private User(String email, String name, AuthProvider authProvider, String providerId) {
		this.email = email;
		this.name = name;
		this.authProvider = authProvider;
		this.providerId = providerId;
		this.role = USER;
		this.isDeleted = false;
	}

	public static User createUser(String email, String name, String password, String phoneNumber) {
		return new User(email, name, password, phoneNumber);
	}

	public static User createOAuthUser(String email, String name, AuthProvider authProvider, String providerId) {
		return new User(email, name, authProvider, providerId);
	}

	public void markDeleted() {
		this.isDeleted = true;
	}

	public void changePassword(String encodedPassword) {
		this.password = encodedPassword;
	}

}
