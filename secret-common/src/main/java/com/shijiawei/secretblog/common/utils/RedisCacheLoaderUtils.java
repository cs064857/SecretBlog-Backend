package com.shijiawei.secretblog.common.utils;

import com.shijiawei.secretblog.common.exception.CustomBaseException;
import com.shijiawei.secretblog.common.myenum.RedisCacheKey;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * ClassName: RedisCacheLoaderUtils
 * Description:
 *
 * @Create 2025/11/4 下午11:41
 */
@Slf4j
@Component
public class RedisCacheLoaderUtils {

    private final RedissonClient redissonClient;

    public RedisCacheLoaderUtils(RedissonClient redissonClient){
        this.redissonClient = redissonClient;
    }

    /**
     * 從Redis中取得數據，如果未取得則從DB中取得，再將DB中的資料加入Redis中進行快取，採用分佈式鎖，避免快取穿透
     * @param cacheKey 快取鍵
     * @param lockKey 分布式鎖鍵
     * @param dbLoader 資料庫載入函數（包含取得數據後將數據加入Redis中）
     * @param cacheLoader 快取讀取函數（將 Map 轉為實體對象）
     * @param waitTime 等待獲取鎖的時間
     * @param leaseTime 鎖持有時間
     * @param unit 時間單位
     * @param intervalTime 檢查是否已經取得鎖的時間間隔
     * @return 快取對象
     * @param <T> 快取對象類型
     */
    public <T> T loadMapWithLock(String cacheKey, String lockKey, Supplier<T> dbLoader, Supplier<T> cacheLoader , long waitTime , long leaseTime , TimeUnit unit , long intervalTime){
        log.info("開始執行 loadMapWithLock - cacheKey: {}, lockKey: {}, waitTime: {}, leaseTime: {}, unit: {}",
                cacheKey, lockKey, waitTime, leaseTime, unit);
        RLock lock = redissonClient.getLock(lockKey);
        try {
            log.debug("嘗試獲取鎖 - lockKey: {}, waitTime: {}, leaseTime: {}", lockKey, waitTime, leaseTime);

            boolean tryLock = lock.tryLock(waitTime, leaseTime,unit);

            //判斷是否成功取得鎖
            if(!tryLock){
                log.warn("未能立即獲取鎖 - lockKey: {}, 進入等待邏輯", lockKey);

                //未成功取得鎖(鎖可能正被其他線程占用), 等待其他線程取得資料
                //設置最長檢查時間為deadline秒
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(waitTime);
                int checkCount = 0;
                log.info("開始檢查快取是否存在 - cacheKey: {}, 最長等待時間: {} 秒", cacheKey, waitTime);
                //持續檢查目標資料是否已經存在
                while(System.nanoTime() < deadline){
                    checkCount++;
                    log.debug("第 {} 次檢查快取是否存在 - cacheKey: {}", checkCount, cacheKey);
                    //判斷是否已經存在
                    if(redissonClient.getMap(cacheKey).isExists()){
                        log.info("快取已存在 - cacheKey: {}, 等待 {} 秒後返回", cacheKey, waitTime);

                        //已經存在，等待最後一次取得鎖的時間
                        TimeUnit.SECONDS.sleep(waitTime);
                        log.info("等待完成，快取資料應已準備完畢 - cacheKey: {}", cacheKey);
                        //從快取讀取方法中獲取方法的返回值
                        return cacheLoader.get();
                    }
                    //間隔時間
                    log.debug("快取尚不存在，等待 {} 秒後重新檢查", intervalTime);
                    TimeUnit.SECONDS.sleep(intervalTime);
                }
                log.error("等待超時，未能獲取鎖且快取未建立 - lockKey: {}, cacheKey: {}, 檢查次數: {}",
                        lockKey, cacheKey, checkCount);
                return null;
            }
            log.info("鎖取得成功，鎖名稱：{}",lockKey);
            //成功取得鎖則執行目標查詢資料庫的方法
            T dataFromDB = dbLoader.get();

            log.info("查詢資料庫完成，快取資料應已寫入完畢 - cacheKey: {}, t: {}", cacheKey, dataFromDB);


            return dataFromDB;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomBaseException("系統異常");
        } finally {
            //最終釋放鎖
            if(lock.isLocked() && lock.isHeldByCurrentThread()){
                lock.unlock();
            }

        }
    }




}
