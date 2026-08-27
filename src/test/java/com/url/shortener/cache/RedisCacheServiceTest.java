package com.url.shortener.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.url.shortener.service.cache.RedisCacheService;

@ExtendWith(MockitoExtension.class)
class RedisCacheServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisCacheService cacheService;

    private final String key = "abc12345";
    private final String value = "https://google.com";

    @BeforeEach
    void setUp() {
        cacheService =
                new RedisCacheService(redisTemplate);
    }

    @Test
    void get_shouldReturnValue_whenCacheHit() {

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.get(key))
                .thenReturn(value);

        Optional<String> result =
                cacheService.get(key);

        assertTrue(result.isPresent());
        assertEquals(value, result.get());

        verify(redisTemplate).opsForValue();
        verify(valueOperations).get(key);
    }

    @Test
    void get_shouldReturnEmpty_whenCacheMiss() {

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.get(key))
                .thenReturn(null);

        Optional<String> result =
                cacheService.get(key);

        assertTrue(result.isEmpty());

        verify(valueOperations).get(key);
    }

    @Test
    void get_shouldReturnEmpty_whenRedisThrowsException() {

        when(redisTemplate.opsForValue())
                .thenThrow(
                        new RuntimeException(
                                "Redis unavailable"
                        )
                );

        Optional<String> result =
                cacheService.get(key);

        assertTrue(result.isEmpty());
    }

    @Test
    void put_shouldStoreValueInRedis() {

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        cacheService.put(key, value);

        verify(redisTemplate).opsForValue();
        verify(valueOperations).set(key, value);
    }

    @Test
    void put_shouldNotThrowException_whenRedisFails() {

        when(redisTemplate.opsForValue())
                .thenThrow(
                        new RuntimeException(
                                "Redis unavailable"
                        )
                );

        // Should not throw exception
        cacheService.put(key, value);

        verify(redisTemplate).opsForValue();
    }

    @Test
    void invalidate_shouldDeleteKey() {

        when(redisTemplate.delete(key))
                .thenReturn(Boolean.TRUE);

        cacheService.invalidate(key);

        verify(redisTemplate).delete(key);
    }

    @Test
    void invalidate_shouldHandleFalseDeleteResult() {

        when(redisTemplate.delete(key))
                .thenReturn(Boolean.FALSE);

        cacheService.invalidate(key);

        verify(redisTemplate).delete(key);
    }

    @Test
    void invalidate_shouldNotThrowException_whenRedisFails() {

        when(redisTemplate.delete(key))
                .thenThrow(
                        new RuntimeException(
                                "Redis unavailable"
                        )
                );

        cacheService.invalidate(key);

        verify(redisTemplate).delete(key);
    }

    @Test
    void exists_shouldReturnTrue_whenKeyExists() {

        when(redisTemplate.hasKey(key))
                .thenReturn(Boolean.TRUE);

        boolean result =
                cacheService.exists(key);

        assertTrue(result);

        verify(redisTemplate).hasKey(key);
    }

    @Test
    void exists_shouldReturnFalse_whenKeyDoesNotExist() {

        when(redisTemplate.hasKey(key))
                .thenReturn(Boolean.FALSE);

        boolean result =
                cacheService.exists(key);

        assertFalse(result);
    }

    @Test
    void exists_shouldReturnFalse_whenRedisReturnsNull() {

        when(redisTemplate.hasKey(key))
                .thenReturn(null);

        boolean result =
                cacheService.exists(key);

        assertFalse(result);
    }

    @Test
    void exists_shouldReturnFalse_whenRedisFails() {

        when(redisTemplate.hasKey(key))
                .thenThrow(
                        new RuntimeException(
                                "Redis unavailable"
                        )
                );

        boolean result =
                cacheService.exists(key);

        assertFalse(result);
    }
}