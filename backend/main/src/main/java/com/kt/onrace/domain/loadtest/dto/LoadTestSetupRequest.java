package com.kt.onrace.domain.loadtest.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public record LoadTestSetupRequest(
	@Pattern(regexp = "LOTTERY|FIRST_COME_NO_QUEUE|FIRST_COME_WITH_QUEUE",
		message = "시나리오 타입은 LOTTERY, FIRST_COME_NO_QUEUE, FIRST_COME_WITH_QUEUE 중 하나여야 합니다")
	String scenarioType, // null이면 전체 이벤트 생성 (하위 호환)

	@Min(value = 10, message = "총 사용자 수는 10 이상이어야 합니다")
	@Max(value = 30000, message = "총 사용자 수는 30000 이하여야 합니다")
	int totalUsers,

	@Min(value = 1, message = "경쟁률은 1 이상이어야 합니다")
	@Max(value = 10, message = "경쟁률은 10 이하여야 합니다")
	int competitionRatio,

	@Min(value = 1, message = "인기 페이스 수는 1 이상이어야 합니다")
	@Max(value = 3, message = "인기 페이스 수는 3 이하여야 합니다")
	int hotPaceCount,

	@DecimalMin(value = "0.01", message = "인기 페이스 재고 비율은 0.01 이상이어야 합니다")
	@DecimalMax(value = "0.8", message = "인기 페이스 재고 비율은 0.8 이하여야 합니다")
	double hotStockRatio,

	@Min(value = 15, message = "총 재고는 15(페이스 수) 이상이어야 합니다")
	Integer totalStock, // null이면 totalUsers/competitionRatio 기반 자동 계산

	@Min(value = 0, message = "인기 코스 인덱스는 0 이상이어야 합니다")
	@Max(value = 2, message = "인기 코스 인덱스는 2 이하여야 합니다 (0=풀코스, 1=하프코스, 2=10km)")
	Integer hotCourseIndex, // null이면 기존 랜덤 선정

	@Min(value = 0, message = "인기 페이스 인덱스는 0 이상이어야 합니다")
	@Max(value = 4, message = "인기 페이스 인덱스는 4 이하여야 합니다 (0=3분, 1=4분, 2=5분, 3=6분, 4=7분)")
	Integer hotPaceIndex, // hotCourseIndex와 함께 지정 → 해당 코스-페이스 1개를 hot으로 고정

	@DecimalMin(value = "0.0", message = "사전정보 저장 비율은 0.0 이상이어야 합니다")
	@DecimalMax(value = "1.0", message = "사전정보 저장 비율은 1.0 이하여야 합니다")
	Double preSaveRatio // null이면 사전저장 건너뜀, 0.7이면 70% 유저 사전저장
) {
}
