package com.myspace.razorpayclone.common.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Token bucket rate limiter: capacity == maxRequests, refill rate == maxRequests / windowSeconds
 * tokens / sec. Unlike fixed window, token trickle back continuously instead of resetting all at once
 * at the window boundary, so it doesn't allow a burst of 2x maxRequests around the edge of two windows.
 * State (token remaining, last refill timestamp) lives in a Redis hash; refill + consume is done atomically
 * in a single Lua script so two concurrent requests for the same key can't both read the same "tokens available"
 * snapshot and both be allowed through.
 *
 **/

@Component
@Slf4j
@ConditionalOnProperty(name = "app.rate-limit.method", havingValue = "bucket-lua")
@RequiredArgsConstructor
public class TokenBucketLuaRateLimiter implements RateLimiter {

    private static final RedisScript<List<Long>> SCRIPT = RedisScript.of("""
            local key = KEYS[1]
            local capacity = tonumber(ARGV[1])
            local refillPerSec = tonumber(ARGV[2])
            local nowMs = tonumber(ARGV[3])
            local ttlSeconds = tonumber(ARGV[4])
            
            local data = redis.call('HMGET', key, 'tokens', 'ts')
            local tokens = tonumber(data[1])
            local lastTs = tonumber(data[2])
            
            if tokens == nil then
                tokens = capacity
                lastTs = nowMs
            end
            local elapsedSec = math.max(0, (nowMs-lastTs) / 1000)
            tokens = math.min(capacity, tokens + (elapsedSec * refillPerSec))
            
            local allowed = 0
            if tokens >= 1 then
                tokens = tokens-1
                allowed = 1
            end
            redis.call('HMSET', key, 'tokens', tokens, 'ts', nowMs)
            redis.call('EXPIRE', key, ttlSeconds)
            
            local retryAfter = 0
            if allowed == 0 then
                retryAfter = math.ceil((1-tokens)/refillPerSec)
            end
            
            return {allowed, math.floor(tokens), retryAfter}
            """);

    private final StringRedisTemplate redis;

    @Override
    public RateLimitResult check(String key, int maxRequestAllowed, long windowSeconds) {
        String redisKey = "ratelimit:bucket:" + key;
        double refillRate = (double) maxRequestAllowed /windowSeconds;

        // Keep bucket state around for 2 windows of inactivity before redis reclaims it
        long ttlSeconds = windowSeconds * 2;
        try {
            List<Long> result = redis.execute(
                    SCRIPT,
                    List.of(redisKey),
                    String.valueOf(maxRequestAllowed),
                    String.valueOf(refillRate),
                    String.valueOf(System.currentTimeMillis()),
                    String.valueOf(ttlSeconds));

            boolean allowed = result.getFirst() == 1L;
            int remaining = result.get(1).intValue();
            int retryAfter = result.get(2).intValue();

            return allowed ? RateLimitResult.allowed(remaining) : RateLimitResult.denied(Math.max(1, retryAfter));
        } catch (DataAccessException e) {
            log.warn("rate limiter not available {}", key, e);
            return RateLimitResult.allowed(maxRequestAllowed);
        }
    }
}
