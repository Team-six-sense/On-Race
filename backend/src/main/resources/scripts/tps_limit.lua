@Configuration
public class RedisScriptConfig {

    @Bean
    public RedisScript<Long> tpsLimitScript() {
        Resource scriptSource = new ClassPathResource("scripts/tps_limit.lua");
        return RedisScript.of(scriptSource, Long.class);
    }
}

@Component
public class TpsLimiterInterceptor implements HandlerInterceptor {

    @Autowired
    private RateLimiterService rateLimiterService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // [예시] 헤더나 파라미터에서 그룹 ID를 가져옴 (없으면 기본값 g1)
        String groupId = request.getHeader("X-Group-ID");
        if (groupId == null) groupId = "g1";

        // 그룹별 제한량 설정 (나중엔 DB나 Config에서 가져오게 고도화 가능)
        int limit = 50; 

        if (!rateLimiterService.isAllowed(groupId, limit)) {
            // [차단] 429 Too Many Requests 에러 반환
            response.setStatus(429);
            response.getWriter().write("Too many requests. Please try again later.");
            return false; 
        }

        return true; // [통과]
    }
}

@Service
public class RateLimiterService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedisScript<Long> tpsLimitScript;

    public boolean isAllowed(String groupId, int maxTps) {
        // 1. 현재 초 단위 타임스탬프를 키로 사용 (예: tps:g1:1710931400)
        String key = "tps:" + groupId + ":" + (System.currentTimeMillis() / 1000);
        
        try {
            // 2. Lua 스크립트 실행 (KEYS, ARGV 전달)
            // KEYS[1] = key, ARGV[1] = maxTps, ARGV[2] = 만료시간(2초)
            Long result = redisTemplate.execute(tpsLimitScript, Collections.singletonList(key), 
                                                String.valueOf(maxTps), "2");
            
            return result != null && result == 1L;
        } catch (Exception e) {
            // [중요] Fail-Open: Redis가 죽어도 서비스는 돌아가게 'true' 반환
            log.error("Redis 에러 발생! 유입 제어 일시 해제", e);
            return true; 
        }
    }
}

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private TpsLimiterInterceptor tpsLimiterInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tpsLimiterInterceptor)
                .addPathPatterns("/api/v1/tickets/**") // [중요] 제어가 필요한 API 경로만 지정
                .excludePathPatterns("/api/v1/health"); // 제외할 경로 (옵션)
    }
}