package com.onrace.api.config;

import com.onrace.api.interceptor.TpsLimiterInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private TpsLimiterInterceptor tpsLimiterInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tpsLimiterInterceptor)
                .addPathPatterns("/api/v1/tickets/**") // 티켓 예매 API 주소
                .excludePathPatterns("/api/v1/tickets/health"); // 헬스체크는 제외
    }
}