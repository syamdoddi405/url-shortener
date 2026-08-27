package com.url.shortener.service.cache;

import java.util.Optional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis implementation of CacheService.
 * Handles caching logic for URL mappings.
 * Implements the Strategy pattern, allowing easy replacement with other implementations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisCacheService implements CacheService {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public Optional<String> get(String key) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                log.debug("Cache hit for key: {}", key);
                return Optional.of(value);
            }
            log.debug("Cache miss for key: {}", key);
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Error retrieving from cache for key: {}", key, e);
            return Optional.empty();
        }
    }

    @Override
    public void put(String key, String value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            log.debug("Cached value for key: {}", key);
        } catch (Exception e) {
            log.error("Error storing in cache for key: {}", key, e);
        }
    }

    @Override
    public void invalidate(String key) {
        try {
            Boolean deleted = redisTemplate.delete(key); 
            if (Boolean.TRUE.equals(deleted)) {
                log.debug("Cache invalidated for key: {}", key);
            }
        } catch (Exception e) {
            log.error("Error invalidating cache for key: {}", key, e);
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            Boolean hasKey = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(hasKey);
        } catch (Exception e) {
            log.warn("Error checking cache existence for key: {}", key, e);
            return false;
        }
    }
}
