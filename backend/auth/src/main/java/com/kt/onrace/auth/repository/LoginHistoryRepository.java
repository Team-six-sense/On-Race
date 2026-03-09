package com.kt.onrace.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.onrace.auth.entity.LoginHistory;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
}
