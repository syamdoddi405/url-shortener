package com.url.shortener.service.cache;

import java.util.Optional;

/**
 * Cache abstraction interface.
 * Follows the Strategy pattern and Dependency Inversion Principle.
 * Allows for easy switching between different cache implementations.
 */
public interface CacheService {

    /**
     * Retrieves a value from cache.
     *
     * @param key the cache key
     * @return Optional containing the cached value, or empty if not found
     */
    Optional<String> get(String key);

    /**
     * Stores a value in cache.
     *
     * @param key the cache key
     * @param value the value to cache
     */
    void put(String key, String value);

    /**
     * Removes a value from cache.
     *
     * @param key the cache key
     */
    void invalidate(String key);

    /**
     * Checks if a key exists in cache.
     *
     * @param key the cache key
     * @return true if key exists, false otherwise
     */
    boolean exists(String key);
}
