package com.kt.onrace.domain.auth.domain;

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
	//TODO: 추후 도메인 내용은 변경

	@Column(nullable = false, length = 30)
	private String loginId;

	@Column(nullable = false, length = 20)
	private String name;

	@Column(nullable = false, length = 256)
	private String password;

	@Column(nullable = false, length = 30)
	private String email;

	@Column(nullable = false, length = 13)
	private String mobile;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Gender gender;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	@Column(nullable = false)
	private boolean isDeleted;

	private User(String loginId, String name, String password, String email, String mobile, Gender gender, Role role) {
		this.loginId = loginId;
		this.name = name;
		this.password = password;
		this.email = email;
		this.mobile = mobile;
		this.gender = gender;
		this.role = role;
		this.isDeleted = false;
	}

	public static User createUser(String loginId, String name, String password, String email, String mobile, Gender gender) {
		return new User(loginId, name, password, email, mobile, gender, Role.USER);
	}

	public static User createAdmin(String loginId, String name, String password, String email, String mobile, Gender gender) {
		return new User(loginId, name, password, email, mobile, gender, Role.ADMIN);
	}
}
