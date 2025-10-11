package com.shijiawei.secretblog.article.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shijiawei.secretblog.article.entity.AmsArticle;
import com.shijiawei.secretblog.article.entity.AmsComment;
import com.shijiawei.secretblog.article.mapper.AmsArticleMapper;
import com.shijiawei.secretblog.article.mapper.AmsCommentMapper;
import com.shijiawei.secretblog.common.exception.CustomBaseException;
import com.shijiawei.secretblog.common.myenum.RedisBloomFilterEnum;
import com.shijiawei.secretblog.common.myenum.RedisLockKey;
import io.swagger.v3.core.util.Json;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.redisson.RedissonBloomFilter;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * ClassName: BloomFilterInitializer
 * Description:
 *
 * @Create 2025/10/9 下午8:04
 */
@Slf4j
@Component
public class BloomFilterInitializer {

//    @Qualifier("articleBloomFilter")
//    @Resource

    private final RedissonClient redissonClient;

    private final RBloomFilter<Long> articleRBloomFilter;
    private final RBloomFilter<Long> commentRBloomFilter;

    private final AmsArticleMapper amsArticleMapper;
    private final AmsCommentMapper amsCommentMapper;

    public BloomFilterInitializer
            (
                RedissonClient redissonClient,
                @Qualifier("articleBloomFilter") RBloomFilter<Long> articleRBloomFilter,
                @Qualifier("commentBloomFilter") RBloomFilter<Long> commentRBloomFilter,
                AmsArticleMapper amsArticleMapper,
                AmsCommentMapper amsCommentMapper
            )
    {
        this.redissonClient=redissonClient;
        this.articleRBloomFilter=articleRBloomFilter;
        this.commentRBloomFilter=commentRBloomFilter;

        this.amsArticleMapper=amsArticleMapper;
        this.amsCommentMapper=amsCommentMapper;

    }



    final int batchSize = 50;


    @PostConstruct
    public void initBloomFilterOnStartup() {
        //順序執行，避免同時初始化導致Redis壓力過大
        setrBloomFilter(RedisBloomFilterEnum.READY_BLOOM_ARTICLE.getPattern(),RedisLockKey.BLOOM_INIT_ARTICLE_LOCK.getKey(), articleRBloomFilter,this::fetchArticleIdsBatch);
        setrBloomFilter(RedisBloomFilterEnum.READY_BLOOM_COMMENT.getPattern(),RedisLockKey.BLOOM_INIT_COMMENT_LOCK.getKey(),commentRBloomFilter,this::fetchCommentIdsBatch);
    }

    // 在應用啟動時初始化布隆過濾器

    public void setrBloomFilter(String readyKey,String lockKey,RBloomFilter<Long> rBloomFilter,Function<Long,List<Long>> fetchIdsFunction) {
        log.info("開始初始化布隆過濾器...");

        RBucket<String> readyBucket = redissonClient.getBucket(readyKey);
        // 1) 僅在不存在時才初始化

        String isReadyBucket = readyBucket.get();
        log.info("isReadyBucket :{}",isReadyBucket);
        if ("1".equals(readyBucket.get())) {
            log.info("Bloom 已存在於 Redis，跳過加載");
            return;
        }

        String rBloomFilterName = rBloomFilter.getName();
        /// TODO增加超時以及重試機制

        boolean locked = false;

        RLock lock = redissonClient.getLock(lockKey);

        try {
            locked = lock.tryLock(500, 0, TimeUnit.MILLISECONDS);

            //拿不到鎖，輪循等待其他人初始化完成
            if(!locked){
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(60);
                while(System.nanoTime() < deadline){
                    if("1".equals(readyBucket.get())){
                        log.info("Bloom {} 已由其他節點完成初始化",rBloomFilterName);
                        return;
                    }
                    TimeUnit.MILLISECONDS.sleep(300);
                }

                throw new CustomBaseException("等待 Bloom Filter 初始化超時: " + rBloomFilterName);
            }

            //二次檢查
            if("1".equals(readyBucket.get())){
                log.info("Bloom {} 已就緒（鎖內二次檢查），跳過初始化", rBloomFilterName);
                return;
            }

            Long lastId = 0L;
            long total = 0;
            while(true){
                List<Long> ids = fetchIdsFunction.apply(lastId);
                //若查詢結果為空 , 跳出迴圈
                if(ids.isEmpty()){
                    break;
                }

                rBloomFilter.add(ids);

                lastId = ids.get(ids.size()-1);
                total += ids.size();
                // 使用 debug 記錄批次處理進度
                log.debug("布隆過濾器: {}, 已處理批次，當前總數: {}, 最後ID: {}",rBloomFilterName, total, lastId);
            }
            // 移到迴圈外面，只在真正完成時輸出一次
            log.info("布隆過濾器: {}, 初始化完成，總共加載 {} 條數據",rBloomFilterName, total);

            readyBucket.set("1");



            /// TODO移除(測試環境避免Redis大量開銷)
            // 2) 設為永不過期（若曾設 TTL）
            //暫時使用永久保存(假設Redis中還存在值則不再次調用加載布隆過濾器)
            rBloomFilter.clearExpire();
            readyBucket.clearExpire();
        }
        catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.error("Bloom {} 初始化被中斷", rBloomFilterName, ie);
            throw new CustomBaseException("初始化中斷: " + rBloomFilterName);
        }
        catch (Exception e) {
            log.error("初始化布隆過濾器失敗: {}", e.getMessage());
            throw new CustomBaseException(e.getMessage());
        }

        finally {
            //若當前執行緒持有鎖則解鎖
            if(locked && lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }

    private List<Long> fetchArticleIdsBatch(Long lastId) {

        return amsArticleMapper.selectObjs(new LambdaQueryWrapper<AmsArticle>()
                .select(AmsArticle::getId)//只查詢ID欄位
                .gt(lastId != 0, AmsArticle::getId, lastId)//當lastId不等於0時啟用, 查詢ID大於lastId的記錄
                .orderByAsc(AmsArticle::getId)//按照id遞增排序
                .last("LIMIT " + batchSize));//批次查詢限制數量
    }

    public List<Long> fetchCommentIdsBatch(Long lastId){
        return amsCommentMapper.selectObjs(new LambdaQueryWrapper<AmsComment>()
                .select(AmsComment::getId)
                .gt(lastId!=0,AmsComment::getId,lastId)
                .orderByAsc(AmsComment::getId)
                .last("LIMIT "+batchSize)
        );
    }

//    // 在應用啟動時初始化布隆過濾器
//    @PostConstruct
//    public void initBloomFilter() {
//        log.info("開始初始化布隆過濾器...");
//
//        try {
//            final int batchSize = 1000;
//            Long lastId = 0L;
//            long total = 0;
//            while(true){
//                List<AmsArticle> amsArticles = amsArticleMapper.selectList(new LambdaQueryWrapper<AmsArticle>()
//                        .select(AmsArticle::getId)//只查詢ID欄位
//                        .gt(lastId != 0, AmsArticle::getId, lastId)//當lastId不等於0時啟用, 查詢ID大於lastId的記錄
//                        .orderByAsc(AmsArticle::getId)//按照id遞增排序
//                        .last("LIMIT " + batchSize));//批次查詢限制數量
//                //若查詢結果為空 , 跳出迴圈
//                if(amsArticles.isEmpty()){
//                    break;
//                }
//
//                for(AmsArticle article : amsArticles){
//                    //將文章ID加入布隆過濾器中
//                    Long articleId = article.getId();
//                    if(articleId!=null){
//                        rBloomFilter.add(articleId);
//                        //設置lastId為當前文章ID, 用於下一次查詢
//                        lastId = article.getId();
//                        total++;
//                    }
//                    log.debug("布隆過濾器已處理批次，當前總數: {}", total);
//
//                }
//                log.info("布隆過濾器初始化完成，總共加載 {} 條數據", total);
//            }
//        } catch (Exception e) {
//            log.error("初始化布隆過濾器失敗: {}", e.getMessage());
//            throw new CustomBaseException(e.getMessage());
//        }
//
//
//    }
}
