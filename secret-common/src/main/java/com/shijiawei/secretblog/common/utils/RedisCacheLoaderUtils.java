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



//
//    /**
//     * Double-Check Locking 模式載入快取
//     *
//     * @param cacheKey 快取鍵
//     * @param lockKey 分布式鎖鍵
//     * @param dbLoader 資料庫載入函數（會寫入快取）
//     * @param cacheReader 快取讀取函數（將 Map 轉為實體對象）
//     * @param waitTime 等待獲取鎖的時間
//     * @param leaseTime 鎖持有時間
//     * @param unit 時間單位
//     */
//    public <T> T loadMapWithLock(String cacheKey,
//                                 String lockKey,
//                                 Supplier<T> dbLoader,
//                                 Function<String, T> cacheReader,
//                                 long waitTime,
//                                 long leaseTime,
//                                 TimeUnit unit) {
//
//        log.info("開始執行 Double-Check Locking - cacheKey: {}, lockKey: {}", cacheKey, lockKey);
//
//        // ==================== 第一次檢查（First Check）====================
//        // 所有線程都先檢查快取是否存在
//        if (redissonClient.getMap(cacheKey).isExists()) {
//            log.info("快取命中（第一次檢查）- cacheKey: {}", cacheKey);
//            return cacheReader.apply(cacheKey);  // ✅ 直接從快取讀取並返回
//        }
//
//        log.info("快取不存在，準備獲取鎖 - cacheKey: {}", cacheKey);
//
//        // ==================== 獲取分布式鎖 ====================
//        RLock lock = redissonClient.getLock(lockKey);
//        try {
//            boolean locked = lock.tryLock(waitTime, leaseTime, unit);
//
//            if (!locked) {
//                log.warn("獲取鎖超時 - lockKey: {}, waitTime: {}{}",
//                        lockKey, waitTime, unit.toString().toLowerCase());
//                return null;  // 超時返回 null，調用方需處理
//            }
//
//            log.info("獲取鎖成功 - lockKey: {}", lockKey);
//
//            // ==================== 第二次檢查（Double-Check）====================
//            // ⭐ 關鍵：其他線程可能已經在我們等待鎖期間寫入了快取
//            if (redissonClient.getMap(cacheKey).isExists()) {
//                log.info("快取已存在（Double-Check）- cacheKey: {}, 避免了重複查詢資料庫", cacheKey);
//                return cacheReader.apply(cacheKey);  // ✅ 讀取快取，不查資料庫
//            }
//
//            // ==================== 快取仍不存在，查詢資料庫 ====================
//            log.info("快取確認不存在，開始查詢資料庫 - cacheKey: {}", cacheKey);
//            T result = dbLoader.get();  // 執行資料庫查詢 + 寫入快取
//
//            log.info("資料庫查詢完成，快取已寫入 - cacheKey: {}", cacheKey);
//            return result;
//
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            log.error("執行被中斷 - lockKey: {}, cacheKey: {}", lockKey, cacheKey, e);
//            throw new CustomBaseException("系統異常");
//        } finally {
//            // 釋放鎖
//            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
//                lock.unlock();
//                log.debug("鎖已釋放 - lockKey: {}", lockKey);
//            }
//        }
//    }
//
//    /**
//     * 從快取讀取文章狀態（用於 Double-Check）
//     *
//     * @param redisKey Redis 快取鍵
//     * @return 文章狀態對象，如果快取為空返回 null
//     */
//    private AmsArtStatus readArticleStatusFromCache(String redisKey) {
//        log.debug("從快取讀取文章狀態 - redisKey: {}", redisKey);
//
//        try {
//            // 從 Redis 讀取 Map 資料
//            RMap<String, Integer> statusMap = redissonClient.getMap(redisKey,
//                    new TypedJsonJacksonCodec(String.class, Integer.class));
//
//            Map<String, Integer> cacheData = statusMap.readAllMap();
//
//            // 檢查快取是否為空
//            if (cacheData.isEmpty()) {
//                log.warn("快取資料為空 - redisKey: {}", redisKey);
//                return null;
//            }
//
//            // 轉換為實體對象
//            AmsArtStatus status = new AmsArtStatus();
//            status.setViewsCount(cacheData.getOrDefault("views", -1));
//            status.setLikesCount(cacheData.getOrDefault("likes", -1));
//            status.setBookmarksCount(cacheData.getOrDefault("bookmarks", -1));
//            status.setCommentsCount(cacheData.getOrDefault("comments", -1));
//
//            log.debug("快取讀取成功 - redisKey: {}, views: {}, likes: {}",
//                    redisKey, status.getViewsCount(), status.getLikesCount());
//
//            return status;
//
//        } catch (Exception e) {
//            log.error("從快取讀取失敗 - redisKey: {}", redisKey, e);
//            return null;
//        }
//    }
//
//    /**
//     * 根據文章ID取得文章狀態
//     * 採用 Double-Check Locking 模式
//     */
//    public AmsArticleStatusVo getArticleStatus(long articleId) {
//
//        String redisKey = RedisCacheKey.ARTICLE_STATUS.format(articleId);
//
//        // ==================== 快速路徑：嘗試直接讀取快取 ====================
//        RMap<String, Integer> statusMap = redissonClient.getMap(redisKey,
//                new TypedJsonJacksonCodec(String.class, Integer.class));
//
//        Map<String, Integer> cacheData = statusMap.readAllMap();
//
//        int views = cacheData.getOrDefault("views", -1);
//        int likes = cacheData.getOrDefault("likes", -1);
//        int bookmarks = cacheData.getOrDefault("bookmarks", -1);
//        int comments = cacheData.getOrDefault("comments", -1);
//
//        // 判斷快取是否完整
//        boolean cacheIncomplete = (views == -1 || likes == -1 || bookmarks == -1 || comments == -1);
//
//        if (cacheIncomplete) {
//            log.info("快取不完整，使用 Double-Check Locking 載入 - articleId: {}", articleId);
//
//            // ==================== 使用 Double-Check Locking 載入 ====================
//            AmsArtStatus status = redisCacheLoaderUtils.loadMapWithLock(
//                    redisKey,
//                    "ams:article:status:lock:" + articleId,  // ✅ 每篇文章獨立的鎖
//                    () -> LoadArticleStatus(articleId),      // 資料庫載入器
//                    this::readArticleStatusFromCache,        // ✅ 快取讀取器（方法引用）
//                    3,    // 等待鎖 3 秒
//                    10,   // 鎖持有 10 秒
//                    TimeUnit.SECONDS
//            );
//
//            // 處理載入結果
//            if (status == null) {
//                log.warn("載入失敗，返回預設值 - articleId: {}", articleId);
//                views = likes = bookmarks = comments = -1;
//            } else {
//                views = status.getViewsCount();
//                likes = status.getLikesCount();
//                bookmarks = status.getBookmarksCount();
//                comments = status.getCommentsCount();
//            }
//        }
//
//        log.info("文章狀態 - articleId: {}, views: {}, likes: {}, bookmarks: {}, comments: {}",
//                articleId, views, likes, bookmarks, comments);
//
//        // 包裝並返回
//        return AmsArticleStatusVo.builder()
//                .viewsCount(views)
//                .likesCount(likes)
//                .bookmarksCount(bookmarks)
//                .commentsCount(comments)
//                .build();
//    }
//
//    /**
//     * 從資料庫載入文章狀態並寫入快取
//     * ✅ 這個方法不需要修改，已經符合 Double-Check 模式
//     */
//    private AmsArtStatus LoadArticleStatus(long articleId) {
//
//        // 查詢資料庫
//        AmsArtStatus articleStatusFromDB = QueryArticleStatus(articleId);
//
//        log.info("資料庫查詢結果 - articleId: {}, status: {}", articleId, articleStatusFromDB);
//
//        if (articleStatusFromDB == null) {
//            log.warn("文章狀態不存在，寫入預設值防止快取穿透 - articleId: {}", articleId);
//        }
//
//        // 寫入快取（無論是否為 null 都寫入，防止快取穿透）
//        int views = (articleStatusFromDB == null) ? -1 : articleStatusFromDB.getViewsCount();
//        int likes = (articleStatusFromDB == null) ? -1 : articleStatusFromDB.getLikesCount();
//        int bookmarks = (articleStatusFromDB == null) ? -1 : articleStatusFromDB.getBookmarksCount();
//        int comments = (articleStatusFromDB == null) ? -1 : articleStatusFromDB.getCommentsCount();
//
//        String redisKey = RedisCacheKey.ARTICLE_STATUS.format(articleId);
//        RMap<String, Integer> statusMap = redissonClient.getMap(redisKey,
//                new TypedJsonJacksonCodec(String.class, Integer.class));
//
//        statusMap.put("views", views);
//        statusMap.put("likes", likes);
//        statusMap.put("bookmarks", bookmarks);
//        statusMap.put("comments", comments);
//
//        log.info("快取寫入完成 - articleId: {}, redisKey: {}", articleId, redisKey);
//
//        return articleStatusFromDB;
//    }

}
