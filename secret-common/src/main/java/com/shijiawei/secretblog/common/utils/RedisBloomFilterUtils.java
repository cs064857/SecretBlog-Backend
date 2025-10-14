package com.shijiawei.secretblog.common.utils;

import com.shijiawei.secretblog.common.myenum.RedisBloomFilterKey;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * ClassName: RedisBloomFilterUtils
 * Description:
 *
 * @Create 2025/10/15 上午2:11
 */
@Slf4j
@Component
public class RedisBloomFilterUtils {

    private final RedissonClient redissonClient;

    @Value("${bloom-filter.article.expected-insertions:10000}")
    private Long expectedInsertions; // 預期插入的元素數量, 默認值1萬
    @Value("${bloom-filter.article.false-probability:0.01}")
    private Double falseProbability; // 可接受的誤判率, 默認值1%

    public RedisBloomFilterUtils(RedissonClient redissonClient){
        this.redissonClient=redissonClient;
    }

    public <T> boolean saveArticleIdToBloomFilter(T obj,String key) {

        RBloomFilter<T> bloomFilter = redissonClient.getBloomFilter(key);

        if (!bloomFilter.isExists()) {
            log.warn("布隆過濾器不存在，嘗試初始化: key={}", key);
            boolean tryInit = bloomFilter.tryInit(expectedInsertions, falseProbability);
            if (tryInit) {
                log.info("布隆過濾器初始化成功: key={}", key);
            } else {
                log.warn("布隆過濾器初始化失敗: key={}", key);
            }
        }

        try {
            boolean added = bloomFilter.add(obj);
            if (added) {
                log.info("成功添加到布隆過濾器: key={}, obj={}", key, obj);
            } else {
                log.warn("元素已存在於布隆過濾器: key={}, obj={}", key, obj);
            }
            return added;
        } catch (Exception e) {
            log.error("添加到布隆過濾器失敗: key={}, obj={}", key, obj, e);
            throw e; // 拋出異常，讓上層捕獲
        }

    }


    /**
     * 在事務提交後執行布隆過濾器操作（通用方法）
     *
     * @param obj 要添加到布隆過濾器的對象
     * @param key 布隆過濾器的key
     * @param <T> 對象類型
     */
    public <T> void saveToBloomFilterAfterCommit(T obj, String key) {
        // 檢查是否在事務中
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            log.warn("當前不在事務中，直接執行布隆過濾器操作: key={}, obj={}", key, obj);
            try {
                saveArticleIdToBloomFilter(obj, key);
            } catch (Exception e) {
                log.error("布隆過濾器添加失敗: key={}, obj={}", key, obj, e);
            }
            return;
        }

        // 註冊事務同步回調
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            saveArticleIdToBloomFilter(obj, key);
                        } catch (Exception e) {
                            // 布隆過濾器出現異常,但不拋出異常，避免影響主流程
                            log.error("事務提交後布隆過濾器添加失敗: key={}, obj={}", key, obj, e);
                        }
                    }
                }
        );
    }
}
