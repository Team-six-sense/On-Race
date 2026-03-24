package com.kt.onrace.domain.mypage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kt.onrace.domain.member.entity.Member;
import com.kt.onrace.domain.member.repository.MemberRepository;
import com.kt.onrace.domain.mypage.client.AuthClient;
import com.kt.onrace.domain.mypage.dto.MyPageAccountResponse;

@ExtendWith(MockitoExtension.class)
class MyPageServiceTest {

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private MyPageQueryService myPageQueryService;

	@Mock
	private ApplicationHistoryService applicationHistoryService;

	@Mock
	private OrderHistoryService orderHistoryService;

	@Mock
	private AuthClient authClient;

	@InjectMocks
	private MyPageService myPageService;

	@Test
	@DisplayName("계정 조회는 회원 검증 후 auth 원천 데이터를 마이페이지 계약으로 변환한다")
	void getAccountReturnsMyPageAccountResponse() {
		Long userId = 7L;
		when(memberRepository.findByIdAndIsDeletedFalseOrThrow(org.mockito.ArgumentMatchers.eq(userId), org.mockito.ArgumentMatchers.any()))
			.thenReturn(Member.createMember(userId));
		when(authClient.getAccount(userId)).thenReturn(
			new AuthClient.AuthAccountResponse(
				userId,
				"user@test.com",
				"김유저",
				"01012345678",
				true,
				"COMPLETED",
				true
			)
		);

		MyPageAccountResponse response = myPageService.getAccount(userId);

		assertThat(response.name()).isEqualTo("김유저");
		assertThat(response.email()).isEqualTo("user@test.com");
		assertThat(response.phone()).isEqualTo("01012345678");
		assertThat(response.canChangePassword()).isTrue();
		assertThat(response.verificationStatus()).isEqualTo("COMPLETED");
		assertThat(response.marketingConsent()).isTrue();
		verify(authClient).getAccount(userId);
	}
}
