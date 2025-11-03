package com.shijiawei.secretblog.common.utils;

import com.shijiawei.secretblog.common.myenum.RedisCacheKey;
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
     * 在事務提交後執行Redis數值遞增操作
     * @param key Redis鍵
     */
    public void afterCommitIncrement(String key){
        //先判斷是否在事務中
        boolean synchronizationActive = TransactionSynchronizationManager.isSynchronizationActive();
        if(!synchronizationActive){
            log.warn("當前不在事務中，直接執行數值遞增操作: key={}",key);
            try {
                //不在事務中, 直接增加值
                redissonClient.getAtomicLong(key).incrementAndGet();
            } catch (Exception e) {
                log.error("數值遞增操作失敗: key={}", key ,e);
            }
            return;
        }

        //註冊事務同步回調
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                redissonClient.getAtomicLong(key).incrementAndGet();
            }
        });

    }

}
