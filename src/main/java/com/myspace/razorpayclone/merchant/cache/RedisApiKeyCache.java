package com.myspace.razorpayclone.merchant.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisApiKeyCache implements ApiKeyCache {
    private final StringRedisTemplate redisTemplate;
    private static final String PREFIX = "apiKey_";
    private static final Duration TTL = Duration.ofMinutes(5);
    private final ObjectMapper objectMapper;


    @Override
    public Optional<ApiKeyCacheEntry> get(String keyId) {
        try {
            String json = redisTemplate.opsForValue().get(PREFIX + keyId);
            if (json == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, ApiKeyCacheEntry.class));
        } catch (Exception e) {
            log.warn("Redis Failed with exception for get {}: {}", keyId, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void put(String keyId, ApiKeyCacheEntry entry) {
        try {
            redisTemplate.opsForValue()
                    .set(PREFIX + keyId, objectMapper.writeValueAsString(entry), TTL);
        } catch (Exception e) {
            log.warn("Redis Failed with exception for put {}: {}", keyId, e.getMessage());
        }
    }

    @Override
    public void evict(String keyId) {
        redisTemplate.delete(PREFIX + keyId);
    }
}
