package com.shijiawei.secretblog.user.authentication.service;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * JWT 黑名單服務
 */
@Service
public class TokenBlacklistService {

    private static final String KEY_PREFIX = "blacklist:jwt:";

    private final RedissonClient redissonClient;

    public TokenBlacklistService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public void blacklist(String sessionId, long ttlMillis) {
        if (sessionId == null) return;
        long ttl = Math.max(ttlMillis, 1000L); // 保底 1s 避免0或負數
        RBucket<String> bucket = redissonClient.getBucket(KEY_PREFIX + sessionId);
        bucket.set("1", ttl, TimeUnit.MILLISECONDS);
    }

    public boolean isBlacklisted(String sessionId) {
        if (sessionId == null) return false;
        return redissonClient.getBucket(KEY_PREFIX + sessionId).isExists();
    }
}

