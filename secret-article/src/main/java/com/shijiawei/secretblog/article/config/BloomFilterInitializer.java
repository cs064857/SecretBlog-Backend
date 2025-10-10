package com.shijiawei.secretblog.article.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shijiawei.secretblog.article.entity.AmsArticle;
import com.shijiawei.secretblog.article.mapper.AmsArticleMapper;
import com.shijiawei.secretblog.common.exception.CustomBaseException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.RedissonBloomFilter;
import org.redisson.api.RBloomFilter;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * ClassName: BloomFilterInitializer
 * Description:
 *
 * @Create 2025/10/9 下午8:04
 */
@Slf4j
@Component
public class BloomFilterInitializer {

    @Resource
    private RBloomFilter<Long> rBloomFilter;

    @Resource
    private AmsArticleMapper amsArticleMapper;

    // 在應用啟動時初始化布隆過濾器
    @PostConstruct
    public void initBloomFilter() {
        log.info("開始初始化布隆過濾器...");

        /// TODO移除(測試環境避免Redis大量開銷)
        //暫時使用永久保存(假設Redis中還存在值則不再次調用加載布隆過濾器)
        // 1) 僅在不存在時才初始化
        if (rBloomFilter.isExists() && rBloomFilter.count() > 0) {
            log.info("Bloom 已存在於 Redis，跳過加載");
            return;
        }
        // 2) 設為永不過期（若曾設 TTL）
        rBloomFilter.clearExpire();




        /// TODO增加超時以及重試機制

        try {
            final int batchSize = 50;
            Long lastId = 0L;
            long total = 0;
            while(true){
                List<Long> articleIdList = amsArticleMapper.selectObjs(new LambdaQueryWrapper<AmsArticle>()
                        .select(AmsArticle::getId)//只查詢ID欄位
                        .gt(lastId != 0, AmsArticle::getId, lastId)//當lastId不等於0時啟用, 查詢ID大於lastId的記錄
                        .orderByAsc(AmsArticle::getId)//按照id遞增排序
                        .last("LIMIT " + batchSize));//批次查詢限制數量
                //若查詢結果為空 , 跳出迴圈
                if(articleIdList.isEmpty()){
                    break;
                }

                rBloomFilter.add(articleIdList);

                lastId = articleIdList.get(articleIdList.size()-1);
                total += articleIdList.size();
                // 使用 debug 記錄批次處理進度
                log.debug("布隆過濾器已處理批次，當前總數: {}, 最後ID: {}", total, lastId);
            }
            // 移到迴圈外面，只在真正完成時輸出一次
            log.info("布隆過濾器初始化完成，總共加載 {} 條數據", total);
        } catch (Exception e) {
            log.error("初始化布隆過濾器失敗: {}", e.getMessage());
            throw new CustomBaseException(e.getMessage());
        }


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
