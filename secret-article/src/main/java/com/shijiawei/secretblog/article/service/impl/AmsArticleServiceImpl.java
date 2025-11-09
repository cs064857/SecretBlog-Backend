package com.shijiawei.secretblog.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shijiawei.secretblog.article.annotation.DelayDoubleDelete;
import com.shijiawei.secretblog.article.dto.AmsArticleUpdateDTO;
import com.shijiawei.secretblog.article.entity.*;
import com.shijiawei.secretblog.article.feign.UserFeignClient;


import com.shijiawei.secretblog.article.service.*;
import com.shijiawei.secretblog.article.utils.CommonmarkUtils;
import com.shijiawei.secretblog.article.vo.AmsArticleStatusVo;
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
import com.shijiawei.secretblog.common.myenum.RedisOpenCacheKey;
import com.shijiawei.secretblog.common.utils.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;
import org.redisson.client.RedisConnectionException;
import org.redisson.client.RedisTimeoutException;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;


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

    @Autowired
    private RedisBloomFilterUtils redisBloomFilterUtils;

    @Autowired
    private RedisCacheLoaderUtils redisCacheLoaderUtils;

    private final RedisRateLimiterUtils redisRateLimiterUtils;

    @Autowired
    @Lazy
    private AmsArticleService amsArticleService;


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

    @Transactional(rollbackFor = Exception.class)
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
            //設置文章標題
            amsArticle.setTitle(amsSaveArticleVo.getTitle());

            /// TODO保存原始文章內容用於安全方面, 避免從原始文章內容從Markdown格式轉換HTML時遺漏某些字等(編輯文章時要更新兩者、讀取時只讀取轉換後的文章)

            // 將原始文章內容從Markdown轉為HTML格式
            String html = CommonmarkUtils.parseMdToHTML(amsSaveArticleVo.getContent());

            log.info("html:{}",html);
            //將Markdown格式的文章保存至資料庫中
            amsArticle.setContent(html);

            this.baseMapper.insert(amsArticle);

            amsArtinfo.setCategoryId(amsSaveArticleVo.getCategoryId());
            R<UserBasicDTO> user = userFeignClient.getUserById(userId);
            if(user.getData()!=null){
//                amsArtinfo.setAccountName(user.getData().getAccountName());
                amsArtinfo.setNickName(user.getData().getNickName());
                amsArtinfo.setAvatar(user.getData().getAvatar());
            }


            //▼
//            amsArtinfo.setNickName(userNameFromToken);
//            amsArtinfo.setAvatar(userNameFromToken);



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
        // 使用封裝好的方法，在事務提交後添加到布隆過濾器
        redisBloomFilterUtils.saveToBloomFilterAfterCommit(
                amsArticle.getId(),
                RedisBloomFilterKey.ARTICLE_BLOOM_FILTER.getKey()
        );


//        // 使用 final 變量以便在後續使用
//        final AmsArticle savedArticle = amsArticle;
//
//        // 註冊事務同步回調，在事務提交後執行
//        TransactionSynchronizationManager.registerSynchronization(
//                new TransactionSynchronization() {
//                    @Override
//                    public void afterCommit() {
//                        try {
//                            redisBloomFilterUtils.saveArticleIdToBloomFilter(savedArticle.getId(),RedisBloomFilterKey.ARTICLE_BLOOM_FILTER.getKey());
//                        } catch (Exception e) {
//                            //布隆過濾器出現異常,但不拋出異常，避免影響主流程
//                            log.error("布隆過濾器添加失敗，文章ID: {}", savedArticle.getId(), e);
//                        }
//                    }
//                }
//        );
    }

//    private void saveArticleIdToBloomFilter(AmsArticle amsArticle) {
//        //將新的文章ID新增至布隆過濾器中
//        //不管是否成功加入布隆過濾器都不影響文章發佈, 不進行回滾
//        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(RedisBloomFilterKey.ARTICLE_BLOOM_FILTER.getKey());
//        bloomFilter.add(amsArticle.getId());
//    }


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
            log.warn("文章不存在，articleId={}",articleId);
            throw new CustomBaseException("文章不存在");
        }

        AmsArticleVo amsArticleVo = amsArticleService.getAmsArticleVo(articleId);
//        AmsArticleVo amsArticleVo = getAmsArticleVo(articleId);
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
//            amsArticleVo.setViewsCount(Math.toIntExact(views));
        } catch (RedisConnectionException | RedisTimeoutException e) {
            log.warn("Redis 計數失敗，改用 DB 回填 viewsCount，articleId={}, err={}", amsArticleVoId, e.getMessage());
        // 僅查所需欄位 + 保證唯一
//            AmsArtStatus status = amsArtStatusService.getOne(
//                    Wrappers.<AmsArtStatus>lambdaQuery()
//                            .select(AmsArtStatus::getViewsCount)
//                            .eq(AmsArtStatus::getArticleId, amsArticleVoId),
//                    false // 若多筆不丟例外
//            );
//            int fallbackViews = status != null && status.getViewsCount() != null ? status.getViewsCount() : 0;
//            amsArticleVo.setViewsCount(fallbackViews);
        } catch (Exception e) {
        // 其他未知例外：不要默默吞，應該明確告警
            log.error("計數發生非預期錯誤，articleId={}", amsArticleVoId, e);
        // 可選：最後一道防線，顯示 DB 值或 0
//            amsArticleVo.setViewsCount(0);
        }

        AmsArticleStatusVo articleStatus = getArticleStatusVo(articleId);
        BeanUtils.copyProperties(articleStatus, amsArticleVo);

        ///TODO注意安全性




//        Long articleLikes = getArticleLikes(articleId);
//        Long articleBookmarks = getArticleBookmarks(articleId);
//        amsArticleVo.setLikesCount(articleLikes.intValue());
//        amsArticleVo.setBookmarksCount(articleBookmarks.intValue());


        return amsArticleVo;
    }

    @OpenCache(prefix = RedisOpenCacheKey.ArticleDetails.ARTICLE_DETAILS_PREFIX,key = RedisOpenCacheKey.ArticleDetails.ARTICLE_DETAILS_KEY,time = 30,chronoUnit = ChronoUnit.MINUTES)
    @Override
    public AmsArticleVo getAmsArticleVo(Long articleId) {
        log.info("獲取文章詳情，articleId={}",articleId);

        if(isArticleNotExists(articleId)){
            log.warn("文章不存在，articleId={}",articleId);
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

        // 優先通過用戶服務獲取最新用戶暱稱；失敗時保留DB中的備援 userName
        /// TODO透過Openfeign獲取用戶資料
//        try {
//            Long uid = amsArticleVo.getUserId();
//            if (uid != null) {
//                amsArticleVo.setUserId(uid);
//                R<UserBasicDTO> userResp = userFeignClient.getUserById(uid);
//                if (userResp != null && userResp.getData() != null) {
//                    String nickName = userResp.getData().getNickName();
//                    String avatar = userResp.getData().getAvatar();
//
//                    if (nickName != null ) {
//                        //▼ avatar!=null && nickName != null
//                        amsArticleVo.setNickName(nickName);
//
//                    }
//                    if(avatar !=null){
//                        amsArticleVo.setAvatar(avatar);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            // 遠端失敗不影響主流程，採用本地 userName 作為退化顯示
//            log.warn("透過用戶服務獲取暱稱失敗，articleId={}, userId={}, err={}", articleId, amsArticleVo.getUserId(), e.getMessage());
//        }







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
//        ///  TODO缺少accountName
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
//        List<AmsArtTagsVo> amsArtTagsVo = amsTagsList.stream().map(amsTags -> {
//            AmsArtTagsVo tagsVo = new AmsArtTagsVo();
//            BeanUtils.copyProperties(amsTags, tagsVo);
//            return tagsVo;
//        }).toList();
//
//        amsArticleVo.setAmsArtTagsVoList(amsArtTagsVo);
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
            log.warn("文章不存在，articleId={}",articleId);
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
            log.warn("文章不存在，articleId={}",articleId);
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
            log.warn("文章不存在，articleId={}",articleId);
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
////        // 同時更新資料庫（省略...）
////        return newLikes;
////    }

//    public Long getArticleLikes(long articleId) {
//        final String redisKey = RedisCacheKey.ARTICLE_LIKES.format(articleId);
//        RAtomicLong likesCounter = redissonClient.getAtomicLong(redisKey);
//
//        // 先嘗試從 Redis 讀
//        try {
//            if (likesCounter.isExists()) {
//                long likes = likesCounter.get();
//                log.debug("likes:{}", likes);
//                return likes;
//            }
//        } catch (RedisConnectionException | RedisTimeoutException e) {
//            // 連線/逾時 -> 降級讀庫（此時通常無法回填）
//            log.warn("Redis不可用，降級讀DB，articleId={}", articleId, e);
//            return readLikesFromDb(articleId);
//        } catch (Exception e) {
//            // 非預期錯誤 -> 告警
//            log.error("獲取文章點讚數發生非預期錯誤，articleId={}", articleId, e);
//            return 0L;
//        }
//
//        // 冷啟動：Redis鍵不存在 -> 讀DB並回填（避免穿透）
//        Long likesFromDb = readLikesFromDb(articleId);
//
//        // 回填：用 CAS 避免覆蓋併發新值
//        try {
//            boolean casOk = likesCounter.compareAndSet(0L, likesFromDb);
//            if (!casOk) {
//                // 可能已有併發寫入，取最新值返回
//                long current = likesCounter.get();
//                log.debug("回填CAS失敗，返回當前Redis值={}，articleId={}", current, articleId);
//                return current;
//            }
//            // 對 0 值設短TTL作為負快取，降低穿透；正值可不設或設長TTL
//            if (likesFromDb == 0L) {
//                likesCounter.expire(5, java.util.concurrent.TimeUnit.MINUTES);
//            }
//        } catch (Exception e) {
//            // 回填失敗不影響主流程
//            log.warn("Redis回填失敗，articleId={}", articleId, e);
//        }
//
//        return likesFromDb;
//    }

    private AmsArticleStatusVo loadArticleStatusVo(long articleId) {
        log.info("開始執行 loadArticleStatus - articleId: {}", articleId);
        AmsArtStatus articleStatusFromDB = QueryArticleStatus(articleId);

        log.info("articleStatusFromDB:{}", articleStatusFromDB);

        if(articleStatusFromDB ==null){
            log.warn("文章狀態資訊不存在，不拋出異常，回傳-1");

        }

        /*
        無論如何都必須將資料寫入Redis快取中, 主要目的是為了防止快取穿透
         */
        int views = (articleStatusFromDB == null)? -1 : articleStatusFromDB.getViewsCount();
        int likes = (articleStatusFromDB == null)? -1 : articleStatusFromDB.getLikesCount();
        int bookmarks = (articleStatusFromDB == null)? -1 : articleStatusFromDB.getBookmarksCount();
        int comments = (articleStatusFromDB == null)? -1 : articleStatusFromDB.getCommentsCount();

        String redisKey = RedisCacheKey.ARTICLE_STATUS.format(articleId);
        RMap<String, Integer> statusMap = redissonClient.getMap(redisKey, new TypedJsonJacksonCodec(String.class, Integer.class));
        statusMap.put("viewsCount", views);
        statusMap.put("likesCount", likes);
        statusMap.put("bookmarksCount", bookmarks);
        statusMap.put("commentsCount", comments);

        /*
        假設成功從Redis快取中獲取文章的指標則直接進行包裝並回傳
         */
        log.info("文章的狀態資訊 articleId:{},statusMap:{}",articleId,statusMap);
        //進行包裝並回傳
        return AmsArticleStatusVo.builder()
                .viewsCount(views)
                .likesCount(likes)
                .bookmarksCount(bookmarks)
                .commentsCount(comments)
                .build();
    }

    private AmsArticleStatusVo parseArticleStatusVoFromRedis(long articleId) {
        log.info("開始執行 parseArticleStatusFromRedis - articleId: {}", articleId);
        //嘗試從Redis取得文章的指標
        String redisKey = RedisCacheKey.ARTICLE_STATUS.format(articleId);

        RMap<String, Integer> statusMap = redissonClient.getMap(redisKey, new TypedJsonJacksonCodec(String.class, Integer.class));

        Map<String, Integer> stringIntMap = statusMap.readAllMap();
        log.info("stringIntMap: {}", stringIntMap);

        if(stringIntMap.isEmpty()){
            return null;
        }

        /*
        獲取文章指標的值
        假設文章指標為空，則將指標值設為-1
         */
        int views = stringIntMap.get("viewsCount");
        int likes = stringIntMap.get("likesCount");
        int bookmarks = stringIntMap.get("bookmarksCount");
        int comments = stringIntMap.get("commentsCount");

        AmsArticleStatusVo articleStatusVo = AmsArticleStatusVo.builder()
                .likesCount(likes)
                .bookmarksCount(bookmarks)
                .commentsCount(comments)
                .viewsCount(views)
                .build();
        log.info("articleStatusVo: {}", articleStatusVo);
        return articleStatusVo;
    }


    private AmsArtStatus QueryArticleStatus(long articleId) {
        log.info("開始執行 QueryArticleStatus articleId={}",articleId);


        AmsArtStatus amsArtStatus = amsArtStatusService.getOne(
                new LambdaQueryWrapper<AmsArtStatus>()
                        .select(AmsArtStatus::getViewsCount,AmsArtStatus::getBookmarksCount,AmsArtStatus::getCommentsCount,AmsArtStatus::getLikesCount)
                        .eq(AmsArtStatus::getArticleId, articleId)
                ,false
        );
        log.info("amsArtStatus={}",amsArtStatus);
        return amsArtStatus;
    }

    /**
     * 根據文章ID取得文章狀態
     * 採用Hash結構
     * @param articleId
     * @return
     */
    public AmsArticleStatusVo getArticleStatusVo(long articleId) {
        log.info("開始執行 getArticleStatusVo - articleId: {}", articleId);

        //先判斷是否存在該文章
        boolean articleNotExists = isArticleNotExists(articleId);
        if(articleNotExists){
            //假設文章不存在則直接拋出異常
            log.warn("文章不存在，articleId={}",articleId);
            throw new CustomBaseException("文章不存在");
        }

        //嘗試從Redis取得文章的指標
        String redisKey = RedisCacheKey.ARTICLE_STATUS.format(articleId);
        log.info("redisKey: {}", redisKey);
        //初步嘗試從Redis中讀取文章的指標
        AmsArticleStatusVo articleStatusVo = this.parseArticleStatusVoFromRedis(articleId);

        //判斷是否成功從Redis中獲取文章的指標或articleStatusVo中其中某個欄位是否為null
        if (articleStatusVo == null || articleStatusVo.allNull()) {
            log.info("Redis 快取未命中,從資料庫載入 - articleId: {}", articleId);
            /*
            假設未成功從Redis獲取文章的指標，或者指標鍵值有遺漏
             */
            //從DB資料庫中查詢
            articleStatusVo = redisCacheLoaderUtils.loadMapWithLock(
                    () -> loadArticleStatusVo(articleId),
                    () -> this.parseArticleStatusVoFromRedis(articleId),
                    3,
                    10,
                    TimeUnit.SECONDS,
                    3,
                    RedisLockKey.ARTICLE_STATUS_LOCK.getFormat(articleId),
                    redisKey
            );




        }
        /*
        假設成功從Redis快取中獲取文章的指標則直接進行包裝並回傳
         */
        log.info("文章的包裝後指標資訊 articleStatusVo:{}", articleStatusVo);
        return articleStatusVo;
    }


    /**
     * 判斷文章是否不存在，透過布隆過濾器以及資料庫雙重確認，非分佈式鎖版本
     * @param articleId
     * @return
     */
    @Override
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

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateArticle(Long articleId, AmsArticleUpdateDTO amsArticleUpdateDTO) {

        /**
         * 透過布隆過濾器判斷文章id是否絕對不存在, 若不存在則拋出異常
         */
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(RedisBloomFilterKey.ARTICLE_BLOOM_FILTER.getKey());

        boolean contains = bloomFilter.contains(articleId);
        if(!contains){
            log.warn("文章不存在，articleId={}",articleId);
            throw new CustomBaseException("文章不存在");
        }



        /**
         * 判斷更新資料是否為空
         */
        if(amsArticleUpdateDTO==null){
            log.warn("更新資料不存在，articleId={}",articleId);
            throw new CustomBaseException("更新資料不存在");
        }

        /**
         * 判斷用戶是否登入, 是否為文章的作者或者管理員權限
         */
        if (!UserContextHolder.isCurrentUserLoggedIn()) {
            log.warn("未取得登入用戶資訊，拒絕編輯文章");
            throw new CustomBaseException("未登入或登入狀態已失效");
        }


        /**
         * 判斷用戶是否登入, 是否為文章的作者或者管理員權限或者足夠的權限
         */

        Long userId = UserContextHolder.getCurrentUserId();

//        String userNameFromToken = UserContextHolder.getCurrentUserNickname();
        if (userId == null) {
            log.warn("用戶ID為空，拒絕編輯文章");
            throw new CustomBaseException("用戶ID缺失");
        }

        //判斷該用戶使否為文章的作者
        int isArtOwner= amsArtinfoService.isArticleOwner(articleId, userId);
        //假設isArtOwner為0表示不是文章的作者,否則是文章的作者
        if(isArtOwner == 0){
            //並非文章的作者,因此需要判斷是否為管理員或者足夠的權限
            //進一步判斷是否為管理員或者足夠的權限
            /// TODO測試管理員權限是否成功實施
//            getHeader(HEADER_USER_ROLE);
            boolean currentUserAdmin = UserContextHolder.isCurrentUserAdmin();
            //判斷是否為管理員或者足夠的權限
            if(!currentUserAdmin){
                //不是文章的作者也不是管理員
                log.warn("權限不足，您無權編輯該文章.UserId:{},ArticleId:{}",userId,articleId);
                throw new CustomBaseException("權限不足，您無權編輯該文章");
            }

        }




        /**
         * 更新文章內容
         */
        //若某個屬性為null，不會進行更新,可以透過 TableField 方法設置策略
        LambdaUpdateWrapper<AmsArticle> articleUpdateWrapper = Wrappers.lambdaUpdate();
        articleUpdateWrapper.eq(AmsArticle::getId, articleId);
        articleUpdateWrapper.set(AmsArticle::getTitle,amsArticleUpdateDTO.getTitle());

        // 將原始文章內容從Markdown轉為HTML格式
        String articleMd = CommonmarkUtils.parseMdToHTML(amsArticleUpdateDTO.getContent());

        articleUpdateWrapper.set(AmsArticle::getContent,articleMd);
        this.baseMapper.update(articleUpdateWrapper);



        /**
         * 更新文章info內容
         */
        //若某個屬性為null，不會進行更新
        LambdaUpdateWrapper<AmsArtinfo> artInfoUpdateWrapper = Wrappers.lambdaUpdate();
        artInfoUpdateWrapper.eq(AmsArtinfo::getArticleId, articleId);
        artInfoUpdateWrapper.set(AmsArtinfo::getCategoryId,amsArticleUpdateDTO.getCategoryId());
        amsArtinfoService.update(artInfoUpdateWrapper);
        /**
         * 判斷文章是否存在舊標籤, 無論是否有新標籤都需要刪除舊標籤
         * 因為假設情況：
         * 1、有新標籤, 代表要新增標籤, 需要刪除舊標籤再增加新標籤
         * 2、沒有新標籤, 代表不設置標籤, 需要刪除舊標籤
         */
        //取得文章的舊標籤ID
        List<AmsArtTag> oldTagList = amsArtTagService.list(new LambdaQueryWrapper<AmsArtTag>().eq(AmsArtTag::getArticleId, articleId));
        //判斷該文章是否存在舊標籤
        if(oldTagList!=null && !oldTagList.isEmpty()){
            //刪除該文章原本的所有標籤
            boolean removed = amsArtTagService.removeByIds(oldTagList.stream().map(AmsArtTag::getId).toList());
            if(removed){
                log.info("文章ID:{},刪除舊標籤成功",articleId);
            }else {
                log.error("文章ID:{},刪除舊標籤失敗",articleId);
                throw new CustomBaseException("文章ID:"+articleId+",刪除舊標籤失敗");
            }
        }



        /**
         * 更新文章標籤
         */

            /**
             * 先判斷要修改的標籤是否存在
             */

            List<Long> newArtTagsIdList = amsArticleUpdateDTO.getTagsId();
            //假設存在要修改的標籤
            if (newArtTagsIdList!=null && !newArtTagsIdList.isEmpty()) {
                /**
                 * 判斷新的標籤是否存在, 避免新增了一個不存在的標籤
                 */

                List<AmsTags> newTagList = amsTagsService.listByIds(newArtTagsIdList);
                //要修改的標籤存在於資料庫中
                if(newTagList !=null&& !newTagList.isEmpty()){

                    /**
                     * 增加新的標籤
                     */
                    List<AmsArtTag> amsArtTags = newArtTagsIdList.stream().map(tagsId -> {
                        AmsArtTag amsArtTag = new AmsArtTag();
                        amsArtTag.setTagsId(tagsId);
                        amsArtTag.setArticleId(articleId);
                        return amsArtTag;
                    }).toList();//包裝成amsArtTag對象(tagsId以及articleId)

                    boolean saveBatch = amsArtTagService.saveBatch(amsArtTags);
                    if(!saveBatch){
                        log.error("文章ID:{},增加新標籤失敗",articleId);
                        throw new CustomBaseException("文章ID:"+articleId+",增加新標籤失敗");
                    }

                }

            }
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
//        //獲取桶對象, 注意原子性因此採用RAtomicLong
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
////        String BucketName = RedisCacheKey.ARTICLE_COMMENTS_LIKES.format(commentId);
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
