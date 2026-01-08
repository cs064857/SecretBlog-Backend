package com.shijiawei.secretblog.common.utils.redis;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * ClassName: RedisRateLimiterUtils
 * Redis 基於 Redisson 的分佈式限流器工具類
 * 用於控制 API 的請求頻率
 * @Create 2025/2/26 上午1:06
 */
@Slf4j
@Component
public class RedisRateLimiterUtils {

    private final RedissonClient redissonClient;

    @Autowired
    public RedisRateLimiterUtils(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 設置並獲取限流器
     * @param bucket 限流器的唯一標識
     * @param rateType 限流類型 (全局(OVERALL)或按客戶端(PER_CLIENT))
     * @param rate 時間間隔內允許的請求數
     * @param rateInterval 時間間隔值
     * @param rateIntervalUnit 時間間隔單位
     * @return 配置好的限流器實例
     */
    public RRateLimiter setRedisRateLimiter(String bucket, RateType rateType, long rate, long rateInterval, RateIntervalUnit rateIntervalUnit) {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(bucket);

        try {
            boolean initialized = rateLimiter.trySetRate(rateType, rate, rateInterval, rateIntervalUnit);
            if (initialized) {
                log.debug(
                        "已初始化 Redis 限流器 bucket={} rateType={} rate={} rateInterval={} rateIntervalUnit={}",
                        bucket,
                        rateType,
                        rate,
                        rateInterval,
                        rateIntervalUnit
                );
            } else {
                log.debug("Redis 限流器已存在，略過初始化 bucket={}", bucket);
            }
        } catch (Exception e) {
            log.error("初始化 Redis 限流器失敗 bucket={}", bucket, e);
            throw e;
        }

        return rateLimiter;
    }

    /**
     * 嘗試取得許可
     *
     * @param rateLimiter 已設定好的限流器
     * @return 是否成功取得許可
     */
    public boolean tryAcquire(RRateLimiter rateLimiter) {
        boolean acquired = rateLimiter.tryAcquire();
        if (!acquired) {
            log.warn("觸發限流（未取得許可） bucket={}", rateLimiter.getName());
        }
        return acquired;
    }
}
