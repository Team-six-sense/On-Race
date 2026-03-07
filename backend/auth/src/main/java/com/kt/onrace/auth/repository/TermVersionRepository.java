package com.kt.onrace.auth.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.kt.onrace.auth.entity.TermVersion;

public interface TermVersionRepository extends JpaRepository<TermVersion, Long> {

	@Query("SELECT tv FROM TermVersion tv JOIN FETCH tv.termMaster WHERE tv.active = true")
	List<TermVersion> findAllActiveWithMaster();
}
