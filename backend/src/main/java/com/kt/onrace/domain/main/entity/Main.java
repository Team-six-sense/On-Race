package com.kt.onrace.domain.main.entity;

import com.kt.onrace.common.entity.BaseEntity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class Main extends BaseEntity {

	private String name;
	private String age;
	private String message;
}
