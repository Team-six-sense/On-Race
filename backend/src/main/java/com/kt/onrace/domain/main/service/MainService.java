package com.kt.onrace.domain.main.service;

import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.kt.onrace.common.logging.annotation.ServiceLog;
import com.kt.onrace.domain.main.domain.Main;
import com.kt.onrace.domain.main.dto.TestRequest;
import com.kt.onrace.domain.main.repository.MainRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MainService {

	private final MainRepository mainRepository;

	@ServiceLog(slowMs = 4000)
	public List<Main> getSyncTestMessage(String message, TestRequest request, int number) {
		List<Main> main = mainRepository.findAll();

		try {
			log.info("이것도?");
			Thread.sleep(5000);

		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		//throw new CustomException(ErrorCode.COMMON_INVALID_REQUEST);
		return main;
	}

	@Async
	@ServiceLog(slowMs = 4000)
	public void getAsyncTestMessage(String message, TestRequest request, int number) {
		try {
			log.info("비동기 작업 시작");
			Thread.sleep(5000);
			log.info("비동기 작업 완료");
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
