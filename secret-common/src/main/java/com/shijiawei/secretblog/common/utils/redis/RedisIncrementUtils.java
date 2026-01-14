package com.shijiawei.secretblog.common.utils.redis;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * ClassName: RedisIncrementUtils
 * Description:
 *
 * @Create 2025/11/3 上午2:17
 */
@Component
@Slf4j
public class RedisIncrementUtils {

    private final RedissonClient redissonClient;

    public RedisIncrementUtils(RedissonClient redissonClient){
        this.redissonClient = redissonClient;
    }

    /**
     * 在事務提交後執行Redis數值遞增操作(無)
     * @param key Redis鍵
     */
    public void afterCommitIncrement(String key){
        afterCommitIncrement(key, null);
    }

    /**
     * 在事務提交後執行Redis數值遞增操作，並可選設置TTL
     * @param key Redis鍵
     * @param ttl 過期時間，若為 ull則不設置過期
     */
    public void afterCommitIncrement(String key, java.time.Duration ttl){
        //先判斷是否在事務中
        boolean synchronizationActive = TransactionSynchronizationManager.isSynchronizationActive();
        if(!synchronizationActive){
            log.warn("當前不在事務中，直接執行數值遞增操作: key={}",key);
            try {
                //不在事務中, 直接增加值
                var atomicLong = redissonClient.getAtomicLong(key);
                atomicLong.incrementAndGet();
                // 設置（若有指定）
                if (ttl !=null&& !ttl.isZero() && !ttl.isNegative()) {
                    atomicLong.expire(ttl);
                    log.debug("成功設置 Redis Key 過期時間: key={}, ttl={}", key, ttl);
                }
            } catch (Exception e) {
                log.error("數值遞增操作失敗: key={}", key ,e);
            }
            return;
        }

        //註冊事務同步回調
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                var atomicLong = redissonClient.getAtomicLong(key);
                atomicLong.incrementAndGet();
                // 設置TTL(若有指定)
                if (ttl !=null&& !ttl.isZero() && !ttl.isNegative()) {
                    atomicLong.expire(ttl);
                    log.debug("成功設置 Redis Key 過期時間: key={}, ttl={}", key, ttl);
                }
            }
        });

    }

    /**
     * 在事務提交後執行Redis數值遞減操作
     * @param key Redis鍵
     */
    public void afterCommitDecrement(String key){
        afterCommitDecrement(key, 1, null);
    }

    /**
     * 在事務提交後執行Redis數值遞減操作
     * @param key Redis鍵
     * @param delta 遞減的數量
     */
    public void afterCommitDecrement(String key, long delta){
        afterCommitDecrement(key, delta, null);
    }

    /**
     * 在事務提交後執行Redis數值遞減操作，並可選設置
     * @param key Redis鍵
     * @param delta 遞減的數量
     * @param ttl 過期時間，若為null則不設置過期
     */
    public void afterCommitDecrement(String key, long delta, java.time.Duration ttl){
        //先判斷是否在事務中
        boolean synchronizationActive = TransactionSynchronizationManager.isSynchronizationActive();
        if(!synchronizationActive){
            log.warn("當前不在事務中，直接執行數值遞減操作: key={}, delta={}",key, delta);
            try {
                //不在事務中, 直接減少值
                var atomicLong = redissonClient.getAtomicLong(key);
                atomicLong.addAndGet(-delta);
                // 設置(若有指定)
                if (ttl !=null&& !ttl.isZero() && !ttl.isNegative()) {
                    atomicLong.expire(ttl);
                    log.debug("成功設置 Redis Key 過期時間: key={}, ttl={}", key, ttl);
                }
            } catch (Exception e) {
                log.error("數值遞減操作失敗: key={}, delta={}", key, delta, e);
            }
            return;
        }

        //註冊事務同步回調
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                var atomicLong = redissonClient.getAtomicLong(key);
                atomicLong.addAndGet(-delta);
                // 設置(若有指定)
                if (ttl !=null&& !ttl.isZero() && !ttl.isNegative()) {
                    atomicLong.expire(ttl);
                    log.debug("成功設置 Redis Key 過期時間: key={}, ttl={}", key, ttl);
                }
            }
        });
    }

}
