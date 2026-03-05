package com.kt.onrace.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.onrace.auth.entity.Terms;

public interface TermsRepository extends JpaRepository<Terms, Long> {
}
