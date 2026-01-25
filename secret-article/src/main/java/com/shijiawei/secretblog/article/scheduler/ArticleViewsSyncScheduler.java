package com.shijiawei.secretblog.article.scheduler;

import com.shijiawei.secretblog.article.service.AmsArtStatusService;
import com.shijiawei.secretblog.common.myenum.RedisCacheKey;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * ClassName: ArticleViewsSyncScheduler
 * Description:將Redis中的文章瀏覽數同步至資料庫
 *
 * @Create 2026/01/25 下午5:55
 */
@Slf4j
@Component
public class ArticleViewsSyncScheduler {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private AmsArtStatusService amsArtStatusService;

    /**
     * 定時同步Redis瀏覽數至DB
     * 每5分鐘執行
     */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void syncViewsCountToDatabase() {
        log.info("開始執行Redis瀏覽數同步至DB定時任務...");

        try {
            //取得符合pattern的所有Redis鍵(例如：ams:article:views:*)
            String keyPattern = RedisCacheKey.ARTICLE_VIEWS.getPattern().replace("%s", "*");
            log.debug("掃描Redis鍵 pattern: {}",keyPattern);

            Iterable<String> keys = redissonClient.getKeys().getKeysByPattern(keyPattern);

            Map<Long, Integer> viewsCountMap = new HashMap<>();
            int keyCount = 0;

            for (String key : keys) {
                keyCount++;
                try {
                    //從key中解析articleId(格式：ams:article:views:{articleId})
                    String[] parts = key.split(":");
                    if (parts.length < 4) {
                        log.warn("無法解析Redis鍵: {}", key);
                        continue;
                    }
                    Long articleId = Long.parseLong(parts[3]);

                    //從Redis取得瀏覽數
                    RAtomicLong viewCounter = redissonClient.getAtomicLong(key);
                    long views = viewCounter.get();
                    viewsCountMap.put(articleId, (int) views);
                } catch (NumberFormatException e) {
                    log.warn("解析articleId失敗，key={}", key, e);
                }
            }

            log.info("掃描完成，共發現 {} 個瀏覽數鍵", keyCount);

            if (viewsCountMap.isEmpty()) {
                log.info("無瀏覽數需要同步，任務結束");
                return;
            }

            //批量更新至DB
            int successCount = amsArtStatusService.batchUpdateViewsCount(viewsCountMap);
            log.info("Redis瀏覽數同步至DB完成，總數={}，成功={}", viewsCountMap.size(), successCount);

        } catch (Exception e) {
            log.error("Redis瀏覽數同步至DB發生錯誤", e);
        }
    }
}
