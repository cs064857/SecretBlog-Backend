package com.shijiawei.secretblog.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shijiawei.secretblog.article.entity.AmsCommentStatistics;
import com.shijiawei.secretblog.article.entity.AmsTags;
import com.shijiawei.secretblog.article.mapper.AmsTagsMapper;
import com.shijiawei.secretblog.article.service.AmsTagsService;
import com.shijiawei.secretblog.article.vo.AmsArticleStatusVo;
import com.shijiawei.secretblog.common.annotation.OpenCache;
import com.shijiawei.secretblog.common.exception.CustomBaseException;
import com.shijiawei.secretblog.common.myenum.RedisCacheKey;
import com.shijiawei.secretblog.common.myenum.RedisLockKey;
import com.shijiawei.secretblog.common.myenum.RedisOpenCacheKey;
import com.shijiawei.secretblog.common.utils.RedisCacheLoaderUtils;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBatch;
import org.redisson.api.RMap;
import org.redisson.api.RMapAsync;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JacksonCodec;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.hash.ObjectHashMapper;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ClassName: AmsTagsServiceImpl
 * Description:
 *
 * @Create 2025/7/28 下午10:28
 */
@Slf4j
@Service
public class AmsTagsServiceImpl extends ServiceImpl<AmsTagsMapper, AmsTags> implements AmsTagsService {

    @Autowired
    private RedisCacheLoaderUtils redisCacheLoaderUtils;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private ObjectMapper objectMapper; // 已註冊 JavaTimeModule
    @Override
    public void createArtTag(String name) {

            AmsTags amsTags = new AmsTags();
            amsTags.setName(name);
            log.info("amsTags:{}",amsTags);


            this.baseMapper.insert(amsTags);
             ///TODO 處理失敗異常

        }

    @Override
    public List<AmsTags> getArtTags() {
        log.warn("執行資料庫查詢所有標籤");

        return this.baseMapper.selectList(new QueryWrapper<>());

    }

    @Override
    public Map<Long, AmsTags> getArtTagsByIds(Set<Long> set) {
        log.info("開始執行 getArtTagsByIds - set: {}", set);

        String redisKey = RedisCacheKey.ARTICLE_TAGS.getPattern();
        log.info("redisKey: {}", redisKey);

        //嘗試從Redis取得文章的指標
        //初步嘗試從Redis中讀取文章的指標
        Map<Long, AmsTags> amsTagsList = this.parseArtTagsByIds(set, redisKey);

        //判斷是否成功從Redis中獲取文章的指標或articleStatusVo中其中某個欄位是否為null
        if (amsTagsList==null) {
            log.info("Redis 快取未命中,從資料庫載入 - set: {}", set);
            /*
            假設未成功從Redis獲取文章的指標，或者指標鍵值有遺漏
             */
            //從DB資料庫中查詢
            amsTagsList = redisCacheLoaderUtils.loadMapWithLock(
                    () -> loadArtTagsByIds(set, redisKey),
                    () -> this.parseArtTagsByIds(set, redisKey),
                    3,
                    10,
                    TimeUnit.SECONDS,
                    3,
                    RedisLockKey.ARTICLE_TAGS_LOCK.getPattern(),
                    RedisCacheKey.ARTICLE_TAGS.getPattern()
            );




        }
        /*
        假設成功從Redis快取中獲取文章的指標則直接進行包裝並回傳
         */
        log.info("文章的包裝後指標資訊 size:{}", amsTagsList.size());
        return amsTagsList;
    }

    private Map<Long, AmsTags> parseArtTagsByIds(Set<Long> set ,String redisKey) {
//        log.info("開始執行 parseArticleStatusFromRedis - articleId: {}", articleId);
        //嘗試從Redis取得文章的指標

        RMap<Long, AmsTags> map = redissonClient.getMap(redisKey,new TypedJsonJacksonCodec(Long.class,AmsTags.class,objectMapper));

        Map<Long, AmsTags> amsTagsMap = map.getAll(set);


        if(amsTagsMap.isEmpty()){
            return null;
        }

        log.info("amsTagsMap size: {}", amsTagsMap.size());
        return amsTagsMap;
    }

    public Map<Long, AmsTags> loadArtTagsByIds(Set<Long> set,String redisKey){
        log.info("開始執行獲取文章中所有留言的指標 - setSize: {}", set.size());


        List<AmsTags> amsTagsList = getArtTags();
        log.info("資料庫查詢完成 - 標籤數量: {}", amsTagsList.size());

        log.debug("Redis Key 資訊 - pattern: {}", redisKey);

        RMap<Long, AmsTags> map = redissonClient.getMap(redisKey,new TypedJsonJacksonCodec(Long.class,AmsTags.class,objectMapper));
        //判斷是否成功從資料庫中取得該文章所有留言的指標
        if(amsTagsList.isEmpty()){
            /*
            假設未成功從資料庫中取得該文章所有留言的指標，則寫入空快取，避免快取穿透，並設置TTL為3分鐘
             */
            log.warn("資料庫查詢無留言資料,寫入空快取標記防止快取穿透 - setSize: {}, TTL: 3分鐘", set.size());


            //創建空快取標記
            Map<Long, AmsTags> emptyMap = new HashMap<>();
            emptyMap.put(-1L, new AmsTags());  // 創建空緩存標記


            //將快取標記寫入資料庫, 過期時間為3分鐘
            map.putAll(emptyMap);
            map.expire(Duration.ofMinutes(3));


            return emptyMap;
        }
        //假設成功從資料庫中取得該文章所有留言的指標，則寫入快取

        log.debug("處理留言指標資料 amsTagsList: {}",amsTagsList);

//        Map<Long, List<AmsTags>> likesMap = amsTagsList.stream().collect(Collectors.groupingBy(AmsTags::getId));
        Map<Long, AmsTags> likesMap = amsTagsList.stream().collect(Collectors.toMap(AmsTags::getId,Function.identity()));



        //包裝成目標對象
        map.putAll(likesMap);
        map.expire(Duration.ofMinutes(30));


        log.info("成功獲取文章中所有留言的指標並且入至快取中 - setSize: {}", set.size());

        return likesMap;

    }

//    public List<AmsTags> QueryArtTagsByIds(Set<Long> set){
//        log.debug("執行資料庫查詢所有標籤 - setSize: {}", set.size());
//        return this.baseMapper.selectList(new LambdaQueryWrapper<>());
//    }

}
