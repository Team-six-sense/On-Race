package com.kt.onrace.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kt.onrace.auth.entity.TermMaster;

public interface TermMasterRepository extends JpaRepository<TermMaster, Long> {

	boolean existsByName(String name);
}
