package com.kt.onrace.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.onrace.auth.entity.TermUser;

public interface TermUserRepository extends JpaRepository<TermUser, Long> {
}
