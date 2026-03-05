package com.kt.onrace.auth.entity;

import static com.kt.onrace.auth.entity.Role.*;

import com.kt.onrace.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class User extends BaseEntity {

	@Column(nullable = false, length = 100)
	private String email;

	@Column(nullable = false, length = 50)
	private String name;

	@Column(nullable = false, length = 20)
	private String phoneNumber;

	@Column(length = 20)
	private String mobile;

	@Column(nullable = false, length = 255)
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	@Column(nullable = false)
	private boolean isDeleted;

	private User(String email, String name, String password, String phoneNumber) {
		this.email = email;
		this.name = name;
		this.password = password;
		this.phoneNumber = phoneNumber;
		this.role = USER;
		this.isDeleted = false;
	}

	public static User createUser(String email, String name, String password, String phoneNumber) {
		return new User(email, name, password, phoneNumber);
	}

	public void markDeleted() {
		this.isDeleted = true;
	}

	public void changePassword(String encodedPassword) {
		this.password = encodedPassword;
	}

}
