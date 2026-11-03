package com.myshopycart.productservice.config;

import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.stereotype.Component;

@Component
public class RedisCacheErrorHandler implements CacheErrorHandler {

    @Override
    public void handleCacheGetError(
            RuntimeException exception,
            Cache cache,
            Object key) {

        System.out.println(
                "Redis cache GET failed for key: "
                        + key
                        + ". Falling back to database.");
    }

    @Override
    public void handleCachePutError(
            RuntimeException exception,
            Cache cache,
            Object key,
            Object value) {

        System.out.println(
                "Redis cache PUT failed for key: "
                        + key
                        + ". Continuing without cache.");
    }

    @Override
    public void handleCacheEvictError(
            RuntimeException exception,
            Cache cache,
            Object key) {

        System.out.println(
                "Redis cache EVICT failed for key: "
                        + key
                        + ". Continuing without cache.");
    }

    @Override
    public void handleCacheClearError(
            RuntimeException exception,
            Cache cache) {

        System.out.println(
                "Redis cache CLEAR failed. Continuing without cache.");
    }
}