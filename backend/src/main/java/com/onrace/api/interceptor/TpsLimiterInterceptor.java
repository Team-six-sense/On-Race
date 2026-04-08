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
        String groupId = request.getHeader("X-Group-ID");
        if (groupId == null || groupId.isEmpty()) {
            groupId = "g1";
        }

        if (!rateLimiterService.isAllowed(groupId)) {
            // [수정 3] 프론트엔드 호환성을 위한 JSON 응답 처리
            response.setStatus(429);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"error\": \"Too Many Requests\", \"message\": \"접속 인원이 많습니다. 잠시 후 다시 시도해주세요.\"}");
            return false;
        }

        return true;
    }
}