package com.kt.onrace.common.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import io.micrometer.context.ContextRegistry;
import io.micrometer.context.ContextSnapshot;
import lombok.RequiredArgsConstructor;

@EnableAsync
@Configuration
@RequiredArgsConstructor
public class AsyncConfig {

	private final ContextRegistry contextRegistry;

	@Bean(name = "taskExecutor")
	public Executor taskExecutor() {

		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

		executor.setCorePoolSize(10); // 기본 스레드 수
		executor.setMaxPoolSize(50); // 최대 스레드 수
		executor.setQueueCapacity(100); // 큐 크기
		executor.setThreadNamePrefix("Async-"); // 스레드 이름

		executor.setWaitForTasksToCompleteOnShutdown(true);
		executor.setAwaitTerminationSeconds(30);

		// Micrometer 적용
		executor.setTaskDecorator(runnable -> {
			// Map<String, String> contextMap = MDC.getCopyOfContextMap(); -> 마이크로미터 사용 변경

			if (contextRegistry != null) {
				// Micrometer 방식: 모든 Context 캡처
				ContextSnapshot snapshot = ContextSnapshot.captureAll(contextRegistry);

				return () -> {
					try (ContextSnapshot.Scope scope = snapshot.setThreadLocals()) {
						runnable.run();
					}
				};
			} else {
				return runnable;
			}
		});

		executor.initialize();
		return executor;
	}

}
