-- tps_limit.lua
local key = KEYS[1]
local limit = tonumber(ARGV[1])
local expire_time = tonumber(ARGV[2])

-- 1. 해당 키의 값을 원자적으로 증가
local current = redis.call("INCR", key)

-- 2. 최초 호출 시(값이 1일 때) 또는 예기치 않게 TTL이 설정되지 않은 경우 만료 시간 설정
-- (고부하 환경에서 EXPIRE 명령이 누락되는 것을 방지하는 방어 로직)
if current == 1 or redis.call("TTL", key) == -1 then
    redis.call("EXPIRE", key, expire_time)
end

-- 3. 현재 값이 제한(limit)을 초과했는지 판별
if current > limit then
    return 0 -- 차단 (False)
end

return 1 -- 허용 (True)