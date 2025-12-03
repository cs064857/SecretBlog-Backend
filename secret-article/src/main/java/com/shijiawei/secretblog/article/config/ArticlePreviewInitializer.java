//package com.shijiawei.secretblog.article.config;
//
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import com.google.common.collect.Lists;
//import com.shijiawei.secretblog.article.entity.AmsArtTag;
//import com.shijiawei.secretblog.article.entity.AmsArticle;
//import com.shijiawei.secretblog.article.entity.AmsArtinfo;
//import com.shijiawei.secretblog.article.entity.AmsCategory;
//import com.shijiawei.secretblog.article.mapper.AmsArticleMapper;
//import com.shijiawei.secretblog.article.service.AmsArtTagService;
//import com.shijiawei.secretblog.article.service.AmsArticleService;
//import com.shijiawei.secretblog.article.service.AmsArtinfoService;
//import com.shijiawei.secretblog.article.service.AmsCategoryService;
//import com.shijiawei.secretblog.article.vo.AmsArticlePreviewVo;
//import com.shijiawei.secretblog.common.codeEnum.ResultCode;
//import com.shijiawei.secretblog.common.exception.BusinessRuntimeException;
//import jakarta.annotation.PostConstruct;
//import lombok.extern.slf4j.Slf4j;
//import org.redisson.api.*;
//import org.redisson.client.RedisException;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.util.CollectionUtils;
//
//import java.time.ZoneId;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
///**
// * ClassName: ArticlePreviewInitializer
// * Description:Redis索引預熱器，
// * 在應用啟動時將文章的分類和標籤關係從數據庫加載到Redis中，
// * 建立高效的索引結構，以支持後續的文章查詢和瀏覽功能。
// *
// * @Create 2025/12/3 下午4:24
// */
//@Slf4j
//@Configuration
//public class ArticlePreviewInitializer {
//
//
//    private final RedissonClient redissonClient;
//    private final AmsArtinfoService amsArtinfoService;
//    private final AmsArtTagService amsArtTagService;
//    private final AmsCategoryService amsCategoryService;
//    private final AmsArticleMapper amsArticleMapper;
//    private final AmsArticleService amsArticleService;
//
//    public ArticlePreviewInitializer(
//            RedissonClient redissonClient ,
//            AmsArtinfoService amsArtinfoService ,
//            AmsArtTagService amsArtTagService ,
//            AmsCategoryService amsCategoryService ,
//            AmsArticleMapper amsArticleMapper,
//            AmsArticleService amsArticleService){
//
//        this.redissonClient = redissonClient;
//        this.amsArtinfoService = amsArtinfoService;
//        this.amsArtTagService = amsArtTagService;
//        this.amsCategoryService = amsCategoryService;
//        this.amsArticleMapper = amsArticleMapper;
//        this.amsArticleService = amsArticleService;
//    }
//
//    @PostConstruct
//    private void init(){
//
//        log.info("開始初始化文章索引（分類與標籤）...");
//
//
//
//
//        /**
//         * 重建所有分類索引
//         */
//        List<Long> categoryIds = getAllCategoryIds();
//        log.info("準備重建分類索引，分類數量: {}", categoryIds.size());
//        // 重建所有分類索引
//        categoryIds.forEach(categoryId -> {
//            log.debug("開始重建分類索引，categoryId={}", categoryId);
//            boolean result = rebuildArticleToCategoryIndex(categoryId);
//            if (!result) {
//                log.error("分類索引重建失敗，categoryId={}", categoryId);
//                throw BusinessRuntimeException.builder()
//                        .iErrorCode(ResultCode.INIT_INDEX_FAILED)
//                        .detailMessage(String.format("分類索引重建失敗，分類ID: %d", categoryId))
//                        .build();
//            }
//            log.debug("分類索引重建完成，categoryId={}", categoryId);
//        });
//
//        /**
//         * 重建所有標籤索引
//         */
//        List<Long> tagIds = getAllTagIds();
//        log.info("準備重建標籤索引，標籤數量: {}", tagIds.size());
//
//        tagIds.forEach(tagId -> {
//            log.debug("開始重建標籤索引，tagId={}", tagId);
//            boolean result = rebuildArticleToTagIndex(tagId);
//            if (!result) {
//                log.error("標籤索引重建失敗，tagId={}", tagId);
//                throw BusinessRuntimeException.builder()
//                        .iErrorCode(ResultCode.INIT_INDEX_FAILED)
//                        .detailMessage(String.format("標籤索引重建失敗，標籤ID: %d", tagId))
//                        .build();
//            }
//            log.debug("標籤索引重建完成，tagId={}", tagId);
//        });
//
//        log.info("文章索引初始化完成。");
//
//        /**
//         * 重建所有文章預覽索引
//         */
//        List<Long> articleIds = this.getAllArticleIds();
//        log.info("準備重建文章預覽索引，文章數量: {}", articleIds.size());
//
//        boolean previewIndex = rebuildArticleToArticlePreviewIndex(articleIds);
//        if(!previewIndex){
//            log.error("文章預覽索引重建失敗");
//            throw BusinessRuntimeException.builder()
//                    .iErrorCode(ResultCode.INIT_INDEX_FAILED)
//                    .detailMessage("文章預覽索引重建失敗")
//                    .build();
//        }
//    }
//
//    private boolean rebuildArticleToCategoryIndex(Long categoryId){
//
//        //透過分類查詢該分類下的所有文章ID
//
//        String categoryKey = "idx:category:" + categoryId;
//        log.debug("使用 Redis key 重建分類索引，categoryKey={}", categoryKey);
//
//        long lastId = 0L; // 上一次查詢的最後一筆 ID
//        final int batchSize = 10;
//        int fetchSize = 0;
//        int totalCount = 0;
//
//        RScoredSortedSet<Long> sortedSet = redissonClient.getScoredSortedSet(categoryKey);
//        do {
//            List<AmsArtinfo> artinfos = amsArtinfoService.list(new LambdaQueryWrapper<AmsArtinfo>()
//                    .eq(AmsArtinfo::getCategoryId, categoryId)
//                    .gt(AmsArtinfo::getId, lastId)
//                    .orderByAsc(AmsArtinfo::getId)
//                    .last("LIMIT " + batchSize));
//
//            fetchSize = artinfos.size();
//            if (fetchSize > 0) {
//                lastId = artinfos.get(artinfos.size() - 1).getId();
//                totalCount += fetchSize;
//
//                Map<Long, Double> map = artinfos.stream().collect(Collectors.toMap(AmsArtinfo::getArticleId, article -> (double)
//                        article.getUpdateTime().atZone(ZoneId.systemDefault())
//                                .toInstant()
//                                .toEpochMilli()
//
//                ));
//                sortedSet.addAll(map);
//                log.debug("分類索引批次寫入，categoryId={}，本批處理數量={}，lastId={}", categoryId, fetchSize, lastId);
//            }
//        } while (fetchSize == batchSize);
//
//        log.info("分類索引重建完成，categoryId={}，共處理文章數量={}", categoryId, totalCount);
//        return true;
//    }
//
//    private List<Long> getAllCategoryIds(){
//        List<AmsCategory> amsCategoryList = amsCategoryService.getAllCategoryIds();
//        if(CollectionUtils.isEmpty(amsCategoryList)){
//            log.warn("未找到任何分類，無法初始化分類索引");
//            throw BusinessRuntimeException.builder()
//                    .iErrorCode(ResultCode.NOT_FOUND)
//                    .detailMessage("未找到任何分類")
//                    .build();
//        }
//        List<Long> categoryIds = amsCategoryList.stream().map(AmsCategory::getId).collect(Collectors.toList());
//        log.debug("獲取所有分類ID成功，數量={}，ids={}", categoryIds.size(), categoryIds);
//        return categoryIds;
//    }
//
//    private List<Long> getAllTagIds(){
//        List<AmsArtTag> amsArtTagList = amsArtTagService.getAllDistinctTagIds();
//        if(CollectionUtils.isEmpty(amsArtTagList)){
//            log.warn("未找到任何標籤，無法初始化標籤索引");
//            throw BusinessRuntimeException.builder()
//                    .iErrorCode(ResultCode.NOT_FOUND)
//                    .detailMessage("未找到任何標籤")
//                    .build();
//        }
//        List<Long> tagIds = amsArtTagList.stream().map(AmsArtTag::getTagsId).collect(Collectors.toList());
//        log.debug("獲取所有標籤ID成功，數量={}，ids={}", tagIds.size(), tagIds);
//        return tagIds;
//    }
//
//    private List<Long> getAllArticleIds(){
//        List<AmsArticle> amsArtTagList = amsArticleService.getAllDistinctArticleIds();
//        if(CollectionUtils.isEmpty(amsArtTagList)){
//            log.warn("未找到任何文章，無法初始化文章索引");
//            throw BusinessRuntimeException.builder()
//                    .iErrorCode(ResultCode.NOT_FOUND)
//                    .detailMessage("未找到任何文章")
//                    .build();
//        }
//        List<Long> articleIds = amsArtTagList.stream().map(AmsArticle::getId).collect(Collectors.toList());
//        log.debug("獲取所有文章ID成功，數量={}，ids={}", articleIds.size(), articleIds);
//        return articleIds;
//    }
//
//    private boolean rebuildArticleToTagIndex(Long tagId){
//
//        String tagKey = "idx:tag:" + tagId;
//        log.debug("使用 Redis key 重建標籤索引，tagKey={}", tagKey);
//
//        long lastId = 0L; // 上一次查詢的最後一筆 ID
//        final int batchSize = 10;
//        int fetchSize = 0;
//        int totalCount = 0;
//
//        RSet<Long> set = redissonClient.getSet(tagKey);
//        do {
//            List<AmsArtTag> amsArtTagList = amsArtTagService.list(new LambdaQueryWrapper<AmsArtTag>()
//                    .select(AmsArtTag::getId,AmsArtTag::getArticleId)
//                    .eq(AmsArtTag::getTagsId, tagId)
//                    .gt(AmsArtTag::getId, lastId)
//                    .orderByAsc(AmsArtTag::getId)
//                    .last("LIMIT " + batchSize));
//
//            fetchSize = amsArtTagList.size();
//            if (fetchSize > 0) {
//                lastId = amsArtTagList.get(amsArtTagList.size() - 1).getId();
//                totalCount += fetchSize;
//
//                set.addAll(amsArtTagList.stream().map(AmsArtTag::getArticleId).toList());
//                log.debug("標籤索引批次寫入，tagId={}，本批處理數量={}，lastId={}", tagId, fetchSize, lastId);
//            }
//        } while (fetchSize == batchSize);
//
//        log.info("標籤索引重建完成，tagId={}，共處理文章數量={}", tagId, totalCount);
//        return true;
//    }
//
//    private boolean rebuildArticleToArticlePreviewIndex(List<Long> articleIds){
//
//        final int batchSize = 3;
//        List<List<Long>> partition = Lists.partition(articleIds, batchSize);
//        int totalSynced = 0;
//        for (List<Long> part : partition) {
//            try {
//                List<AmsArticlePreviewVo> amsArticlePreviewVoList = amsArticleMapper.getArticlesPreviewPageByArticleIds(part);
//                if(CollectionUtils.isEmpty(amsArticlePreviewVoList)){
//                    log.warn("未找到任何文章預覽，無法初始化文章預覽索引");
//                    throw BusinessRuntimeException.builder()
//                            .iErrorCode(ResultCode.NOT_FOUND)
//                            .detailMessage("未找到任何文章預覽")
//                            .build();
//                }
//
//
//                RBatch rBatch = redissonClient.createBatch();
//
//                amsArticlePreviewVoList.forEach(amsArticlePreviewVo -> {
//                    String articleKey = "idx:article:preview:" + amsArticlePreviewVo.getArticleId();
//                    RBucketAsync<AmsArticlePreviewVo> rBatchBucket = rBatch.getBucket(articleKey);
//                    rBatchBucket.setAsync(amsArticlePreviewVo);
//                });
//                BatchResult<?> batchResult = rBatch.execute();
//
//                int syncedSlaves = batchResult.getSyncedSlaves();
//                totalSynced+=amsArticlePreviewVoList.size();
//                log.debug("批次文章預覽索引重建完成，批次數量={}，批次同步副本數量={}", part.size(), syncedSlaves);
//            } catch (RedisException e) {
//                log.error("批次文章預覽索引重建發生錯誤，批次數量={}，錯誤訊息={}", part.size(), e.getMessage(), e);
//                throw BusinessRuntimeException.builder()
//                        .iErrorCode(ResultCode.INIT_INDEX_FAILED)
//                        .detailMessage("批次文章預覽索引重建發生錯誤")
//                        .build();
//            }
//
//        }
//
//        log.info("文章預覽索引重建全部完成，articleId數量={}，同步副本數量={}",articleIds.size(), totalSynced);
////        log.debug("文章預覽索引重建完成，articleId數量={}，共處理文章數量={}，同步副本數量={}", part.size(), amsArticlePreviewVoList.size(), syncedSlaves);
//
//        return true;
//    }
//
////    private boolean rebuildArticleToArticlePreviewIndex(List<Long> articleIds){
////
////
////
////        List<String> articleKeys = articleIds.stream().map(id -> "idx:article:preview:" + id).toList();
////
////
////        log.debug("使用 Redis key 重建文章預覽索引，articleKey={}", articleKey);
////
////        long lastId = 0L; // 上一次查詢的最後一筆 ID
////        final int batchSize = 10;
////        int fetchSize = 0;
////        int totalCount = 0;
//////
////        RBucket<AmsArticlePreviewVo> bucket = redissonClient.getBucket(articleKey);
////
////        bucket.set();
////
////        RBuckets buckets = redissonClient.getBuckets();
////
////        String[] articleKeysArray = articleKeys.toArray(new String[0]);
////
////        Map<String, AmsArticlePreviewVo> stringObjectMap = buckets.get(articleKeysArray);
////
////        List<AmsArticlePreviewVo> amsArticlePreviewVoList = amsArticleMapper.getArticlesPreviewPageByArticleIds(articleIds);
////        if(CollectionUtils.isEmpty(amsArticlePreviewVoList)){
////            log.warn("未找到任何文章預覽，無法初始化文章預覽索引");
////            throw BusinessRuntimeException.builder()
////                    .iErrorCode(ResultCode.NOT_FOUND)
////                    .detailMessage("未找到任何文章預覽")
////                    .build();
////        }
////
////        amsArticlePreviewVoList.stream().forEach(amsArticlePreviewVo -> {
////            String articleKey = "idx:article:preview:" + amsArticlePreviewVo.getArticleId();
////            stringObjectMap.set
////
////
////        });
////
////
////
////
////
////        log.info("文章預覽索引重建完成，articleId={}，共處理文章數量={}", articleId, totalCount);
////        return true;
////    }
//}
