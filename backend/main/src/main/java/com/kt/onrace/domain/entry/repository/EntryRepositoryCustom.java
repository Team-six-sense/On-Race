package com.kt.onrace.domain.entry.repository;

import com.kt.onrace.domain.entry.dto.EntryCountResult;

public interface EntryRepositoryCustom {

	EntryCountResult countTotalAndAppliedByPaceId(Long paceId);
}
