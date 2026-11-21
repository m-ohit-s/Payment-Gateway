package com.myspace.razorpayclone.common.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rate-limit.method", havingValue = "sliding-lua")
public class SlidingWindowLuaLimiter implements RateLimiter{
    /**
     * Atomic sliding window via Lua
     * KEYS[1] = redis key
     * ARGS[1] = now in ms
     * ARGS[2] = window floor in ms (now - windowMs)
     * ARGS[3] = max requests
     * ARGS[4] = window ttl in seconds (for EXPIRE)
     * ARGS[5] = unique member id for this request
     *
     * Return a three element array:
     * [0] = 1 (allowed) or 0 (denied)
     * [1] = remaining count (maxRequest - count after add, or 0 if denied)
     * [2] = score of the oldest member (used to compute Retry-After), or 0 if denied on empty set
    */

    private static final String SLIDING_LUA = """
            Local key = KEYS[1]
            Local now = tonumber(ARGV[1])
            Local floor = tonumber(ARGV[2])
            Local limit = tonumber(ARGV[3])
            Local ttl = tonumber(ARGV[4])
            Local member = ARGV[5]
            
            -- prune expired members
            redis.call('ZREMRANGEBYSCORE', key, '-inf', floor)
            
            -- count current members
            local count = redis.call('ZCARD', key)
            
            if count >= limit then
                -- return oldest member score so caller can compute Retry-After
                local oldest = redis.call('ZRANGE', key, 0, 0, 'WITHSCORES')
                local oldestScore = 0
                if oldest > 0 then
                    oldestScore = tonumber(oldest[2])
                end
                return {0, 0, oldestScore}
            end
            
            -- add this request and reset TTL automatically
            redis.call('ZADD', key, now, member)
            redis.call('EXPIRE', key, ttl)
            
            local remaining = limit - count - 1
            return {1, remaining, 0}
            """;

    private final RedisScript<List<Long>> script = RedisScript.of(SLIDING_LUA, (Class<List<Long>>) (Class<?>) List.class);
    private final StringRedisTemplate redis;

    @Override
    public RateLimitResult check(String key, int maxRequestAllowed, long windowSeconds) {

        try {
            long now = System.currentTimeMillis();
            long floorMs = now - (windowSeconds * 1000);
            String member = UUID.randomUUID().toString();

            List<Long> result = redis.execute(
                    script,
                    List.of("ratelimit:sliding:" + key),
                    String.valueOf(now),
                    String.valueOf(floorMs),
                    String.valueOf(maxRequestAllowed),
                    String.valueOf(windowSeconds + 1),
                    member
            );

            if (result == null || result.isEmpty()) {
                return RateLimitResult.allowed(maxRequestAllowed);
            }

            boolean allowed = result.getFirst() == 1L;
            int remaining = result.get(1).intValue();
            long oldestScoreMs = result.get(2);

            if (!allowed) {
                int retryAfter = oldestScoreMs > 0 ?
                        (int) Math.max(1, (oldestScoreMs + windowSeconds * 1000 - now) / 1000) :
                        (int) windowSeconds;
                return RateLimitResult.denied(retryAfter);
            }
            return RateLimitResult.allowed(remaining);
        } catch (DataAccessException e) {
            log.warn("Rate limiter unavailable, failing open for key = {}", key, e);
            return RateLimitResult.allowed(maxRequestAllowed);
        }
    }
}
