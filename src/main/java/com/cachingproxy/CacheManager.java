package com.cachingproxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the cache storage and retrieval
 */
public class CacheManager {
    private static final Logger logger = LoggerFactory.getLogger(CacheManager.class);
    private static final CacheManager INSTANCE = new CacheManager();

    private final Map<String, CacheEntry> cache;

    private CacheManager() {
        this.cache = new ConcurrentHashMap<>();
    }

    public static CacheManager getInstance() {
        return INSTANCE;
    }

    /**
     * Generate cache key from HTTP method and URL
     */
    public String generateCacheKey(String method, String url) {
        return method.toUpperCase() + ":" + url;
    }

    /**
     * Get cached entry if it exists
     */
    public CacheEntry getCached(String cacheKey) {
        return cache.get(cacheKey);
    }

    /**
     * Store response in cache
     */
    public void cache(String cacheKey, CacheEntry entry) {
        cache.put(cacheKey, entry);
        logger.debug("Cached response for: {}", cacheKey);
    }

    /**
     * Clear all cache entries
     */
    public void clearCache() {
        int size = cache.size();
        cache.clear();
        logger.info("Cache cleared. {} entries removed.", size);
    }

    /**
     * Get cache statistics
     */
    public int getCacheSize() {
        return cache.size();
    }

    /**
     * Print cache statistics
     */
    public void printStats() {
        logger.info("Cache Statistics - Total entries: {}", cache.size());
    }
}
