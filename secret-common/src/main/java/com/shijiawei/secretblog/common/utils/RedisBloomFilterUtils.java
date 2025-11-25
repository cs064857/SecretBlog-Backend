package com.shijiawei.secretblog.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shijiawei.secretblog.common.codeEnum.ResultCode;
import com.shijiawei.secretblog.common.exception.BusinessRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Map;

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

    @Autowired
    private ObjectMapper objectMapper;

    public RedisBloomFilterUtils(RedissonClient redissonClient){
        this.redissonClient=redissonClient;
    }

    /**
     * 判斷布隆過濾器中是否存在該值，用於篩選百分百不存在的值
     * 若該值百分百不存在於布隆過濾器中則返回true , 若該值可能存在於布隆過濾器中則返回false
     * @param key
     * @param value
     * @return
     * @param <T>
     */
    public <T> boolean isDefinitelyNotExists(String key, T value) {
        RBloomFilter<T> filter = redissonClient.getBloomFilter(key);

        if (!filter.contains(value)) {
            //值絕對不存在於布隆過濾器中的情況下，拋出異常
            log.warn("布隆過濾器確認該值不存在: key={}, value={}", key, value);
            return true;
        }

        return false;
    }

    /**
     * 判斷布隆過濾器中是否存在該值，用於篩選百分百不存在的值
     * 若該值不存在於布隆過濾器中則直接拋出異常
     * @param key
     * @param value
     * @param errorMessage
     * @param <T>
     */
    public <T>  void  requireExists(String key, T value , String errorMessage) {
        RBloomFilter<T> filter = redissonClient.getBloomFilter(key);
        //判斷值是否存在於布隆過濾器中
        if (!filter.contains(value)) {
            //值絕對不存在於布隆過濾器中的情況下，拋出異常
//            log.warn("布隆過濾器確認該值不存在: key={}, value={} , errorMessage:{}", key, value ,errorMessage);
//            throw new CustomRuntimeException(ResultCode.BLOOM_FILTER_KEY_NOT_FOUND.getCode(), ResultCode.BLOOM_FILTER_KEY_NOT_FOUND.getMessage());

            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.BLOOM_FILTER_KEY_NOT_FOUND)
                    .detailMessage("布隆過濾器確認該值不存在")
                    .data(Map.of("key",StringUtils.defaultString(key),
                            "value", ObjectUtils.defaultIfNull(value,"")))
                    .build();

        }
    }

    /**
     * 在事務提交後將對象加入至布隆過濾器中
     * @param obj 要添加到布隆過濾器的對象
     * @param key 布隆過濾器的key
     * @return boolean
     * @param <T> 對象類型
     */
    public <T> boolean saveObjToBloomFilter(T obj,String key) {

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
                saveObjToBloomFilter(obj, key);
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
                            saveObjToBloomFilter(obj, key);
                        } catch (Exception e) {
                            // 布隆過濾器出現異常,但不拋出異常，避免影響主流程
                            log.error("事務提交後布隆過濾器添加失敗: key={}, obj={}", key, obj, e);
                        }

                    }
                }
        );
    }

    public <T,R> void saveMapToRMapAfterCommit(String rMapKey , Map<T, R> value , Class<T> keyClass , Class<R> valueClass){

        if(value.isEmpty()){
            log.error("事務提交後將值添加至Redis失敗, rMapKey:{}",rMapKey);
        }

        if(!TransactionSynchronizationManager.isActualTransactionActive()){
            log.warn("當前不在事務中，直接執行布隆過濾器操作: rMapKey={}, mapSize={}", rMapKey, value.size());
            try {
                RMap<T, R> rMap = redissonClient.getMap(rMapKey, new TypedJsonJacksonCodec(keyClass, valueClass));
                updateRedisMap(value, rMap);
            } catch (Exception e) {
                log.error("事務提交後同步 Redis 失敗 (DB已提交). rMapKey: {}, MapSize: {}, Error: {}",
                        rMapKey, value.size(), e.getMessage(), e);
            }
            return;
        }
        
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){
            
            @Override
            public void afterCommit(){
                try {
                    RMap<T, R> rMap = redissonClient.getMap(rMapKey, new TypedJsonJacksonCodec(keyClass, valueClass));

                    updateRedisMap(value, rMap);
                } catch (Exception e) {
                    log.error("事務提交後同步 Redis 失敗 (DB已提交). rMapKey: {}, MapSize: {}, Error: {}",
                            rMapKey, value, e.getMessage(), e);
                }
            }
            
        } );

    }

    private <T,R> void updateRedisMap(Map<T, R> map, RMap<T, R> rMap) {


        if(!rMap.isExists()){
            log.error("事務提交後同步 Redis 失敗 (DB已提交) , Redis鍵尚未存在. rMapName: {}, MapSize: {}",
                    rMap.getName(), map);
        }
        rMap.putAll(map);
        log.debug("成功將 Map 資料同步至 Redis: rMapName={}, mapSize={}", rMap.getName(), map.size());
    }
}
