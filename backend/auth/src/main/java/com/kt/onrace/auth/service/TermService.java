package com.kt.onrace.auth.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kt.onrace.auth.dto.TermResponse;
import com.kt.onrace.auth.repository.TermVersionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermService {

	private final TermVersionRepository termVersionRepository;

	public List<TermResponse> getActiveTerms() {
		return termVersionRepository.findAllActiveWithMaster()
			.stream()
			.map(TermResponse::from)
			.toList();
	}
}
