package com.onrace.api.interceptor;

import com.onrace.api.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TpsLimiterInterceptor implements HandlerInterceptor {

    @Autowired
    private RateLimiterService rateLimiterService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 헤더에서 그룹 ID 추출 (기본값 g1)
        String groupId = request.getHeader("X-Group-ID");
        if (groupId == null || groupId.isEmpty()) {
            groupId = "g1";
        }

        // 그룹별 제한량 (예: 50 TPS)
        int limit = 50; 

        if (!rateLimiterService.isAllowed(groupId, limit)) {
            // 차단 시 429 에러와 메시지 반환
            response.setStatus(429);
            response.setContentType("text/plain; charset=UTF-8");
            response.getWriter().write("접속 인원이 많습니다. 잠시 후 다시 시도해주세요.");
            return false; 
        }

        return true; 
    }
}