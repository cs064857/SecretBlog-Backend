package com.shijiawei.secretblog.article.service.impl;

import com.alibaba.nacos.common.utils.JacksonUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.shijiawei.secretblog.article.annotation.DelayDoubleDelete;
import com.shijiawei.secretblog.article.annotation.OpenCache;
import com.shijiawei.secretblog.article.annotation.OpenLog;
import com.shijiawei.secretblog.article.entity.AmsArticle;
import com.shijiawei.secretblog.article.service.AmsArticleService;
import com.shijiawei.secretblog.article.mapper.AmsArticleMapper;
import com.shijiawei.secretblog.article.vo.AmsSaveArticleVo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author User
 * @description 针对表【ams_article(文章內容)】的数据库操作Service实现
 * @createDate 2024-08-26 00:17:06
 */
@Slf4j
@Service
public class AmsArticleServiceImpl extends ServiceImpl<AmsArticleMapper, AmsArticle> implements AmsArticleService {

    @Autowired
    RedissonClient redissonClient;
    static final String key ="articles";
//    @Autowired
//    private RedisCacheManager cacheManager;

    @OpenLog//開啟方法執行時間紀錄
    @DelayDoubleDelete(key = "articles",delay = 5,timeUnit = TimeUnit.SECONDS)//AOP延遲雙刪
    @Override
    public void saveArticles(AmsSaveArticleVo amsSaveArticleVo) {
        AmsArticle amsArticle = new AmsArticle();
        amsArticle.setTitle(amsSaveArticleVo.getTitle());
        amsArticle.setCategoryId(amsSaveArticleVo.getCategoryId());
        amsArticle.setContent(amsSaveArticleVo.getContent());

        //TODO 新增文章添加用戶ID與TAGID
//        amsArticle.setUserId(1L);
//        amsArticle.setTagId(1L);

        this.baseMapper.insert(amsArticle);
    }

    //-----------------------------------------------------------------------------
//    @Override
//    public List<AmsArticle> getListArticle() {
//        List<AmsArticle> articles = (List<AmsArticle>)cacheManager.getCache("articles").get("articles").get();
//        if(CollectionUtils.isEmpty(articles)){
//            articles =getListArticleFromDB();
//        }
//        return articles;
//    }
//
//    /**
//     * 獲取文章列表數據,使用StringCodec.INSTANCE
//     * @return
//     */
//    @Cacheable(value = "articles")
//    @OpenLog
//    public List<AmsArticle> getListArticleFromDB() {
//        try {
//            if(redissonClient.getLock("articles_Lock").tryLock()){
//                List<AmsArticle> articles = (List<AmsArticle>)cacheManager.getCache("articles").get("articles").get();
//                if(articles != null){
//                    return articles;
//                }
//
//                articles = this.baseMapper.selectList(new LambdaQueryWrapper<AmsArticle>()
//                        .eq(AmsArticle::getDeleted, 0));
//
//                cacheManager.getCache("articles").put("articles", articles);
//
//                return articles;
//            }else {
//                return null;
//            }
//        } finally {
//            redissonClient.getLock("articles_Lock").forceUnlock();
//        }
//-----------------------------------------------------------------------------


    /**
     * 獲取文章列表數據,使用StringCodec.INSTANCE
     * @return
     */
    @OpenLog
    @OpenCache(prefix = "test",key = "articles",time = 30,chronoUnit = ChronoUnit.MINUTES)
    @Override
    public List<AmsArticle> getListArticle() {
        List<AmsArticle> articles = this.baseMapper.selectList(new LambdaQueryWrapper<AmsArticle>()
                        .eq(AmsArticle::getDeleted, 0));

        return articles;
    }


//    /**
//      * 獲取文章列表數據,使用StringCodec.INSTANCE
//      * @return
//      */
//    @OpenLog
//    @Override
//    public List<AmsArticle> getListArticle() {
//        List<AmsArticle> articles = new ArrayList<>();
//        //試圖從Redis緩存中獲得文章列表數據
//        String redisCache = (String) redissonClient.getBucket(key).get();
//        //若成功獲得文章列表數據
//        if (redisCache!=null) {
//            articles = JacksonUtils.toObj(redisCache, new TypeReference<List<AmsArticle>>() {});
//            log.info("緩存命中...");
//            return articles;
//        }
//
//        //若Redis沒有緩存該資料須查詢資料時,加上分散式鎖,只放一名用戶進入資料庫中查詢,解決緩存擊穿問題
//        if(redissonClient.getLock("articles_Lock").tryLock()){
//            try {
//                log.info("獲得Redisson分散式鎖...查詢資料庫中...");
//                // 若Redis緩存中未存在該數據,則至資料庫中查詢,並保存至Redis緩存中
//                articles = this.baseMapper.selectList(new LambdaQueryWrapper<AmsArticle>()
//                        .eq(AmsArticle::getDeleted, 0));
//
//                if (!CollectionUtils.isEmpty(articles)) {
//                    //將資料保存至Redis緩存中,過期時間為24小時
//                    redissonClient.getBucket(key).set(JacksonUtils.toJson(articles), Duration.ofHours(24));
//                }
//            } finally {
//                //強制解鎖
//                redissonClient.getLock("articles_Lock").forceUnlock();
//            }
//        }
//        return articles;
//    }



//-----------------------------------------------------------------------------

//        List<AmsArticle> articles = new ArrayList<>();
//        //試圖從Redis緩存中獲得文章列表數據
//        String redisCache = (String) redissonClient.getBucket(key).get();
//        //若成功獲得文章列表數據
//        if (redisCache!=null) {
//            articles = JacksonUtils.toObj(redisCache, new TypeReference<List<AmsArticle>>() {});
////            log.info("執行getListArticle時緩存命中,articles:{}", articles);
//            log.info("緩存命中...");
//            return articles;
//        }
//
//        //若Redis沒有緩存該資料須查詢資料時,加上分散式鎖,只放一名用戶進入資料庫中查詢,解決緩存擊穿問題
//        if(redissonClient.getLock("articles_Lock").tryLock()){
//            try {
//                log.info("獲得Redisson分散式鎖...");
//                articles = getListArticleFromDB();
//            } finally {
//                //強制解鎖
//                redissonClient.getLock("articles_Lock").forceUnlock();
//            }
//        }
//        return articles;
//    }






//    /**
//     * 獲取文章列表數據,使用StringCodec.INSTANCE
//     * @return
//     */
//    @OpenLog
//    @Override
//    public List<AmsArticle> getListArticle() {
//        List<AmsArticle> articles = new ArrayList<>();
//        //試圖從Redis緩存中獲得文章列表數據
//        String redisCache = (String) redissonClient.getBucket(key).get();
//        //若成功獲得文章列表數據
//        if (redisCache!=null) {
//            articles = JacksonUtils.toObj(redisCache, new TypeReference<List<AmsArticle>>() {});
////            log.info("執行getListArticle時緩存命中,articles:{}", articles);
//            log.info("緩存命中...");
//            return articles;
//        }
//
//        //若Redis沒有緩存該資料須查詢資料時,加上分散式鎖,只放一名用戶進入資料庫中查詢,解決緩存擊穿問題
//        if(redissonClient.getLock("articles_Lock").tryLock()){
//            try {
//                log.info("獲得Redisson分散式鎖...");
//                articles = getListArticleFromDB();
//            } finally {
//                //強制解鎖
//                redissonClient.getLock("articles_Lock").forceUnlock();
//            }
//        }
//        return articles;
//    }
//
//    /**
//     * 獲取文章列表數據,使用StringCodec.INSTANCE
//     * @return
//     */
//    public List<AmsArticle> getListArticleFromDB() {
//        log.info("查詢資料庫中...");
//        // 若Redis緩存中未存在該數據,則至資料庫中查詢,並保存至Redis緩存中
//        List<AmsArticle> articles = this.baseMapper.selectList(new LambdaQueryWrapper<AmsArticle>()
//                .eq(AmsArticle::getDeleted, 0));
//
//        if (!CollectionUtils.isEmpty(articles)) {
//            //將資料保存至Redis緩存中,過期時間為24小時
//            redissonClient.getBucket(key).set(JacksonUtils.toJson(articles), Duration.ofHours(24));
//        }
//        return articles;
//    }

//-----------------------------------------------------------------------------














    //    /**
//     * 獲取文章列表數據,使用JsonJacksonCodec
//     * @return
//     */
//    @Override
//    public List<AmsArticle> getListArticle() {
//        // 使用泛型方式來獲取列表
//        RBucket<List<AmsArticle>> bucket = redissonClient.getBucket(key);
//        List<AmsArticle> articles = bucket.get();
//
//        if (!CollectionUtils.isEmpty(articles)) {
//            log.info("執行getListArticle時緩存命中,articles:{}", articles);
//            return articles;
//        }
//
//        // 若Redis緩存中未存在該數據,則至資料庫中查詢,並保存至Redis緩存中
//        articles = this.baseMapper.selectList(new LambdaQueryWrapper<AmsArticle>()
//                .eq(AmsArticle::getDeleted, 0));
//
//        if (!CollectionUtils.isEmpty(articles)) {
//            bucket.set(articles, Duration.ofHours(24));
//        }
//
//        log.info("執行getListArticle時查詢資料庫,articles:{}", articles);
//        return articles;
//    }
}





