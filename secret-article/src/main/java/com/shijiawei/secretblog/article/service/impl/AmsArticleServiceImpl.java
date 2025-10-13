package com.shijiawei.secretblog.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shijiawei.secretblog.article.annotation.DelayDoubleDelete;
import com.shijiawei.secretblog.article.entity.*;
import com.shijiawei.secretblog.article.feign.UserFeignClient;


import com.shijiawei.secretblog.article.service.*;
import com.shijiawei.secretblog.article.vo.AmsArticleVo;

import com.shijiawei.secretblog.common.myenum.RedisBloomFilterKey;
import com.shijiawei.secretblog.common.myenum.RedisCacheKey;
import com.shijiawei.secretblog.common.annotation.OpenCache;
import com.shijiawei.secretblog.common.dto.UserBasicDTO;
import com.shijiawei.secretblog.article.vo.AmsArticlePreviewVo;
import com.shijiawei.secretblog.article.annotation.OpenLog;
import com.shijiawei.secretblog.article.mapper.AmsArticleMapper;
import com.shijiawei.secretblog.article.vo.AmsSaveArticleVo;
import com.shijiawei.secretblog.common.exception.CustomBaseException;
import com.shijiawei.secretblog.common.myenum.RedisLockKey;
import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.common.utils.RedisRateLimiterUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;
import org.redisson.client.RedisConnectionException;
import org.redisson.client.RedisTimeoutException;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.shijiawei.secretblog.common.utils.UserContextHolder;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author User
 * @description 針對表【ams_article(文章內容)】的數據庫操作Service實現
 * @createDate 2024-08-26 00:17:06
 */
@Slf4j
@Service
public class AmsArticleServiceImpl extends ServiceImpl<AmsArticleMapper, AmsArticle> implements AmsArticleService {

    @Autowired
    RedissonClient redissonClient;
    @Autowired
    private AmsArtinfoService amsArtinfoService;

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private AmsArtTagService amsArtTagService;

    @Autowired
    private AmsTagsService amsTagsService;

    @Autowired
    private AmsArtStatusService amsArtStatusService;

    @Autowired
    private AmsCategoryService amsCategoryService;

    @Autowired
    private AmsCommentInfoService amsCommentInfoService;


    private final RedisRateLimiterUtils redisRateLimiterUtils;

    public AmsArticleServiceImpl(RedisRateLimiterUtils redisRateLimiterUtils) {
        this.redisRateLimiterUtils = redisRateLimiterUtils;
    }


    ////    public R logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
//
//        String originalJwtToken = null;
//        // 1) 優先：從認證物件(MyJwtAuthentication)取出在過濾器中保存的原始JWT
//        if (authentication instanceof com.shijiawei.secretblog.user.authentication.handler.login.business.MyJwtAuthentication auth) {
//            originalJwtToken = auth.getJwtToken();
//        }
//
//        log.info("Logout originalJwtToken: {}", originalJwtToken);
//
//        // 將當前 sessionId 放入黑名單（使用剩餘有效期作為TTL）
//        if (authentication != null && authentication.isAuthenticated()) {
//            Object principal = authentication.getPrincipal();
//            if (principal instanceof UserLoginInfo currentUser) {
//                long now = TimeTool.nowMilli();
//                long expiredTime = currentUser.getExpiredTime();
//                long ttl = Math.max(expiredTime - now, 1000L); // 至少1秒，避免0或負數
//                try {
//                    tokenBlacklistService.blacklist(currentUser.getSessionId(), ttl);
//                    log.info("SessionId {} 已加入黑名單, TTL={}ms", currentUser.getSessionId(), ttl);
//                } catch (Exception e) {
//                    log.warn("加入黑名單失敗: {}", e.getMessage(), e);
//                }
//            }
//        }

    @OpenLog//開啟方法執行時間紀錄
    @DelayDoubleDelete(prefix = "AmsArticles", key = "categoryId_#{#amsSaveArticleVo.categoryId}")
//    @DelayDoubleDelete(prefix = "AmsArticle",key = "articles",delay = 5,timeUnit = TimeUnit.SECONDS)//AOP延遲雙刪

    @Transactional
    @Override
    public void saveArticles(AmsSaveArticleVo amsSaveArticleVo, HttpServletRequest httpServletRequest,Authentication authentication) {
        AmsArticle amsArticle = new AmsArticle();
        AmsArtinfo amsArtinfo = new AmsArtinfo();

        // 從網關傳遞的請求標頭中取得用戶資訊
        if (!UserContextHolder.isCurrentUserLoggedIn()) {
            log.warn("未取得登入用戶資訊，拒絕發佈文章");
            throw new IllegalStateException("未登入或登入狀態已失效");
        }

        Long userId = UserContextHolder.getCurrentUserId();

        String userNameFromToken = UserContextHolder.getCurrentUserNickname();
        if (userId == null) {
            log.warn("用戶ID為空，拒絕發佈文章");
            throw new IllegalStateException("用戶ID缺失");
        }

        try {
            amsArticle.setTitle(amsSaveArticleVo.getTitle());
            amsArticle.setContent(amsSaveArticleVo.getContent());
            this.baseMapper.insert(amsArticle);

            amsArtinfo.setCategoryId(amsSaveArticleVo.getCategoryId());
            amsArtinfo.setUserName(userNameFromToken);
            amsArtinfo.setArticleId(amsArticle.getId());
            amsArtinfo.setUserId(userId);
            amsArtinfoService.save(amsArtinfo);

            List<Long> tagIds = amsSaveArticleVo.getTagsId();
            if (tagIds != null && !tagIds.isEmpty()) {
                List<AmsArtTag> amsArtTagList = tagIds.stream().map(tagsId -> {
                    AmsArtTag amsArtTag = new AmsArtTag();
                    amsArtTag.setTagsId(tagsId);
                    amsArtTag.setArticleId(amsArticle.getId());
                    return amsArtTag;
                }).toList();
                amsArtTagService.saveBatch(amsArtTagList);
            } else {
                log.debug("本次發佈未附加標籤或標籤列表為空");
            }

            AmsArtStatus amsArtStatus = new AmsArtStatus();
            amsArtStatus.setArticleId(amsArticle.getId());
            amsArtStatusService.save(amsArtStatus);


        } catch (Exception e) {
            log.error("保存文章過程失敗，將回滾事務", e);
            throw e; // 讓 @Transactional 觸發回滾
        }

        // 在事務方法的最後（確保提交前執行）
        // 使用 final 變量以便在後續使用
        final AmsArticle savedArticle = amsArticle;

        // 註冊事務同步回調，在事務提交後執行
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        saveArticleIdToBloomFilter(savedArticle);
                    }
                }
        );
    }

    private void saveArticleIdToBloomFilter(AmsArticle amsArticle) {
        //將新的文章ID新增至布隆過濾器中
        //不管是否成功加入布隆過濾器都不影響文章發佈, 不進行回滾
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(RedisBloomFilterKey.ARTICLE_BLOOM_FILTER.getKey());
        bloomFilter.add(amsArticle.getId());
    }
//    /**
//     * 獲取文章列表數據
//     * @return
//     */
//    @OpenLog
//    @OpenCache(prefix = "AmsArticle",key = "articles",time = 30,chronoUnit = ChronoUnit.MINUTES)
//    @Override
//    public List<AmsArticle> getListArticle() {
//        List<AmsArticle> articles = this.baseMapper.selectList(new LambdaQueryWrapper<AmsArticle>()
//                        .eq(AmsArticle::getDeleted, 0));
//        return articles;
//    }
//

    //    @OpenLog
//    @OpenCache(prefix = "AmsArticles", key = "categoryId_#{#categoryId}:routerPage_#{#routePage}:articles")//姝ｇ?SpEL瑾炴■,璁婃暩浣跨敤#{#璁婃暩鍚峿
    @Override
    public Page<AmsArticlePreviewVo> getArticlesByCategoryIdAndPage(Long categoryId, Integer routePage) {
        // 根據categoryId分類查詢
        Page<AmsArtinfo> amsArtinfoPage = amsArtinfoService.page(new Page<>(routePage, 20),
                new LambdaQueryWrapper<AmsArtinfo>().eq(AmsArtinfo::getCategoryId, categoryId));
        List<AmsArtinfo> amsArtinfoRecords = amsArtinfoPage.getRecords();

        // 若本頁沒有資料，直接回傳空分頁但保留分頁資訊
        if (amsArtinfoRecords == null || amsArtinfoRecords.isEmpty()) {
            Page<AmsArticlePreviewVo> empty = new Page<>(amsArtinfoPage.getCurrent(), amsArtinfoPage.getSize(), amsArtinfoPage.getTotal());
            empty.setRecords(new ArrayList<>());
            return empty;
        }

        // 從文章資訊列表獲取所有的文章ID與用戶ID
        List<Long> articleIdList = amsArtinfoRecords.stream().map(AmsArtinfo::getArticleId).collect(Collectors.toList());
        List<Long> userIdList = amsArtinfoRecords.stream().map(AmsArtinfo::getUserId).collect(Collectors.toList());

        // 查詢關聯資料
        List<AmsArticle> amsArticleList = this.baseMapper.selectBatchIds(articleIdList);
        List<AmsArtStatus> amsArtStatusList = amsArtStatusService.list(new LambdaQueryWrapper<AmsArtStatus>().in(AmsArtStatus::getArticleId, articleIdList));
        AmsCategory amsCategory = amsCategoryService.getById(categoryId);
        List<AmsArtTag> amsArtTagList = amsArtTagService.list(new LambdaQueryWrapper<AmsArtTag>().in(AmsArtTag::getArticleId, articleIdList));

        // 映射加速查找
        Map<Long, AmsArtStatus> amsArtStatusMap = amsArtStatusList.stream().collect(Collectors.toMap(AmsArtStatus::getArticleId, Function.identity(), (a, b) -> a));
        Map<Long, AmsArticle> amsArticleMap = amsArticleList.stream().collect(Collectors.toMap(AmsArticle::getId, Function.identity(), (a, b) -> a));
        Map<Long, List<AmsArtTag>> amsArtTagMap = amsArtTagList.stream().collect(Collectors.groupingBy(AmsArtTag::getArticleId));

        R<List<UserBasicDTO>> usersByIds = userFeignClient.selectUserBasicInfoByIds(userIdList);
        Map<Long, UserBasicDTO> userDTOMap = new HashMap<>();
        if (usersByIds != null && usersByIds.getData() != null) {
            userDTOMap = usersByIds.getData().stream().collect(Collectors.toMap(UserBasicDTO::getUserId, Function.identity(), (a, b) -> a));
        }

        // 組裝VO，按原 amsArtinfoRecords 順序
        List<AmsArticlePreviewVo> amsArticlePreviewVoList = new ArrayList<>();
        for (AmsArtinfo artInfo : amsArtinfoRecords) {
            AmsArticlePreviewVo vo = new AmsArticlePreviewVo();
            // artInfo 基本屬性
            BeanUtils.copyProperties(artInfo, vo);

            // article 基本屬性
            AmsArticle amsArticle = amsArticleMap.get(artInfo.getArticleId());
            vo.setTitle(amsArticle.getTitle());
            // 狀態屬性
            AmsArtStatus status = amsArtStatusMap.get(artInfo.getArticleId());
            if (status != null) {
                BeanUtils.copyProperties(status, vo);
            }
            // 使用者屬性
            UserBasicDTO userBasicDTO = userDTOMap.get(artInfo.getUserId());
            if (userBasicDTO != null) {
                BeanUtils.copyProperties(userBasicDTO, vo);
            }
            // 分類與標籤
            if (amsCategory != null) {
                vo.setCategoryName(amsCategory.getCategoryName());
            }
            vo.setAmsArtTagList(amsArtTagMap.get(artInfo.getArticleId()));

            amsArticlePreviewVoList.add(vo);
        }

        // 包裝為分頁返回
        Page<AmsArticlePreviewVo> resultPage = new Page<>(amsArtinfoPage.getCurrent(), amsArtinfoPage.getSize(), amsArtinfoPage.getTotal());
        resultPage.setRecords(amsArticlePreviewVoList);
        return resultPage;
    }

    @Override
    public AmsArticleVo getAmsArticleVoWithStatus(Long articleId){

        if(isArticleNotExists(articleId)){
            log.info("文章不存在，articleId={}",articleId);
            throw new CustomBaseException("文章不存在");
        }


        AmsArticleVo amsArticleVo = getAmsArticleVo(articleId);
        if(amsArticleVo==null){
            log.warn("無法成功獲取文章資訊，該文章可能已被刪除");
            throw new CustomBaseException("無法成功獲取文章資訊，該文章可能已被刪除");
        }
        Long amsArticleVoId = amsArticleVo.getId();
        if (amsArticleVoId == null) {
            log.warn("文章ID為空，無法獲取文章資訊");
            throw new CustomBaseException("文章ID缺失");
        }


//        try {
//            Long views = incrementArticleViewsCount(amsArticleVoId);
//            amsArticleVo.setViewsCount(Math.toIntExact(views));
//        }catch(Exception e){
//
//            /// TODO查詢資料庫中的瀏覽人數
//            AmsArtStatus amsArtStatusServiceOne = amsArtStatusService.getOne(new LambdaQueryWrapper<AmsArtStatus>().eq(AmsArtStatus::getArticleId, amsArticleVoId));
//
//            //防止無法獲取瀏覽人數時能正常顯示文章，缺點是會導致瀏覽人數為顯示0以及無法正確統計瀏覽人數
////            amsArticleVo.setViewsCount(1);
//            log.warn("無法獲取文章瀏覽人數，可能是Redis服務");
//        }

        try {
            long views = incrementArticleViewsCount(amsArticleVoId);

            // 建議 VO 改為 long，避免溢位
            amsArticleVo.setViewsCount(Math.toIntExact(views));
        } catch (RedisConnectionException | RedisTimeoutException e) {
            log.warn("Redis 計數失敗，改用 DB 回填 viewsCount，articleId={}, err={}", amsArticleVoId, e.getMessage());
        // 僅查所需欄位 + 保證唯一
            AmsArtStatus status = amsArtStatusService.getOne(
                    Wrappers.<AmsArtStatus>lambdaQuery()
                            .select(AmsArtStatus::getViewsCount)
                            .eq(AmsArtStatus::getArticleId, amsArticleVoId),
                    false // 若多筆不丟例外
            );
            int fallbackViews = status != null && status.getViewsCount() != null ? status.getViewsCount() : 0;
            amsArticleVo.setViewsCount(fallbackViews);
        } catch (Exception e) {
        // 其他未知例外：不要默默吞，應該明確告警
            log.error("計數發生非預期錯誤，articleId={}", amsArticleVoId, e);
        // 可選：最後一道防線，顯示 DB 值或 0
            amsArticleVo.setViewsCount(0);
        }


        ///TODO注意安全性
        Long articleLikes = getArticleLikes(articleId);
        Long articleBookmarks = getArticleBookmarks(articleId);
        amsArticleVo.setLikesCount(articleLikes.intValue());
        amsArticleVo.setBookmarksCount(articleBookmarks.intValue());


        return amsArticleVo;
    }

    @OpenCache(prefix = "AmsArticle",key = "ArticleVo_#{#articleId}",time = 30,chronoUnit = ChronoUnit.MINUTES)
    @Override
    public AmsArticleVo getAmsArticleVo(Long articleId) {

        if(isArticleNotExists(articleId)){
            log.info("文章不存在，articleId={}",articleId);
            throw new CustomBaseException("文章不存在");
        }


//
//        // 從網關傳遞的請求標頭中取得用戶資訊
//        if (!UserContextHolder.isCurrentUserLoggedIn()) {
//            log.warn("未取得登入用戶資訊，拒絕查看文章");
//            throw new IllegalStateException("未登入或登入狀態已失效");
//        }
//
//        Long userId = UserContextHolder.getCurrentUserId();
//
//        if (userId == null) {
//            log.warn("用戶ID為空，拒絕查看文章");
//            throw new IllegalStateException("用戶ID缺失");
//        }
//
//        redissonClient.getAtomicLong("AmsArticle:ArticleVo:"+articleId).incrementAndGet();



        AmsArticleVo amsArticleVo = this.baseMapper.getArticleVo(articleId);
        if(amsArticleVo== null){
            throw new CustomBaseException("無法成功獲取文章資訊，該文章可能已被刪除");
        }






//        R<UserBasicDTO> user = userFeignClient.getUserById(amsArticleVo.getUserId());
//        if(user == null || user.getData() == null){
//            log.warn("未能通過用戶ID獲取用戶資訊，UserId={}", amsArticleVo.getUserId());
//            throw new CustomBaseException("未能獲取用戶資訊");
//        }
//        amsArticleVo.setUserName(user.getData().getNickName());




//        AmsArticle amsArticle = this.baseMapper.selectById(articleId);
//        AmsArtinfo amsArtinfo = amsArtinfoService.getOne(new LambdaQueryWrapper<AmsArtinfo>().eq(AmsArtinfo::getArticleId, articleId));
//        AmsCategory amsCategory = amsCategoryService.getById(amsArtinfo.getCategoryId());
//        AmsArtStatus amsArtStatus = amsArtStatusService.getOne(new LambdaQueryWrapper<AmsArtStatus>().eq(AmsArtStatus::getArticleId, amsArticle.getId()));
//        long commentsCount = amsCommentInfoService.count(new LambdaQueryWrapper<AmsCommentInfo>().eq(AmsCommentInfo::getArticleId, amsArticle.getId()));
//        List<AmsArtTag> amsArtTagList = amsArtTagService.list(new LambdaQueryWrapper<AmsArtTag>().eq(AmsArtTag::getArticleId, amsArticle.getId()));
//        /*
//        設置文章標籤
//         */
//            /*
//            搜集文章標籤對象的所有標籤ID
//             */
//            List<Long> amsArtTagsIdList = amsArtTagList.stream().map(AmsArtTag::getTagsId).toList();
//
//            /*
//            透過標籤ID列表獲取標籤對象列表
//             */
//            List<AmsTags> amsTagsList = amsTagsService.listByIds(amsArtTagsIdList);
//
//
//
//
//        AmsArticleVo amsArticleVo = new AmsArticleVo();
//        /*
//        設置文章內容、文章標題、文章ID
//         */
//        BeanUtils.copyProperties(amsArticle, amsArticleVo);
//        /*
//        設置文章資訊
//         */
//        ///  TODO缺少userName
//        BeanUtils.copyProperties(amsArtinfo, amsArticleVo,"id","articleId");
//        /*
//        設置分類資訊
//         */
//        amsArticleVo.setCategoryId(amsCategory.getId());
//        amsArticleVo.setCategoryName(amsCategory.getCategoryName());
//        /*
//        設置文章狀態
//         */
//        BeanUtils.copyProperties(amsArtStatus, amsArticleVo,"id","articleId");
//        /*
//        設置評論數量
//         */
//        amsArticleVo.setCommentsCount((int) commentsCount);
//
//
//
//        /*
//        包裝標籤對象列表到文章VO中
//         */
//        List<AmsArticleTagsVo> amsArticleTagsVo = amsTagsList.stream().map(amsTags -> {
//            AmsArticleTagsVo tagsVo = new AmsArticleTagsVo();
//            BeanUtils.copyProperties(amsTags, tagsVo);
//            return tagsVo;
//        }).toList();
//
//        amsArticleVo.setAmsArticleTagsVoList(amsArticleTagsVo);
        return amsArticleVo;
    }
//
//
//
////    @Override
////    public Page<AmsArticle> getLatestArticles() {
////
////
////    }

    public Long incrementArticleViewsCount(Long articleId) {

        if(isArticleNotExists(articleId)){
            log.info("文章不存在，articleId={}",articleId);
            throw new CustomBaseException("文章不存在");
        }


//        /*
//          檢查用戶是否登入
//        */
//
//        if (!UserContextHolder.isCurrentUserLoggedIn()) {
//            log.warn("未取得登入用戶資訊，拒絕對文章按讚");
//            throw new CustomBaseException("未登入或登入狀態已失效");
//        }
//        Long userId = UserContextHolder.getCurrentUserId();
//        String userNameFromToken = UserContextHolder.getCurrentUserNickname();
//        if (userId == null) {
//            log.warn("用戶ID為空，拒絕對文章按讚");
//            throw new CustomBaseException("用戶ID缺失");
//        }

        /// TODO(可選)根據IP、UA、Cookie等資訊限制同一用戶在短時間內多次刷新導致瀏覽數異常增長

        if(articleId==null || articleId <=0){
            throw new CustomBaseException("文章ID不能為空或小於等於0");
        }



        /**
         * 驗證該文章是否存在
         */


        /**
         * 同步點讚數從Redis到資料庫中
         */
        //例如：ams:article:views:1965494783750287361
        final String redisKey = RedisCacheKey.ARTICLE_VIEWS.format(articleId);
        //獲取桶對象, 注意原子性因此採用RAtomicLong


        /// TODO排程定時將Redis中的瀏覽數同步到資料庫中

        RAtomicLong viewCounter = redissonClient.getAtomicLong(redisKey);

        //設置該文章查看人數值為++ , 注意原子性
        long newViews = viewCounter.incrementAndGet();


        if (newViews == 1) {
            viewCounter.clearExpire(); // 移除任何可能存在的 TTL
            log.info("文章 {} 產生第一次瀏覽，已確保 Key 為持久化", articleId);
        }

        log.debug("newLikes:{}",newViews);
        return newViews;
    }


    @Override
    public Long incrementArticleLikes(Long articleId) {


        if(isArticleNotExists(articleId)){
            log.info("文章不存在，articleId={}",articleId);
            throw new CustomBaseException("文章不存在");
        }

        /*
          檢查用戶是否登入
        */

        if (!UserContextHolder.isCurrentUserLoggedIn()) {
            log.warn("未取得登入用戶資訊，拒絕對文章按讚");
            throw new CustomBaseException("未登入或登入狀態已失效");
        }
        Long userId = UserContextHolder.getCurrentUserId();
        /**
         * 點讚限流
         */
//        String userLikeRateLimit = RedisRateLimitKey.RATE_LIMIT_USER_LIKE.format(userId);
//        RateType rateType = RedisRateLimitKey.RATE_LIMIT_USER_LIKE.getRateType();
//        Long rate = RedisRateLimitKey.RATE_LIMIT_USER_LIKE.getRate();
//        RateIntervalUnit rateIntervalUnit = RedisRateLimitKey.RATE_LIMIT_USER_LIKE.getRateIntervalUnit();
//        Long rateInterval = RedisRateLimitKey.RATE_LIMIT_USER_LIKE.getRateInterval();
//
//        redisRateLimiterUtils.setRedisRateLimiter(userLikeRateLimit,rateType,rate,rateInterval,rateIntervalUnit);


        /**
         * 透過布隆
         */



        /// TODO(可選)根據IP、UA、Cookie等資訊限制同一用戶在短時間內多次刷新導致瀏覽數異常增長


        /**
         * 檢查該用戶是否已經點讚過該文章，若未點過則修改紀錄成已經點過，防止重複點讚
         */

        final String userLikeKey = RedisCacheKey.ARTICLE_LIKED_USERS.format(articleId);
        final String likesCountKey = RedisCacheKey.ARTICLE_LIKES.format(articleId);

        // Lua 腳本保證原子性
        String luaScript =
                "local added = redis.call('SADD', KEYS[1], ARGV[1]) \n" +
                        "if added == 1 then \n" +
                        "    local count = redis.call('INCR', KEYS[2]) \n" +
                        "    return count \n" +
                        "else \n" +
                        "    return -1 \n" +
                        "end";

        RScript script = redissonClient.getScript(StringCodec.INSTANCE);
        Long result = script.eval(
                RScript.Mode.READ_WRITE,
                luaScript,
                RScript.ReturnType.INTEGER,
                Arrays.asList(userLikeKey, likesCountKey),
                userId.toString()
        );

        if (result == -1) {
            throw new CustomBaseException("您已經點讚過該文章");
        }




//        final String userLikeKey = RedisCacheKey.ARTICLE_LIKED_USERS.format(articleId);
//
//        RSet<Long> set = redissonClient.getSet(userLikeKey);
//        boolean add = set.add(userId);
//        if(!add){
//            log.warn("用戶 {} 已經點讚過該文章 {}",userId,articleId);
//            throw new CustomBaseException("您已經點讚過該文章，請勿重複點讚");
//        }
//        /**
//         * 同步點讚數從Redis到資料庫中
//         */
//        //例如：ams:article:likes:1965494783750287361
//        final String redisKey = RedisCacheKey.ARTICLE_LIKES.format(articleId);
//        //獲取桶對象, 注意原子性因此採用RAtomicLong
//
//
//
//        /// TODO排程定時將Redis中的瀏覽數同步到資料庫中
//
//        RAtomicLong likesCounter = redissonClient.getAtomicLong(redisKey);
//
//        //設置該文章點讚人數值為++ , 注意原子性
//        long newLikes = likesCounter.incrementAndGet();
//
//        if (newLikes == 1) {
//            likesCounter.clearExpire(); // 移除任何可能存在的 TTL
//            log.info("文章 {} 產生第一次瀏覽，已確保 Key 為持久化", articleId);
//        }
//        /**
//         * 紀錄該用戶已成功點讚該文章
//         */
//
//
//
//
//        log.debug("newLikes:{}",newLikes);


        return result;
    }

    @Override
    public Long incrementArticleBooksMarket(Long articleId) {


        if(isArticleNotExists(articleId)){
            log.info("文章不存在，articleId={}",articleId);
            throw new CustomBaseException("文章不存在");
        }


        /*
          檢查用戶是否登入
        */

        if (!UserContextHolder.isCurrentUserLoggedIn()) {
            log.warn("未取得登入用戶資訊，拒絕對文章加入書籤");
            throw new CustomBaseException("未登入或登入狀態已失效");
        }
        Long userId = UserContextHolder.getCurrentUserId();
        /**
         * 點讚限流
         */
//        String userLikeRateLimit = RedisRateLimitKey.RATE_LIMIT_USER_LIKE.format(userId);
//        RateType rateType = RedisRateLimitKey.RATE_LIMIT_USER_LIKE.getRateType();
//        Long rate = RedisRateLimitKey.RATE_LIMIT_USER_LIKE.getRate();
//        RateIntervalUnit rateIntervalUnit = RedisRateLimitKey.RATE_LIMIT_USER_LIKE.getRateIntervalUnit();
//        Long rateInterval = RedisRateLimitKey.RATE_LIMIT_USER_LIKE.getRateInterval();
//
//        redisRateLimiterUtils.setRedisRateLimiter(userLikeRateLimit,rateType,rate,rateInterval,rateIntervalUnit);



        /// TODO(可選)根據IP、UA、Cookie等資訊限制同一用戶在短時間內多次刷新導致瀏覽數異常增長


        /**
         * 檢查該用戶是否已經點讚過該文章，若未點過則修改紀錄成已經點過，防止重複點讚
         */

        final String userBookMarksKey = RedisCacheKey.ARTICLE_MARKED_USERS.format(articleId);
        final String likesCountKey = RedisCacheKey.ARTICLE_BOOKMARKS.format(articleId);

        // Lua 腳本保證原子性
        String luaScript =
                "local added = redis.call('SADD', KEYS[1], ARGV[1]) \n" +
                        "if added == 1 then \n" +
                        "    local count = redis.call('INCR', KEYS[2]) \n" +
                        "    return count \n" +
                        "else \n" +
                        "    return -1 \n" +
                        "end";

        RScript script = redissonClient.getScript(StringCodec.INSTANCE);
        Long result = script.eval(
                RScript.Mode.READ_WRITE,
                luaScript,
                RScript.ReturnType.INTEGER,
                Arrays.asList(userBookMarksKey, likesCountKey),
                userId.toString()
        );

        if (result == -1) {
            throw new CustomBaseException("您已經點讚過該文章");
        }


        return result;
    }







//    @Override
//    public Long incrementBookmarks(long articleId) {
//        //        /*
////          檢查用戶是否登入
////        */
////
////        if (!UserContextHolder.isCurrentUserLoggedIn()) {
////            log.warn("未取得登入用戶資訊，拒絕對文章按讚");
////            throw new CustomBaseException("未登入或登入狀態已失效");
////        }
////        Long userId = UserContextHolder.getCurrentUserId();
////        String userNameFromToken = UserContextHolder.getCurrentUserNickname();
////        if (userId == null) {
////            log.warn("用戶ID為空，拒絕對文章按讚");
////            throw new CustomBaseException("用戶ID缺失");
////        }
//
//        /// TODO(可選)根據IP、UA、Cookie等資訊限制同一用戶在短時間內多次刷新導致瀏覽數異常增長
//
//
//        /**
//         * 同步點讚數從Redis到資料庫中
//         */
//        //例如：ams:article:likes:1965494783750287361
//        final String redisKey = RedisCacheKey.ARTICLE_LIKES.format(articleId);
//        //獲取桶對象, 注意原子性因此採用RAtomicLong
//
//
//        /// TODO排程定時將Redis中的瀏覽數同步到資料庫中
//
//        RAtomicLong likesCounter = redissonClient.getAtomicLong(redisKey);
//
//        //設置該文章點讚人數值為++ , 注意原子性
//        long newLikes = likesCounter.incrementAndGet();
//
//        if (newLikes == 1) {
//            likesCounter.clearExpire(); // 移除任何可能存在的 TTL
//            log.info("文章 {} 產生第一次瀏覽，已確保 Key 為持久化", articleId);
//        }
//
//        log.debug("newLikes:{}",newLikes);
//
//
//
//
//
//        // 同時更新資料庫（省略...）
//        return newLikes;
//    }

    public Long getArticleLikes(long articleId) {
        final String redisKey = RedisCacheKey.ARTICLE_LIKES.format(articleId);
        RAtomicLong likesCounter = redissonClient.getAtomicLong(redisKey);

        // 先嘗試從 Redis 讀
        try {
            if (likesCounter.isExists()) {
                long likes = likesCounter.get();
                log.debug("likes:{}", likes);
                return likes;
            }
        } catch (RedisConnectionException | RedisTimeoutException e) {
            // 連線/逾時 -> 降級讀庫（此時通常無法回填）
            log.warn("Redis不可用，降級讀DB，articleId={}", articleId, e);
            return readLikesFromDb(articleId);
        } catch (Exception e) {
            // 非預期錯誤 -> 告警
            log.error("獲取文章點讚數發生非預期錯誤，articleId={}", articleId, e);
            return 0L;
        }

        // 冷啟動：Redis鍵不存在 -> 讀DB並回填（避免穿透）
        Long likesFromDb = readLikesFromDb(articleId);

        // 回填：用 CAS 避免覆蓋併發新值
        try {
            boolean casOk = likesCounter.compareAndSet(0L, likesFromDb);
            if (!casOk) {
                // 可能已有併發寫入，取最新值返回
                long current = likesCounter.get();
                log.debug("回填CAS失敗，返回當前Redis值={}，articleId={}", current, articleId);
                return current;
            }
            // 對 0 值設短TTL作為負快取，降低穿透；正值可不設或設長TTL
            if (likesFromDb == 0L) {
                likesCounter.expire(5, java.util.concurrent.TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            // 回填失敗不影響主流程
            log.warn("Redis回填失敗，articleId={}", articleId, e);
        }

        return likesFromDb;
    }


    public Long getArticleBookmarks(long articleId) {
        final String redisKey = RedisCacheKey.ARTICLE_BOOKMARKS.format(articleId);
        RAtomicLong bookmarksCounter = redissonClient.getAtomicLong(redisKey);

        // 先嘗試從 Redis 讀
        try {
            if (bookmarksCounter.isExists()) {
                long bookmarks = bookmarksCounter.get();
                log.debug("bookmarks:{}", bookmarks);
                return bookmarks;
            }
        } catch (RedisConnectionException | RedisTimeoutException e) {
            // 連線/逾時 -> 降級讀庫（此時通常無法回填）
            log.warn("Redis不可用，降級讀DB，articleId={}", articleId, e);
            return readbookmarksFromDb(articleId);
        } catch (Exception e) {
            // 非預期錯誤 -> 告警
            log.error("獲取文章點讚數發生非預期錯誤，articleId={}", articleId, e);
            return 0L;
        }

        // 冷啟動：Redis鍵不存在 -> 讀DB並回填（避免穿透）
        Long bookmarksFromDb = readbookmarksFromDb(articleId);

        // 回填：用 CAS 避免覆蓋併發新值
        try {
            boolean casOk = bookmarksCounter.compareAndSet(0L, bookmarksFromDb);
            if (!casOk) {
                // 可能已有併發寫入，取最新值返回
                long current = bookmarksCounter.get();
                log.debug("回填CAS失敗，返回當前Redis值={}，articleId={}", current, articleId);
                return current;
            }
            // 對 0 值設短TTL作為負快取，降低穿透；正值可不設或設長TTL
            if (bookmarksFromDb == 0L) {
                bookmarksCounter.expire(5, java.util.concurrent.TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            // 回填失敗不影響主流程
            log.warn("Redis回填失敗，articleId={}", articleId, e);
        }

        return bookmarksFromDb;
    }

    private Long readbookmarksFromDb(long articleId) {
        AmsArtStatus amsArtStatus = amsArtStatusService.getOne(
                new LambdaQueryWrapper<AmsArtStatus>()
                        .select(AmsArtStatus::getBookmarksCount)
                        .eq(AmsArtStatus::getArticleId, articleId)
                ,false
        );
        return Optional.ofNullable(amsArtStatus)
                .map(AmsArtStatus::getBookmarksCount)
                .map(Integer::longValue)
                .orElse(0L);
    }

    private Long readLikesFromDb(long articleId) {
        AmsArtStatus amsArtStatus = amsArtStatusService.getOne(
                new LambdaQueryWrapper<AmsArtStatus>()
                        .select(AmsArtStatus::getLikesCount)
                        .eq(AmsArtStatus::getArticleId, articleId)
                        ,false
        );
        return Optional.ofNullable(amsArtStatus)
                .map(AmsArtStatus::getLikesCount)
                .map(Integer::longValue)
                .orElse(0L);
    }






    /**
     * 判斷文章是否不存在，透過布隆過濾器以及資料庫雙重確認，非分佈式鎖版本
     * @param articleId
     * @return
     */
    public boolean isArticleNotExists(Long articleId) {

        if (articleId == null || articleId <= 0) {
            log.warn("非法文章ID: {}", articleId);
            return true;
        }

        /**
         * 透過布隆過濾器初步判斷該文章是否存在
         */

        try {
            /// TODO增加 Bloom 就緒旗標，透過旗標判斷是否需要檢查布隆過濾器，避免Redis服務異常導致無法使用布隆過濾器
            String articleBloomFilterPattern = RedisBloomFilterKey.ARTICLE_BLOOM_FILTER.getKey();
            RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(articleBloomFilterPattern);

            if(!bloomFilter.contains(articleId)){
                //若果布隆過濾器中不存在該文章ID，表示該文章一定不存在
                log.info("該文章ID:{}不存在或已被刪除", articleId);
                return true;
            }
        } catch (RedisConnectionException e) {
            log.debug("Redis 連線異常，跳過布隆過濾器檢查，articleId={}", articleId, e);
            return isArticleNotExistsFromDB(articleId);

        }


        return false;

    }

    private boolean isArticleNotExistsFromDB(Long articleId) {
        if (articleId == null || articleId <= 0) {
            log.warn("非法文章ID: {}", articleId);
            return true;
        }

        AmsArticle amsArticle = this.baseMapper.selectById(articleId);
        if(amsArticle == null){
            //資料庫中不存在該文章
            log.info("該文章ID:{}不存在或已被刪除", articleId);
            return true;
        }

        Long amsArticleId = amsArticle.getId();


        if(amsArticleId==null){
            //表示該文章一定不存在
            log.info("該文章ID:{}不存在或已被刪除", articleId);
            return true;
        }
        return false;
    }


//    /**
//     * 判斷文章是否不存在，透過布隆過濾器以及資料庫雙重確認，使用分佈式鎖版本
//     * @param articleId
//     * @return
//     */
//    public boolean isArticleDefinitelyNotExists(Long articleId) {
//
//        if (articleId == null || articleId <= 0) {
//            log.warn("文章ID不能小於等於0或為空");
//            throw new CustomBaseException("文章ID不能小於等於0或為空");
//        }
//
//        /**
//         * 透過布隆過濾器初步判斷該文章是否存在
//         */
//        String articleBloomFilterPattern = RedisBloomFilterKey.ARTICLE_BLOOM_FILTER.getKey();
//        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(articleBloomFilterPattern);
//        boolean contains = bloomFilter.contains(articleId);
//        //判斷布隆過濾器中是否存在該文章ID
//        if (!contains) {
//            //不存在於布隆過濾器中，表示該文章不存在
//            log.warn("該文章ID:{}不存在於布隆過濾器中", articleId);
//            return true;
//        }
//
//        //存在於布隆過濾器中，表示該文章存在
////        return false;
//
//        RLock lock = redissonClient.getLock(RedisLockKey.ARTICLE_EXISTS_LOCK.getFormat(articleId));
//        try {
//            boolean tryLock = lock.tryLock(10,0,TimeUnit.SECONDS);
//
//            if(!tryLock){
//                long waitTime = System.nanoTime() + TimeUnit.SECONDS.toNanos(60);
//                while(System.nanoTime() < waitTime){
//
//                    contains = bloomFilter.contains(articleId);
//                    if(!contains){
//                        //不存在於布隆過濾器中，表示該文章不存在
//                        log.warn("該文章ID:{}不存在於布隆過濾器中", articleId);
//                        return true;
//                    }
//
//                    TimeUnit.MILLISECONDS.sleep(300);
//                }
//                throw new CustomBaseException("系統繁忙，請稍後再試");
//            }
//            //        Long artcileId =this.baseMapper.selectById((new LambdaQueryWrapper<AmsArticle>()
////                .select(AmsArticle::getId)
////                .eq(AmsArticle::getId, articleId)));
//
//            //查詢資料庫前再次確認布隆過濾器中是否不存在該文章ID，防止併發情況下重複查詢資料庫
//            contains = bloomFilter.contains(articleId);
//            if(!contains){
//                //不存在於布隆過濾器中，表示該文章不存在
//                log.warn("該文章ID:{}不存在於布隆過濾器中", articleId);
//                return true;
//            }
//
//            AmsArticle amsArticle = this.baseMapper.selectById(articleId);
//            if(amsArticle == null){
//                //資料庫中不存在該文章
//                log.warn("該文章ID:{}不存在於資料庫中",articleId);
//                return true;
//            }
//
//            Long amsArticleId = amsArticle.getId();
//
//
//            if(amsArticleId==null){
//                log.warn("該文章ID:{}不存在或已被刪除",articleId);
//                return true;
//            }
////                bloomFilter.add(articleId);
//            return false;
//
//        }
//        catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            log.warn("文章存在性檢查被中斷，articleId={}", articleId);
//            throw new CustomBaseException("系統繁忙，請稍後再試");
//        }
//        finally {
//            if(lock.isHeldByCurrentThread()){
//                lock.unlock();
//            }
//        }
//
//
//    }


    /**
     * 判斷文章是否存在，透過布隆過濾器以及資料庫雙重確認
     */
//    public boolean isExistsArticle(Long articleId){
//
//        if(articleId == null || articleId <= 0){
//            log.warn("文章ID不能小於等於0或為空");
//            throw new CustomBaseException("文章ID不能小於等於0或為空");
//        }
//
//        /**
//         * 透過布隆過濾器初步判斷該文章是否存在
//         */
//        String articleBloomFilterPattern = RedisBloomFilterKey.ARTICLE_BLOOM_FILTER.getKey();
//        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(articleBloomFilterPattern);
//        boolean contains = bloomFilter.contains(articleId);
//        if(contains){
//            //存在於布隆過濾器中，表示該文章存在
//            return true;
//        }
//        log.warn("該文章ID:{}不存在於布隆過濾器中，進一步從資料庫中確認",articleId);
//        //不存在於布隆過濾器中，表示該文章不存在或已被刪除，進一步從資料庫中確認
//
//        RLock lock = redissonClient.getLock(RedisLockKey.ARTICLE_EXISTS_LOCK.getFormat(articleId));
//        try {
//        boolean tryLock = lock.tryLock(10,0,TimeUnit.SECONDS);
//
//            if(!tryLock){
//                long waitTime = System.nanoTime() + TimeUnit.SECONDS.toNanos(60);
//                while(System.nanoTime() < waitTime){
//
//                    contains = bloomFilter.contains(articleId);
//                    if(contains){
//                        log.info("文章ID:{}在等待期間被其他線程確認存在於資料庫中",articleId);
//                        return true;
//                    }
//
//                    TimeUnit.MILLISECONDS.sleep(300);
//                }
//                throw new CustomBaseException("系統繁忙，請稍後再試");
//            }
//                //        Long artcileId =this.baseMapper.selectById((new LambdaQueryWrapper<AmsArticle>()
////                .select(AmsArticle::getId)
////                .eq(AmsArticle::getId, articleId)));
//
//                //查詢資料庫前再次確認布隆過濾器中是否存在該文章ID，防止併發情況下重複查詢資料庫
//                contains = bloomFilter.contains(articleId);
//                if(contains){
//                    //存在於布隆過濾器中，表示該文章存在
//                    return true;
//                }
//
//                AmsArticle amsArticle = this.baseMapper.selectById(articleId);
//                if(amsArticle == null){
//                    //資料庫中不存在該文章
//                    log.warn("該文章ID:{}不存在於資料庫中",articleId);
//                    return false;
//                }
//
//                Long amsArticleId = amsArticle.getId();
//
//
//                if(amsArticleId==null){
//                    log.warn("該文章ID:{}不存在或已被刪除",articleId);
//                    return false;
//                }
//                bloomFilter.add(articleId);
//                return true;
//
//
//
//
//
//        }
//        catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            log.warn("文章存在性檢查被中斷，articleId={}", articleId);
//            throw new CustomBaseException("系統繁忙，請稍後再試");
//        }
//        finally {
//            if(lock.isHeldByCurrentThread()){
//                lock.unlock();
//            }
//        }
//
//
//    }

//    public Long incrementArticleLikesCount(Long articleId) {
//
//        /*
//          檢查用戶是否登入
//        */
//
//        if (!UserContextHolder.isCurrentUserLoggedIn()) {
//            log.warn("未取得登入用戶資訊，拒絕對文章按讚");
//            throw new CustomBaseException("未登入或登入狀態已失效");
//        }
//        Long userId = UserContextHolder.getCurrentUserId();
//        String userNameFromToken = UserContextHolder.getCurrentUserNickname();
//        if (userId == null) {
//            log.warn("用戶ID為空，拒絕對文章按讚");
//            throw new CustomBaseException("用戶ID缺失");
//        }
//
//        /**
//         * 同步點讚數從Redis到資料庫中
//         */
////        String BucketName = String.format("AmsArticles:ArticleId_%d:ViewsCount", articleId);
//
//        String BucketName = RedisCacheKey.ARTICLE_VIEWS.format(articleId);
//            /*
//              判斷該文章是否存在
//            */
//        //獲取桶對象, 名稱為 "AmsComments:CommentId_{commentId}:LikesCount" , 注意原子性因此採用RAtomicLong
//        RAtomicLong atomicLong = redissonClient.getAtomicLong(BucketName);
//        //先嘗試從Redis中讀取是否存在該文章的緩存
//        boolean atomicLongExists = atomicLong.isExists();
//
//
//
//        if(!atomicLongExists){
//            //若不存在則根據commentId從資料庫中讀取該文章是否存在
//            if(!this.isExistsArticle(articleId)){
//                //假設從Reids緩存以及從資料庫中讀取該文章皆表明該文章不存在
//                throw new CustomBaseException("該文章不存在或已被刪除");
//            }
//
//
//        }
//
//
//            /*
//            判斷用戶是否已對該文章按讚,若已按讚則不能重複讚按
//             */
////        String userLikeKey = String.format("AmsComments:CommentId_%d:UserLikes", articleId);
//
//        String userLikeKey = RedisCacheKey.ARTICLE_USER_LIKED.format(articleId, userId);
//
//        RSet<Long> userLikeSet = redissonClient.getSet(userLikeKey);
//
//
//
//        //成功點讚紀錄用戶已點讚
//        boolean add = userLikeSet.add(userId);
//        if(!add){
//            //若用戶已存在於點讚集合中,表示用戶已點讚過
//            log.warn("用戶ID:{}，用戶名稱:{}，重複對文章ID:{}按讚",userId,userNameFromToken,articleId);
//            throw new CustomBaseException("您已對該文章按讚過，請勿重複按讚");
//        }
//        //設置點讚值為++ , 注意原子性
//        long newLikes = atomicLong.incrementAndGet();
//        log.debug("newLikes:{}",newLikes);
//        return newLikes;
//    }

}
