package com.shijiawei.secretblog.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shijiawei.secretblog.article.annotation.DelayDoubleDelete;
import com.shijiawei.secretblog.article.dto.AmsArticleUpdateDTO;
import com.shijiawei.secretblog.article.dto.ArticlePreviewQueryDto;
import com.shijiawei.secretblog.article.entity.*;
import com.shijiawei.secretblog.article.feign.UserFeignClient;


import com.shijiawei.secretblog.article.service.*;
import com.shijiawei.secretblog.article.utils.CommonmarkUtils;
import com.shijiawei.secretblog.article.vo.*;

import com.shijiawei.secretblog.common.codeEnum.IErrorCode;
import com.shijiawei.secretblog.common.codeEnum.ResultCode;
import com.shijiawei.secretblog.common.exception.BusinessException;
import com.shijiawei.secretblog.common.exception.BusinessRuntimeException;
import com.shijiawei.secretblog.common.myenum.RedisBloomFilterKey;
import com.shijiawei.secretblog.common.myenum.RedisCacheKey;
import com.shijiawei.secretblog.common.annotation.OpenCache;
import com.shijiawei.secretblog.common.dto.UserBasicDTO;
import com.shijiawei.secretblog.article.annotation.OpenLog;
import com.shijiawei.secretblog.article.mapper.AmsArticleMapper;
import com.shijiawei.secretblog.common.myenum.RedisLockKey;
import com.shijiawei.secretblog.common.myenum.RedisOpenCacheKey;
import com.shijiawei.secretblog.common.redisutils.RedisLuaScripts;
import com.shijiawei.secretblog.common.utils.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.LongValue;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.*;
import org.redisson.client.RedisConnectionException;
import org.redisson.client.RedisTimeoutException;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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

    @Autowired
    private AmsArtActionService amsArtActionService;


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

    /**
     * 保存文章
     * @param amsSaveArticleVo 文章資料保存VO
     * @param httpServletRequest HTTP請求
     * @param authentication 認證物件
     */
    @OpenLog//開啟方法執行時間紀錄
    @DelayDoubleDelete(prefix = "AmsArticles", key = "categoryId_#{#amsSaveArticleVo.categoryId}")
//    @DelayDoubleDelete(prefix = "AmsArticle",key = "articles",delay = 5,timeUnit = TimeUnit.SECONDS)//AOP延遲雙刪
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveArticles(AmsSaveArticleVo amsSaveArticleVo, HttpServletRequest httpServletRequest,Authentication authentication) {

        if((amsSaveArticleVo.getTitle()==null)||(amsSaveArticleVo.getTitle().isEmpty())){
            log.warn("文章標題為空，拒絕發佈文章");
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.PARAM_MISSING)
                    .detailMessage("文章標題不可為空")
                    .build();
        }
        log.info("準備新增文章，標題:{}，分類ID:{}，tags:{}",
                amsSaveArticleVo.getTitle(),
                amsSaveArticleVo.getCategoryId(),
                amsSaveArticleVo.getTagsId());


        AmsArticle amsArticle = new AmsArticle();
        AmsArtinfo amsArtinfo = new AmsArtinfo();
        // 從網關傳遞的請求標頭中取得用戶資訊
        if (!UserContextHolder.isCurrentUserLoggedIn()) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.UNAUTHORIZED)
                    .detailMessage("用戶未登入，拒絕發佈文章")
                    .data(Map.of("title",StringUtils.defaultString(amsSaveArticleVo.getTitle(),"")))
                    .build();
        }

        Long userId = UserContextHolder.getCurrentUserId();


//        String userNameFromToken = UserContextHolder.getCurrentUserNickname();
        if (userId == null) {
//            log.warn("用戶ID為空，拒絕發佈文章 - 文章標題:{}", amsSaveArticleVo.getTitle());
//            throw new CustomRuntimeException("用戶ID缺失");
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.NOT_FOUND)
                    .detailMessage("用戶不存在，拒絕發佈文章")
                    .data(Map.of("articleTitle",StringUtils.defaultString(amsSaveArticleVo.getTitle(),"")))
                    .build();
        }
        log.info("成功驗證用戶登入狀態,開始保存文章 - 用戶ID:{}, 標題:{}, 分類ID:{} , 標籤ID:{}",
                userId, amsSaveArticleVo.getTitle(), amsSaveArticleVo.getCategoryId(),amsSaveArticleVo.getTagsId()!=null?amsSaveArticleVo.getTagsId():"未附加標籤");
        try {

            amsArticle.setTitle(amsSaveArticleVo.getTitle());

            /// TODO保存原始文章內容用於安全方面, 避免從原始文章內容從Markdown格式轉換HTML時遺漏某些字等(編輯文章時要更新兩者、讀取時只讀取轉換後的文章)
            log.debug("即將轉換Markdown文章內容,原始文章內容長度:{}", amsSaveArticleVo.getContent() != null ? amsSaveArticleVo.getContent().length() : 0);

            // 將原始文章內容從Markdown轉為HTML格式
            String html = CommonmarkUtils.parseMdToHTML(amsSaveArticleVo.getContent());

            log.debug("Markdown格式的文章內容轉換完成,HTML內容長度:{}", html != null ? html.length() : 0);
            //將Markdown格式的文章保存至資料庫中
            amsArticle.setContent(html);

            this.baseMapper.insert(amsArticle);
            log.info("文章主體已寫入DB，文章ID:{}", amsArticle.getId());

            amsArtinfo.setCategoryId(amsSaveArticleVo.getCategoryId());
            log.debug("透過 OpenFeign 請求用戶資訊，userId:{}", userId);

            R<UserBasicDTO> user = userFeignClient.getUserById(userId);
            if(user.getData()!=null){
                log.debug("成功透過OpenFeign獲取用戶資訊 - userId: {}, nickName: {}", userId, user.getData().getNickName());
//                amsArtinfo.setAccountName(user.getData().getAccountName());
                amsArtinfo.setNickName(user.getData().getNickName());
                amsArtinfo.setAvatar(user.getData().getAvatar());
            }else {
                log.warn("透過OpenFeign獲取用戶資訊失敗 - userId: {}", userId);

            }


            //▼
//            amsArtinfo.setNickName(userNameFromToken);
//            amsArtinfo.setAvatar(userNameFromToken);



            amsArtinfo.setArticleId(amsArticle.getId());
            amsArtinfo.setUserId(userId);
            amsArtinfoService.save(amsArtinfo);
            log.debug("文章附加資訊已寫入DB，文章ID:{}, userId:{}", amsArticle.getId(), userId);

            List<Long> tagIds = amsSaveArticleVo.getTagsId();
            if (tagIds != null && !tagIds.isEmpty()) {
                log.debug("準備寫入文章標籤，文章ID:{}，tagIds:{}", amsArticle.getId(), tagIds);

                List<AmsArtTag> amsArtTagList = tagIds.stream().map(tagsId -> {
                    AmsArtTag amsArtTag = new AmsArtTag();
                    amsArtTag.setTagsId(tagsId);
                    amsArtTag.setArticleId(amsArticle.getId());
                    return amsArtTag;
                }).toList();
                amsArtTagService.saveBatch(amsArtTagList);
                log.info("成功將標籤加入文章 - articleId: {}, tagIds: {}", amsArticle.getId(), tagIds);
            } else {
                log.debug("本次發佈未附加標籤或標籤列表為空 - articleId: {}" ,amsArticle.getId());
            }

            AmsArtStatus amsArtStatus = new AmsArtStatus();
            amsArtStatus.setArticleId(amsArticle.getId());
            amsArtStatusService.save(amsArtStatus);
            log.debug("文章狀態已初始化，文章ID:{}", amsArticle.getId());

            log.info("創建文章成功, 文章ID:{}, 用戶ID:{}, 標題:{}, 分類ID:{} , 標籤數量:{}",
                    amsArticle.getId(), userId, amsSaveArticleVo.getTitle(),
                    amsSaveArticleVo.getCategoryId(),
                    (tagIds != null && !tagIds.isEmpty()) ? tagIds.size() : 0);
        } catch (Exception e) {
//            log.error("創建文章過程失敗，將回滾事務,userId:{}, 標題:{}, 分類ID:{}",
//                    userId, amsSaveArticleVo.getTitle(), amsSaveArticleVo.getCategoryId(), e);
//            throw new CustomRuntimeException("系統異常，請稍後再試"); // 讓 @Transactional 觸發回滾
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.ARTICLE_INTERNAL_ERROR)
                    .detailMessage("創建文章過程失敗")
                    .cause(e.getCause())
                    .data(Map.of("title",amsSaveArticleVo.getTitle(),
                            "categoryId", ObjectUtils.defaultIfNull(amsSaveArticleVo.getCategoryId(), "")))
                    .build();
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


    /**
     * 獲取文章預覽列表分頁

     * @return 文章預覽列表分頁VO
     */
    //    @OpenLog
//    @OpenCache(prefix = "AmsArticles", key = "categoryId_#{#categoryId}:routerPage_#{#routePage}:articles")
    @OpenCache(
            prefix = "AmsArticles",
            key = "categoryId_#{#categoryId}:routerPage_#{#routePage}:tags_#{#tagsId == null || #tagsId.isEmpty() ? 'NONE' : #tagsId.toString()}",
            time = 3,
            chronoUnit = ChronoUnit.MINUTES /// TODO暫時將快取TTL設置為3分鐘
    )
    @Override
    public IPage<AmsArticlePreviewVo> getArticlesPreviewPage(Integer routePage,Long categoryId) {
        log.debug("開始取得文章預覽列表 - categoryId: {}, routePage: {} ",categoryId, routePage);

        final int pageSize = 20;

        Page<AmsArticlePreviewVo> page = new Page<>(routePage, pageSize);


        IPage<AmsArticlePreviewVo> resultPage = this.baseMapper.getArticlesPreviewPage(page, routePage,categoryId);



        log.debug("文章預覽查詢完成，總條數:{}，當前頁:{}，每頁數量:{}", resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize());

        //目標是重新包裝其中的amsArtTagList欄位
        List<AmsArticlePreviewVo> articles = resultPage.getRecords();
        if(articles.isEmpty()){
            //不拋出異常只記錄
            log.warn("獲取文章預覽時結果為空, categoryId:{} , routerPage:{}",categoryId,routePage);
            //返回空的資料
            return new Page<>();
//            throw BusinessRuntimeException.builder()
//                    .iErrorCode(ResultCode.ARTICLE_INTERNAL_ERROR)
//                    .detailMessage("獲取文章預覽時結果為空")
//                    .data(Map.of("categoryId",ObjectUtils.defaultIfNull(categoryId, ""),
//                            "routePage", ObjectUtils.defaultIfNull(routePage, "")))
//                    .build();
        }
        //獲取文章所需的標籤IDS，目標是取得標籤的名稱等資訊


        Stream<AmsArtTagsVo> amsArtTagsVoStream = articles.stream().flatMap(item -> item.getAmsArtTagList().stream());
        Set<Long> tagsIdSet = amsArtTagsVoStream.map(AmsArtTagsVo::getId).collect(Collectors.toSet());
        log.debug("彙總標籤ID完成，標籤ID數量:{}", tagsIdSet.size());

        Map<Long, AmsTags> tagsList = amsTagsService.getArtTagsByIds(tagsIdSet);
        log.debug("從DB取得標籤實體數量:{}", tagsList != null ? tagsList.size() : 0);


        articles.stream()
                .flatMap(article -> article.getAmsArtTagList().stream())
                .forEach(
                        AmsArtTagsVo->{
                            AmsTags tags = tagsList.get(AmsArtTagsVo.getId());
                            if(tags!=null){
                                AmsArtTagsVo.setName(tags.getName());
                            }else{
                                log.warn("標籤 ID {} 不存在於 tagsList 中", AmsArtTagsVo.getId());
                            }
                        });

        articles.stream().forEach(item->{
            List<AmsArtTagsVo> amsArtTagList = item.getAmsArtTagList();
            if (amsArtTagList == null) {
                log.debug("文章 文章ID:{} 無標籤列表", item.getArticleId());
                return;
            }
            amsArtTagList.forEach(amsArtTagsVo -> {
                AmsTags tags = tagsList.get(amsArtTagsVo.getId());
                if (tags != null) {
                    amsArtTagsVo.setName(tags.getName());
                }
            });
        });

        log.debug("文章預覽標籤填充完成，categoryId:{}，routePage:{}", categoryId, routePage);
        log.debug("文章預覽結果返回，records:{}，total:{}", resultPage.getRecords().size(), resultPage.getTotal());
        return resultPage;

    }



    /**
     * 獲取帶指標的文章詳情
     * @param articleId 文章ID
     * @return 文章詳情VO
     */
    @Override
    public AmsArticleVo getAmsArticleVoWithStatus(Long articleId){
        log.info("開始獲取帶指標的文章詳情 - 文章ID:{}", articleId);

        isArticleNotExists(articleId);

        AmsArticleVo amsArticleVo = amsArticleService.getAmsArticleVo(articleId);
//        AmsArticleVo amsArticleVo = getAmsArticleVo(articleId);
        if(amsArticleVo==null){

            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.NOT_FOUND)
                    .detailMessage("文章不存在")
                    .data(Map.of("articleId", ObjectUtils.defaultIfNull(articleId, "")))
                    .build();
        }
        Long amsArticleVoId = amsArticleVo.getId();
        if (amsArticleVoId == null) {

            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.NOT_FOUND)
                    .detailMessage("文章不存在")
                    .data(Map.of("articleId", ObjectUtils.defaultIfNull(articleId, "")))
                    .build();
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

        long newViews = 0L;
        try {
            newViews = incrementArticleViewsCount(amsArticleVoId);
            log.debug("文章瀏覽數累加成功，文章ID:{}，views:{}", amsArticleVoId, newViews);

            // 建議 VO 改為 long，避免溢位
//            amsArticleVo.setViewsCount(Math.toIntExact(views));
        } catch (RedisConnectionException | RedisTimeoutException e) {
            log.error("Redis 計數失敗，改用 DB 回填 viewsCount，文章ID:{}, err:{}",
                    amsArticleVoId, e.getMessage());

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

        amsArticleVo.setViewsCount(Math.toIntExact(newViews));

        log.info("獲取帶指標的文章詳情成功，文章ID:{}，views:{}，likes:{}，bookmarks:{}，comments:{}",
                articleId,
                articleStatus.getViewsCount(),
                articleStatus.getLikesCount(),
                articleStatus.getBookmarksCount(),
                articleStatus.getCommentsCount());
        ///TODO注意安全性




//        Long articleLikes = getArticleLikes(articleId);
//        Long articleBookmarks = getArticleBookmarks(articleId);
//        amsArticleVo.setLikesCount(articleLikes.intValue());
//        amsArticleVo.setBookmarksCount(articleBookmarks.intValue());


        return amsArticleVo;
    }

    /**
     * 獲取文章詳情
     * @param articleId 文章ID
     * @return 文章詳情VO
     */
    @OpenCache(prefix = RedisOpenCacheKey.ArticleDetails.ARTICLE_DETAILS_PREFIX,key = RedisOpenCacheKey.ArticleDetails.ARTICLE_DETAILS_KEY,time = 30,chronoUnit = ChronoUnit.MINUTES)
    @Override
    public AmsArticleVo getAmsArticleVo(Long articleId) {
        log.info("獲取文章詳情，articleId={}",articleId);

        isArticleNotExists(articleId);


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
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.NOT_FOUND)
                    .detailMessage("文章不存在")
                    .data(Map.of("articleId", ObjectUtils.defaultIfNull(articleId, "")))
                    .build();
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
//        設置留言數量
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

    /**
     * 用戶瀏覽文章，累加文章瀏覽數
     * @param articleId 文章ID
     * @return 新的文章瀏覽數
     */
    public Long incrementArticleViewsCount(Long articleId) {
        log.info("開始累加文章瀏覽數，文章ID={}",articleId);

        isArticleNotExists(articleId);


//        /*
//          檢查用戶是否登入
//        */
//
//        if (!UserContextHolder.isCurrentUserLoggedIn()) {
//            log.warn("未取得登入用戶資訊，拒絕對文章按讚");
//            throw new CustomRuntimeException("未登入或登入狀態已失效");
//        }
//        Long userId = UserContextHolder.getCurrentUserId();
//        String userNameFromToken = UserContextHolder.getCurrentUserNickname();
//        if (userId == null) {
//            log.warn("用戶ID為空，拒絕對文章按讚");
//            throw new CustomRuntimeException("用戶ID缺失");
//        }

        /// TODO(可選)根據IP、UA、Cookie等資訊限制同一用戶在短時間內多次刷新導致瀏覽數異常增長

        /**
         * 驗證該文章是否存在
         */


        /**
         * 同步點讚數從Redis到資料庫中
         */
        //例如：ams:article:views:1965494783750287361
        final String redisKey = RedisCacheKey.ARTICLE_VIEWS.format(articleId);
        log.debug("文章瀏覽數 快取鍵:{}",redisKey);
        //獲取桶對象, 注意原子性因此採用RAtomicLong


        /// TODO排程定時將Redis中的瀏覽數同步到資料庫中

        RAtomicLong viewCounter = redissonClient.getAtomicLong(redisKey);
        log.debug("取得文章瀏覽數計數器對象完成，文章ID:{}", articleId);
        //設置該文章查看人數值為++ , 注意原子性
        long newViews = viewCounter.incrementAndGet();


        if (newViews == 1) {
            viewCounter.clearExpire(); // 移除任何可能存在的 TTL
            log.debug("文章ID:{} 產生第一次瀏覽，已確保快取為持久化", articleId);
        }

        log.info("文章瀏覽數增加完成，文章ID:{}，新的文章瀏覽數:{}", articleId, newViews);
        return newViews;
    }

    /**
     * 用戶對文章點讚
     * @param articleId 文章ID
     * @return 新的文章點讚數
     */
    @Override
    public Long incrementArticleLikes(Long articleId) {
        log.info("開始累加文章點讚數，文章ID={}",articleId);

        isArticleNotExists(articleId);

        /*
          檢查用戶是否登入
        */

        if (!UserContextHolder.isCurrentUserLoggedIn()) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.UNAUTHORIZED)
                    .detailMessage("用戶未登入，拒絕對文章按讚")
                    .data(Map.of("articleId", ObjectUtils.defaultIfNull(articleId, "")))
                    .build();
        }
        Long userId = UserContextHolder.getCurrentUserId();
        log.info("用戶文章點讚文章, 用戶ID:{} , 文章ID:{}", userId, articleId);




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
         * 檢查用戶是否已經點讚過該文章
         * 先檢查Redis中的點讚用戶集合
         */
        final String userLikeKey = RedisCacheKey.ARTICLE_LIKED_USERS.format(articleId);
        RSet<String> likedUsersSet = redissonClient.getSet(userLikeKey, StringCodec.INSTANCE);

        if (likedUsersSet.contains(userId.toString())) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.REPEAT_OPERATION)
                    .detailMessage("用戶已經點讚過該文章, 不允許重複點讚")
                    .data(Map.of(
                            "userId", ObjectUtils.defaultIfNull(userId, ""),
                            "articleId", ObjectUtils.defaultIfNull(articleId, "")
                    ))
                    .build();
        }

        /**
         *  記錄/更新使用者對該文章的點讚狀態至 AmsArtAction
         */
        try {
//            AmsArtAction action = AmsArtAction.builder()
//                    .articleId(articleId)
//                    .userId(userId)
//                    .isLiked((byte) 1)
//                    //不改動isBookmarked,新增時默認為0
//                    .build();
//            amsArtActionService.saveOrUpdate(action);

            // 1. 先根據 業務唯一鍵 (articleId + userId) 查詢資料庫
            LambdaQueryWrapper<AmsArtAction> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AmsArtAction::getArticleId, articleId)
                    .eq(AmsArtAction::getUserId, userId);

            AmsArtAction existingAction = amsArtActionService.getOne(queryWrapper);

            if (existingAction != null) {
                // 2. 如果存在 -> 執行 Update

                // 更新狀態
                existingAction.setIsLiked((byte) 1);
                // updateAt 會由 MP 自動填充
                boolean update = amsArtActionService.updateById(existingAction);
                if(!update){
                    throw BusinessRuntimeException.builder()
                            .iErrorCode(ResultCode.ARTICLE_INTERNAL_ERROR)
                            .detailMessage("更新用戶點讚狀態失敗")
                            .data(Map.of(
                                    "userId", ObjectUtils.defaultIfNull(userId, ""),
                                    "articleId", ObjectUtils.defaultIfNull(articleId, "")
                            ))
                            .build();
                }
            } else {
                // 3. 如果不存在 -> 執行 Insert
                AmsArtAction newAction = AmsArtAction.builder()
                        .articleId(articleId)
                        .userId(userId)
                        .isLiked((byte) 1)
                        // .isBookmarked((byte) 0) // 預設值，視需求
                        .build();
                boolean save = amsArtActionService.save(newAction);
                if(!save){
                    throw BusinessRuntimeException.builder()
                            .iErrorCode(ResultCode.ARTICLE_INTERNAL_ERROR)
                            .detailMessage("新增用戶點讚狀態失敗")
                            .data(Map.of(
                                    "userId", ObjectUtils.defaultIfNull(userId, ""),
                                    "articleId", ObjectUtils.defaultIfNull(articleId, "")
                            ))
                            .build();
                }
            }


        } catch (DuplicateKeyException e)   {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.REPEAT_OPERATION)
                    .detailMessage("用戶已經點讚過該文章, 不允許重複點讚")
                    .data(Map.of(
                            "userId", ObjectUtils.defaultIfNull(userId, ""),
                            "articleId", ObjectUtils.defaultIfNull(articleId, "")
                    ))
                    .build();
        } catch (Exception e) {
//            log.error("同步使用者點讚行為至 AmsArtAction 失敗, articleId:{}, userId:{}", articleId, userId, e);
            throw BusinessException.builder()
                    .iErrorCode(ResultCode.ARTICLE_INTERNAL_ERROR)
                    .detailMessage("同步使用者點讚行為至 AmsArtAction 失敗")
                    .data(Map.of(
                            "articleId", ObjectUtils.defaultIfNull(articleId, ""),
                            "userId", ObjectUtils.defaultIfNull(userId, "")
                    ))
                    .build();
        }

        /**
         * Redis操作
         * 1、將用戶ID加入到該文章的點讚用戶集合中
         * 2、增加該文章的點讚數
         * 會檢查該用戶是否已經點讚過該文章，若未點過則修改紀錄成已經點過，防止重複點讚
         */

//        final String likesCountKey = RedisCacheKey.ARTICLE_LIKES.format(articleId);

        final String articleStatusKey = RedisCacheKey.ARTICLE_STATUS.format(articleId);

        // Lua 腳本：將用戶加入點讚集合並增加點讚數
        String luaScript =
                "local added = redis.call('SADD', KEYS[1], ARGV[1]) \n" +
                        "if added == 1 then \n" +
                        "    local count = redis.call('HINCRBY', KEYS[2] , 'likesCount' , 1) \n" +
                        "    return count \n" +
                        "else \n" +
                        "    return -1 \n" +
                        "end";

        RScript script = redissonClient.getScript(StringCodec.INSTANCE);
        Long result = script.eval(
                RScript.Mode.READ_WRITE,
                luaScript,
                RScript.ReturnType.INTEGER,
                Arrays.asList(userLikeKey, articleStatusKey),
                userId.toString()
        );
        if (result == -1) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.REPEAT_OPERATION)
                    .detailMessage("用戶已經點讚過該文章, 不允許重複點讚")
                    .data(Map.of(
                            "userId", ObjectUtils.defaultIfNull(userId, ""),
                            "articleId", ObjectUtils.defaultIfNull(articleId, "")
                    ))
                    .build();
        }


        log.info("文章點讚數增加完成，文章ID:{}，新的文章點讚數:{}", articleId, result);

        return result;
    }

    /**
     * 用戶對文章取消點讚
     * @param articleId 文章ID
     * @return 新的文章點讚數
     */
    @Override
    public Long decrementArticleLikes(Long articleId) {
        log.info("開始取消文章點讚數，文章ID={}", articleId);

        isArticleNotExists(articleId);

        /*
          檢查用戶是否登入
        */

        if (!UserContextHolder.isCurrentUserLoggedIn()) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.UNAUTHORIZED)
                    .detailMessage("用戶未登入，拒絕取消文章按讚")
                    .data(Map.of("articleId", ObjectUtils.defaultIfNull(articleId, "")))
                    .build();
        }
        Long userId = UserContextHolder.getCurrentUserId();
        log.info("用戶取消文章點讚, 用戶ID:{} , 文章ID:{}", userId, articleId);

        /**
         * 檢查用戶是否已經點讚過該文章
         * 先檢查Redis中的點讚用戶集合
         */
        final String userLikeKey = RedisCacheKey.ARTICLE_LIKED_USERS.format(articleId);
        RSet<String> likedUsersSet = redissonClient.getSet(userLikeKey, StringCodec.INSTANCE);

        if (!likedUsersSet.contains(userId.toString())) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.REPEAT_OPERATION)
                    .detailMessage("用戶尚未點讚該文章, 無法取消點讚")
                    .data(Map.of(
                            "userId", ObjectUtils.defaultIfNull(userId, ""),
                            "articleId", ObjectUtils.defaultIfNull(articleId, "")
                    ))
                    .build();
        }

        /**
         *  更新使用者對該文章的點讚狀態至 AmsArtAction
         */
        try {
//            AmsArtAction action = AmsArtAction.builder()
//                    .articleId(articleId)
//                    .userId(userId)
//                    .isLiked((byte) 0)
//                    //不改動isBookmarked,新增時默認為0
//                    .build();
//            amsArtActionService.saveOrUpdate(action);

            LambdaUpdateWrapper<AmsArtAction> updateWrapper = new LambdaUpdateWrapper<AmsArtAction>()
                    .eq(AmsArtAction::getArticleId, articleId)
                    .eq(AmsArtAction::getUserId, userId)
                    .set(AmsArtAction::getIsLiked, (byte) 0);

            boolean update = amsArtActionService.update(updateWrapper);
            if(!update){
                throw BusinessRuntimeException.builder()
                        .iErrorCode(ResultCode.ARTICLE_INTERNAL_ERROR)
                        .detailMessage("同步使用者取消點讚行為至 AmsArtAction 失敗")
                        .data(Map.of(
                                "userId", ObjectUtils.defaultIfNull(userId, ""),
                                "articleId", ObjectUtils.defaultIfNull(articleId, "")
                        ))
                        .build();
            }


        } catch (Exception e) {
            throw BusinessException.builder()
                    .iErrorCode(ResultCode.ARTICLE_INTERNAL_ERROR)
                    .detailMessage("同步使用者取消點讚行為至 AmsArtAction 失敗")
                    .data(Map.of(
                            "articleId", ObjectUtils.defaultIfNull(articleId, ""),
                            "userId", ObjectUtils.defaultIfNull(userId, "")
                    ))
                    .build();
        }

        /**
         * Redis操作
         * 1、將用戶ID從該文章的點讚用戶集合中移除
         * 2、減少該文章的點讚數
         * 會檢查該用戶是否已經點讚過該文章，若已點過則修改紀錄成未點過，防止重複取消點讚
         */

        final String articleStatusKey = RedisCacheKey.ARTICLE_STATUS.format(articleId);

        // Lua 腳本：將用戶從點讚集合移除並減少點讚數
        String luaScript =
                "local removed = redis.call('SREM', KEYS[1], ARGV[1]) \n" +
                        "if removed == 1 then \n" +
                        "    local count = redis.call('HINCRBY', KEYS[2] , 'likesCount' , -1) \n" +
                        "    return count \n" +
                        "else \n" +
                        "    return -1 \n" +
                        "end";

        RScript script = redissonClient.getScript(StringCodec.INSTANCE);
        Long result = script.eval(
                RScript.Mode.READ_WRITE,
                luaScript,
                RScript.ReturnType.INTEGER,
                Arrays.asList(userLikeKey, articleStatusKey),
                userId.toString()
        );
        if (result == -1) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.REPEAT_OPERATION)
                    .detailMessage("用戶尚未點讚該文章, 無法取消點讚")
                    .data(Map.of(
                            "userId", ObjectUtils.defaultIfNull(userId, ""),
                            "articleId", ObjectUtils.defaultIfNull(articleId, "")
                    ))
                    .build();
        }

        log.info("文章點讚數減少完成，文章ID:{}，新的文章點讚數:{}", articleId, result);

        return result;
    }


    /**
     * 用戶對文章加入書籤
     * @param articleId 文章ID
     * @return 新的文章書籤數
     */
    @Override
    public Long incrementArticleBooksMarket(Long articleId) {
        log.info("開始對文章加入書籤，articleId:{}", articleId);

        isArticleNotExists(articleId);


        /*
          檢查用戶是否登入
        */

        if (!UserContextHolder.isCurrentUserLoggedIn()) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.UNAUTHORIZED)
                    .detailMessage("用戶未登入，拒絕對文章加入書籤")
                    .build();
        }
        Long userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.NOT_FOUND)
                    .detailMessage("用戶ID不存在，拒絕對文章加入書籤")
                    .build();
        }

        log.debug("用戶嘗試對文章加入書籤，userId:{}，articleId:{}", userId, articleId);

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
         * Redis操作
         * 1、將用戶ID加入到該文章的收藏用戶集合中
         * 2、增加該文章的收藏數
         * 會檢查該用戶是否已經收藏過該文章，若未收藏過則修改紀錄成已經收藏，防止重複收藏
         */

        /**
         * 檢查用戶是否已經點讚過該文章
         * 先檢查Redis中的點讚用戶集合
         */
        final String userBookMarksKey = RedisCacheKey.ARTICLE_MARKED_USERS.format(articleId);
        RSet<String> bookmarkedUsersSet = redissonClient.getSet(userBookMarksKey, StringCodec.INSTANCE);

        if (bookmarkedUsersSet.contains(userId.toString())) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.REPEAT_OPERATION)
                    .detailMessage("用戶已經加入書籤過該文章, 不允許重複加入")
                    .data(Map.of(
                            "userId", ObjectUtils.defaultIfNull(userId, ""),
                            "articleId", ObjectUtils.defaultIfNull(articleId, "")
                    ))
                    .build();
        }
        /**
         *  記錄/更新使用者對該文章的點讚狀態至 AmsArtAction
         */
        try {
            AmsArtAction action = AmsArtAction.builder()
                    .articleId(articleId)
                    .userId(userId)
                    //不改動isLiked,新增時默認為0
                    .isBookmarked((byte) 1)
                    .build();
            amsArtActionService.saveOrUpdate(action);

        } catch (Exception e) {
            log.error("同步使用者加入書籤行為至 AmsArtAction 失敗, articleId:{}, userId:{}", articleId, userId, e);
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.ARTICLE_INTERNAL_ERROR)
                    .detailMessage("同步使用者加入書籤行為至 AmsArtAction 失敗")
                    .data(Map.of(
                            "articleId", ObjectUtils.defaultIfNull(articleId, ""),
                            "userId", ObjectUtils.defaultIfNull(userId, "")
                    ))
                    .build();
        }
        /**
         *
         */


        final String articleStatusKey = RedisCacheKey.ARTICLE_STATUS.format(articleId);
        log.debug("用戶書籤快取鍵:{}, 文章指標快取鍵:{}", userBookMarksKey, articleStatusKey);

        // Lua 腳本保證原子性
        String luaScript =
                "local added = redis.call('SADD', KEYS[1], ARGV[1]) \n" +
                        "if added == 1 then \n" +
                        "    local count = redis.call('HINCRBY', KEYS[2] , 'bookmarksCount' , 1) \n" +
                        "    return count \n" +
                        "else \n" +
                        "    return -1 \n" +
                        "end";

        RScript script = redissonClient.getScript(StringCodec.INSTANCE);

        Long result = script.eval(
                RScript.Mode.READ_WRITE,
                luaScript,
                RScript.ReturnType.INTEGER,
                Arrays.asList(userBookMarksKey, articleStatusKey),
                userId.toString()
        );



        if (result == -1) {
//            log.warn("用戶ID:{} 已經對該文章:{} 加入書籤 , 不允許重複加入書籤",userId,articleId);
//            throw new CustomRuntimeException("您已經對該文章加入書籤 , 不允許重複加入書籤");
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.REPEAT_OPERATION)
                    .data(Map.of("userId",ObjectUtils.defaultIfNull(userId, ""),
                            "articleId", ObjectUtils.defaultIfNull(articleId, "")))
                    .build();
        }



        log.info("文章加入用戶的書籤完成, 用戶ID:{}, 文章ID:{}, 新的文章書籤數:{}",userId, articleId, result);
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
////            throw new CustomRuntimeException("未登入或登入狀態已失效");
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

    /**
     * 從資料庫中讀取文章指標並回填Redis
     * @param articleId 文章ID
     * @return 文章指標VO對象
     */
    private AmsArticleStatusVo loadArticleStatusVo(long articleId) {
        log.info("開始執行 loadArticleStatus - articleId: {}", articleId);
        AmsArtStatus articleStatusFromDB = QueryArticleStatus(articleId);

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

        final String redisKey = RedisCacheKey.ARTICLE_STATUS.format(articleId);
        RMap<String, Integer> statusMap = redissonClient.getMap(redisKey);
        statusMap.put("viewsCount", views);
        statusMap.put("likesCount", likes);
        statusMap.put("bookmarksCount", bookmarks);
        statusMap.put("commentsCount", comments);
        Duration statusTtl = RedisCacheKey.ARTICLE_STATUS.getTtl();
        if(statusTtl!=null){
            statusMap.expire(statusTtl);
        }
        /*
        假設成功從Redis快取中獲取文章的指標則直接進行包裝並回傳
         */
        log.info("文章的狀態資訊 文章ID:{},指標數量:{}",articleId,statusMap.size());
        //進行包裝並回傳
        return AmsArticleStatusVo.builder()
                .viewsCount(views)
                .likesCount(likes)
                .bookmarksCount(bookmarks)
                .commentsCount(comments)
                .build();
    }

    /**
     * 從Redis中解析文章指標VO對象
     * @param articleId 文章ID
     * @return 文章指標VO對象
     */
    private AmsArticleStatusVo parseArticleStatusVoFromRedis(long articleId) {
        //嘗試從Redis取得文章的指標
        final String redisKey = RedisCacheKey.ARTICLE_STATUS.format(articleId);

        RMap<String, Integer> statusMap = redissonClient.getMap(redisKey);

        Map<String, Integer> stringIntMap = statusMap.readAllMap();

        if(stringIntMap.isEmpty()){
            log.debug("Redis中無文章指標數據 - 文章ID: {} , 文章快取鍵:{}", articleId,redisKey);
            return null;
        }

        /*
        獲取文章指標的值
        假設文章指標為空，則將指標值設為-1
         */
        Integer views = getMapValueAsInt(stringIntMap.get("viewsCount"));
        Integer likes = getMapValueAsInt(stringIntMap.get("likesCount"));
        Integer bookmarks = getMapValueAsInt(stringIntMap.get("bookmarksCount"));
        Integer comments = getMapValueAsInt(stringIntMap.get("commentsCount"));



//        Integer views = stringIntMap.get("viewsCount");
//        Integer likes = stringIntMap.get("likesCount");
//        Integer bookmarks = stringIntMap.get("bookmarksCount");
//        Integer comments = stringIntMap.get("commentsCount");

        log.debug("從Redis取得文章指標完成，articleId:{}，stringIntMap:{}", articleId, stringIntMap);
        return AmsArticleStatusVo.builder()
                .likesCount(likes)
                .bookmarksCount(bookmarks)
                .commentsCount(comments)
                .viewsCount(views)
                .build();
    }

    private Integer getMapValueAsInt(Object value) {
        if (value == null) {
            return -1; // 依照你的註釋，空值設為 -1
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        try {
            // 將 Object 轉為 String 再解析為 int
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            // 如果 Redis 裡存了非數字的髒數據，保險起見也返回 -1 或 0
            return -1;
        }
    }

    /**
     * 根據文章ID於資料庫中查詢文章指標
     * @param articleId 文章ID
     * @return 文章指標對象
     */
    private AmsArtStatus QueryArticleStatus(long articleId) {
        log.debug("從DB查詢文章狀態，articleId:{}", articleId);

        return amsArtStatusService.getOne(
                new LambdaQueryWrapper<AmsArtStatus>()
                        .select(AmsArtStatus::getViewsCount, AmsArtStatus::getBookmarksCount, AmsArtStatus::getCommentsCount, AmsArtStatus::getLikesCount)
                        .eq(AmsArtStatus::getArticleId, articleId)
                , false
        );
    }

    /**
     * 根據文章ID取得文章狀態
     * 採用Hash結構
     * @param articleId 文章ID
     * @return 文章指標對象
     */
    public AmsArticleStatusVo getArticleStatusVo(long articleId) {
        log.info("開始執行 getArticleStatusVo - articleId: {}", articleId);

        //先判斷是否存在該文章
        isArticleNotExists(articleId);


        //嘗試從Redis取得文章的指標
        final String redisKey = RedisCacheKey.ARTICLE_STATUS.format(articleId);
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
     * @param articleId 文章ID
     * @return 布林值, true 表示文章不存在，false 表示文章存在
     */
    @Override
    public void isArticleNotExists(Long articleId) {

        if (articleId == null || articleId <= 0) {
//            log.warn("非法文章ID: {}", articleId);
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.PARAM_ERROR)
                    .detailMessage("非法文章ID")
                    .data(Map.of("articleId", ObjectUtils.defaultIfNull(articleId, "")))
                    .build();
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
//                log.warn("該文章ID:{}不存在或已被刪除", articleId);
                throw BusinessRuntimeException.builder()
                        .iErrorCode(ResultCode.NOT_FOUND)
                        .detailMessage("該文章ID不存在於布隆過濾器中")
                        .data(Map.of("articleId", ObjectUtils.defaultIfNull(articleId, "")))
                        .build();
            }
        } catch (RedisConnectionException e) {
//            log.error("Redis 連線異常，跳過布隆過濾器檢查，articleId={}", articleId, e);
            boolean articleNotExistsFromDB = isArticleNotExistsFromDB(articleId);
            if(articleNotExistsFromDB){
                throw BusinessRuntimeException.builder()
                        .iErrorCode(ResultCode.NOT_FOUND)
                        .detailMessage("該文章ID不存在於資料庫中")
                        .data(Map.of("articleId", ObjectUtils.defaultIfNull(articleId, "")))
                        .build();
            }

        }

    }

    /**
     * 修改文章
     * @param articleId 文章ID
     * @param amsArticleUpdateDTO 更新的文章資料
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateArticle(Long articleId, AmsArticleUpdateDTO amsArticleUpdateDTO) {
        log.info("開始修改文章，articleId={}",articleId);
        /**
         * 透過布隆過濾器判斷文章id是否絕對不存在, 若不存在則拋出異常
         */
        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(RedisBloomFilterKey.ARTICLE_BLOOM_FILTER.getKey());

        boolean contains = bloomFilter.contains(articleId);
        if(!contains){
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.NOT_FOUND)
                    .detailMessage("文章不存在")
                    .data(Map.of("articleId", ObjectUtils.defaultIfNull(articleId, "")))
                    .build();
        }



        /**
         * 判斷更新資料是否為空
         */
        if(amsArticleUpdateDTO==null){
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.PARAM_MISSING)
                    .detailMessage("修改文章時更新資料為空")
                    .data(Map.of("articleId", ObjectUtils.defaultIfNull(articleId, "")))
                    .build();
        }

        /**
         * 判斷用戶是否登入, 是否為文章的作者或者管理員權限
         */
        if (!UserContextHolder.isCurrentUserLoggedIn()) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.UNAUTHORIZED)
                    .detailMessage("用戶未登入，拒絕對文章加入書籤")
                    .build();
        }


        /**
         * 判斷用戶是否登入, 是否為文章的作者或者管理員權限或者足夠的權限
         */

        Long userId = UserContextHolder.getCurrentUserId();

//        String userNameFromToken = UserContextHolder.getCurrentUserNickname();
        if (userId == null) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.UNAUTHORIZED)
                    .detailMessage("用戶ID為空，拒絕編輯文章")
                    .data(Map.of("articleId", ObjectUtils.defaultIfNull(articleId, "")))
                    .build();
        }
        log.info("修改文章 - 文章ID: {} , 文章標題:{} , 用戶ID:{}" , articleId ,amsArticleUpdateDTO.getTitle() ,userId);

        //判斷該用戶使否為文章的作者
        int isArtOwner= amsArtinfoService.isArticleOwner(articleId, userId);
        //假設isArtOwner為0表示不是文章的作者,否則是文章的作者
        if(isArtOwner == 0){
            log.info("用戶ID:{} , 並非為文章的作者，將判斷是否為管理員",userId);

            //並非文章的作者,因此需要判斷是否為管理員或者足夠的權限
            //進一步判斷是否為管理員或者足夠的權限
            /// TODO測試管理員權限是否成功實施
//            getHeader(HEADER_USER_ROLE);
            boolean currentUserAdmin = UserContextHolder.isCurrentUserAdmin();

            //判斷是否為管理員或者足夠的權限
            if(!currentUserAdmin){
                //不是文章的作者也不是管理員
                throw BusinessRuntimeException.builder()
                        .iErrorCode(ResultCode.FORBIDDEN)
                        .detailMessage("用戶權限不足，無權編輯該文章")
                        .data(Map.of(
                                "userId", ObjectUtils.defaultIfNull(userId, ""),
                                "articleId", ObjectUtils.defaultIfNull(articleId, "")
                        ))
                        .build();
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
        int articleContentUpdate = this.baseMapper.update(articleUpdateWrapper);
        if(articleContentUpdate == 0){
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.UPDATE_FAILED)
                    .detailMessage("編輯文章時更新文章內容失敗")
                    .data(Map.of(
                            "userId", ObjectUtils.defaultIfNull(userId, ""),
                            "articleId", ObjectUtils.defaultIfNull(articleId, "")
                    ))
                    .build();
        }


        /**
         * 更新文章info內容
         */
        //若某個屬性為null，不會進行更新
        LambdaUpdateWrapper<AmsArtinfo> artInfoUpdateWrapper = Wrappers.lambdaUpdate();
        artInfoUpdateWrapper.eq(AmsArtinfo::getArticleId, articleId);
        artInfoUpdateWrapper.set(AmsArtinfo::getCategoryId,amsArticleUpdateDTO.getCategoryId());
        boolean artInfoUpdate = amsArtinfoService.update(artInfoUpdateWrapper);
        if(!artInfoUpdate){

            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.UPDATE_FAILED)
                    .detailMessage("編輯文章時更新文章分類失敗")
                    .data(Map.of(
                            "userId", ObjectUtils.defaultIfNull(userId, ""),
                            "articleId", ObjectUtils.defaultIfNull(articleId, "")
                    ))
                    .build();
        }
        /**
         * 判斷文章是否存在舊標籤, 無論是否有新標籤都需要刪除舊標籤
         * 因為假設情況：
         * 1、有新標籤, 代表要新增標籤, 需要刪除舊標籤再增加新標籤
         * 2、沒有新標籤, 代表不設置標籤, 需要刪除舊標籤
         */
        //取得文章的舊標籤ID
        List<AmsArtTag> oldTagList = amsArtTagService.list(new LambdaQueryWrapper<AmsArtTag>().eq(AmsArtTag::getArticleId, articleId));
        log.info("文章ID:{} , 舊標籤數量:{}",articleId,oldTagList!=null ? oldTagList.size() : 0);
        //判斷該文章是否存在舊標籤
        if(oldTagList!=null && !oldTagList.isEmpty()){
            //刪除該文章原本的所有標籤
            boolean removed = amsArtTagService.removeByIds(oldTagList.stream().map(AmsArtTag::getId).toList());
            if(removed){
                log.info("文章ID:{},刪除舊標籤成功",articleId);
            }else {
                throw BusinessRuntimeException.builder()
                        .iErrorCode(ResultCode.DELETE_FAILED)
                        .detailMessage("編輯文章時刪除舊標籤失敗")
                        .data(Map.of(
                                "userId", ObjectUtils.defaultIfNull(userId, ""),
                                "articleId", ObjectUtils.defaultIfNull(articleId, "")
                        ))
                        .build();
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
                    throw BusinessRuntimeException.builder()
                            .iErrorCode(ResultCode.UPDATE_FAILED)
                            .detailMessage("編輯文章時增加新標籤失敗")
                            .data(Map.of(
                                    "userId", ObjectUtils.defaultIfNull(userId, ""),
                                    "articleId", ObjectUtils.defaultIfNull(articleId, "")
                            ))
                            .build();
                }

            }

        }
        log.info("修改文章成功 - 文章ID: {}, 標題: {}, 用戶ID: {}, 分類ID: {}, 標籤數: {}",
                articleId,
                amsArticleUpdateDTO.getTitle(),
                userId,
                amsArticleUpdateDTO.getCategoryId(),
                newArtTagsIdList != null ? newArtTagsIdList.size() : 0);
    }


    /**
     * 判斷文章是否不存在，透過查詢資料庫確認
     * @param articleId 文章ID
     * @return 如果文章不存在則返回true，否則返回false
     */
    private boolean isArticleNotExistsFromDB(Long articleId) {
        if (articleId == null || articleId <= 0) {
            log.warn("非法文章ID: {}", articleId);
            return true;
        }

        AmsArticle amsArticle = this.baseMapper.selectById(articleId);
        if(amsArticle == null){
            //資料庫中不存在該文章
            log.warn("該文章ID:{}不存在或已被刪除", articleId);
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
//            throw new CustomRuntimeException("文章ID不能小於等於0或為空");
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
//                throw new CustomRuntimeException("系統繁忙，請稍後再試");
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
//            throw new CustomRuntimeException("系統繁忙，請稍後再試");
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
//            throw new CustomRuntimeException("文章ID不能小於等於0或為空");
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
//                throw new CustomRuntimeException("系統繁忙，請稍後再試");
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
//            throw new CustomRuntimeException("系統繁忙，請稍後再試");
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
//            throw new CustomRuntimeException("未登入或登入狀態已失效");
//        }
//        Long userId = UserContextHolder.getCurrentUserId();
//        String userNameFromToken = UserContextHolder.getCurrentUserNickname();
//        if (userId == null) {
//            log.warn("用戶ID為空，拒絕對文章按讚");
//            throw new CustomRuntimeException("用戶ID缺失");
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
//                throw new CustomRuntimeException("該文章不存在或已被刪除");
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
//            throw new CustomRuntimeException("您已對該文章按讚過，請勿重複按讚");
//        }
//        //設置點讚值為++ , 注意原子性
//        long newLikes = atomicLong.incrementAndGet();
//        log.debug("newLikes:{}",newLikes);
//        return newLikes;
//    }

    /**
     * 刪除文章（邏輯刪除）
     * 只需將 AmsArtinfo.deleted 設置為 1，其他關聯資料會因為無法通過 AmsArtinfo 查詢而自然不可訪問
     *
     * @param articleId 文章ID
     * @return 刪除結果
     */
    @Transactional(rollbackFor = Exception.class)
    @DelayDoubleDelete(prefix = RedisOpenCacheKey.ArticleDetails.ARTICLE_DETAILS_PREFIX,
            key = RedisOpenCacheKey.ArticleDetails.ARTICLE_DETAILS_KEY)
    @Override
    public R<Void> deleteArticle(Long articleId) {
        log.info("開始邏輯刪除文章 - articleId: {}", articleId);

        // 1. 驗證文章是否存在
        isArticleNotExists(articleId);

        // 2. 檢查用戶是否登入
        if (!UserContextHolder.isCurrentUserLoggedIn()) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.UNAUTHORIZED)
                    .detailMessage("用戶未登入，無法刪除文章")
                    .build();
        }
        Long userId = UserContextHolder.getCurrentUserId();
        log.info("用戶嘗試刪除文章 - userId: {}, articleId: {}", userId, articleId);

        // 3. 獲取文章資訊（AmsArtinfo），同時驗證是否已被刪除
        AmsArtinfo artinfo = amsArtinfoService.getOne(
                new LambdaQueryWrapper<AmsArtinfo>()
                        .eq(AmsArtinfo::getArticleId, articleId)
        );

        if (artinfo == null) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.NOT_FOUND)
                    .detailMessage("文章資訊不存在")
                    .data(Map.of("articleId", ObjectUtils.defaultIfNull(articleId, "")))
                    .build();
        }

        // 4. 檢查文章是否已被刪除
        if (artinfo.getDeleted() != null && artinfo.getDeleted() == 1) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.DELETE_FAILED)
                    .detailMessage("文章已被刪除")
                    .data(Map.of("articleId", ObjectUtils.defaultIfNull(articleId, "")))
                    .build();
        }

        // 5. 權限驗證：只有作者或管理員可刪除
        boolean isAuthor = artinfo.getUserId().equals(userId);
        boolean isAdmin = UserContextHolder.isCurrentUserAdmin();

        if (!isAuthor && !isAdmin) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.FORBIDDEN)
                    .detailMessage("無權限刪除此文章，只有作者或管理員可以刪除")
                    .data(Map.of(
                            "userId", ObjectUtils.defaultIfNull(userId, ""),
                            "articleOwnerId", ObjectUtils.defaultIfNull(artinfo.getUserId(), "")
                    ))
                    .build();
        }

        // 6. 邏輯刪除 AmsArtinfo（設置 deleted = 1）
        boolean deleteArtinfo = amsArtinfoService.update(
                new LambdaUpdateWrapper<AmsArtinfo>()
                        .eq(AmsArtinfo::getArticleId, articleId)
                        .set(AmsArtinfo::getDeleted, 1)
        );

        if (!deleteArtinfo) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.DELETE_FAILED)
                    .detailMessage("刪除文章失敗")
                    .data(Map.of("articleId", ObjectUtils.defaultIfNull(articleId, "")))
                    .build();
        }

        // 7. 清理 Redis 快取
        try {
            cleanArticleRedisCache(articleId, artinfo.getCategoryId());
        } catch (Exception e) {
            log.error("清理文章 Redis 緩存失敗 - articleId: {}", articleId, e);
            // 快取清理失敗不影響主流程，僅記錄日誌
        }

        log.info("文章邏輯刪除成功 - articleId: {}, userId: {}", articleId, userId);
        return R.ok();
    }

    /**
     * 清理文章相關的 Redis 快取
     *
     * @param articleId  文章ID
     * @param categoryId 分類ID
     */
    private void cleanArticleRedisCache(Long articleId, Long categoryId) {
        log.info("開始清理文章 Redis 快取 - articleId: {}, categoryId: {}", articleId, categoryId);

        // 1. 使用 Lua 腳本原子清除文章計數相關快取
        final String likesKey = RedisCacheKey.ARTICLE_LIKES.format(articleId);
        final String viewsKey = RedisCacheKey.ARTICLE_VIEWS.format(articleId);
        final String commentsKey = RedisCacheKey.ARTICLE_COMMENTS.format(articleId);
        final String bookmarksKey = RedisCacheKey.ARTICLE_BOOKMARKS.format(articleId);
        final String statusKey = RedisCacheKey.ARTICLE_STATUS.format(articleId);
        final String likedUsersKey = RedisCacheKey.ARTICLE_LIKED_USERS.format(articleId);
        final String markedUsersKey = RedisCacheKey.ARTICLE_MARKED_USERS.format(articleId);

        RScript rScript = redissonClient.getScript(StringCodec.INSTANCE);
        Long luaResult = rScript.eval(
                RScript.Mode.READ_WRITE,
                RedisLuaScripts.DELETE_ARTICLE_SCRIPT,
                RScript.ReturnType.INTEGER,
                Arrays.asList(likesKey, viewsKey, commentsKey, bookmarksKey,
                        statusKey, likedUsersKey, markedUsersKey)
        );

        if (luaResult == null || luaResult != 1) {
            log.warn("Lua 腳本清理文章 Redis 數據返回異常 - articleId: {}, result: {}",
                    articleId, luaResult);
        } else {
            log.info("成功清理文章計數/集合快取 - articleId: {}", articleId);
        }

        // 2. 清除分類文章預覽列表快取（使用模式匹配）
        if (categoryId != null) {
            String categoryPattern = "AmsArticles:categoryId_" + categoryId + "*";
            RKeys keys = redissonClient.getKeys();
            Iterable<String> keysToDelete = keys.getKeysByPattern(categoryPattern);
            long count = 0;
            for (String key : keysToDelete) {
                redissonClient.getBucket(key).delete();
                count++;
            }
            log.info("清除分類文章列表快取 - categoryId: {}, 刪除鍵數量: {}", categoryId, count);
        }

        // 3. 清除文章留言相關快取
        String commentPattern = RedisOpenCacheKey.ArticleComments.COMMENT_DETAILS_PREFIX
                + ":" + articleId + "*";
        RKeys commentKeys = redissonClient.getKeys();
        Iterable<String> commentKeysToDelete = commentKeys.getKeysByPattern(commentPattern);
        long commentCount = 0;
        for (String key : commentKeysToDelete) {
            redissonClient.getBucket(key).delete();
            commentCount++;
        }
        log.info("清除文章留言快取 - articleId: {}, 刪除鍵數量: {}", articleId, commentCount);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAuthorInfo(Long userId, String nickName, String avatar) {
        // 更新文章作者資訊
        LambdaUpdateWrapper<AmsArtinfo> artInfoWrapper = new LambdaUpdateWrapper<>();
        artInfoWrapper.eq(AmsArtinfo::getUserId, userId);
        if (nickName != null) {
            artInfoWrapper.set(AmsArtinfo::getNickName, nickName);
        }
        if (avatar != null) {
            artInfoWrapper.set(AmsArtinfo::getAvatar, avatar);
        }
        amsArtinfoService.update(artInfoWrapper);

        // 更新評論作者資訊
        LambdaUpdateWrapper<AmsCommentInfo> commentInfoWrapper = new LambdaUpdateWrapper<>();
        commentInfoWrapper.eq(AmsCommentInfo::getUserId, userId);
        if (nickName != null) {
            commentInfoWrapper.set(AmsCommentInfo::getNickName, nickName);
        }
        if (avatar != null) {
            commentInfoWrapper.set(AmsCommentInfo::getAvatar, avatar);
        }
        amsCommentInfoService.update(commentInfoWrapper);
    }

    @Override
    public void updateAuthorAvatar(Long userId, String avatar) {
        if(userId == null){
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.PARAM_MISSING)
                    .detailMessage("userId is required")
                    .build();
        }
        if(avatar == null){
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.PARAM_MISSING)
                    .detailMessage("avatar is required")
                    .build();
        }

        // 更新文章作者頭像
        amsArtinfoService.update(new LambdaUpdateWrapper<AmsArtinfo>()
                .eq(AmsArtinfo::getUserId, userId)
                .set(AmsArtinfo::getAvatar, avatar));

        // 更新評論作者頭像
        amsCommentInfoService.update(new LambdaUpdateWrapper<AmsCommentInfo>()
                .eq(AmsCommentInfo::getUserId, userId)
                .set(AmsCommentInfo::getAvatar, avatar));
    }

    /**
     * 獲取正被文章使用的所有標籤ID列表
     * @return 所有正被文章使用的標籤ID列表
     */
    public List<AmsArticle> getAllDistinctArticleIds(){

        LambdaQueryWrapper<AmsArticle> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(AmsArticle::getId);
        queryWrapper.groupBy(AmsArticle::getId);

        return this.baseMapper.selectList(queryWrapper);

    }

    /**
     * 獲取文章預覽列表支持tag查詢
     */
//    @Override
//    public IPage<AmsArticlePreviewVo> getArticlesPreviewPage(Integer routePage,Long categoryId,List<Long> tagsId) {
//        log.debug("開始取得文章預覽列表 - categoryId: {}, routePage: {} ,tagsId.size: {}",categoryId, routePage,tagsId!=null?tagsId.size():0);
//
//        final int pageSize = 20;
//
//        Page<AmsArticlePreviewVo> page = new Page<>(routePage, pageSize);
//
////        //測試
////        if (tagsId == null) {
////            tagsId = new ArrayList<>();
////        }
////
////        tagsId.add(1950619174177013762L);
////        tagsId.add(1950621768463069185L);
//
//
//        IPage<AmsArticlePreviewVo> resultPage = this.baseMapper.getArticlesPreviewPage(page, routePage,categoryId,tagsId);
//
//
//
//        log.debug("文章預覽查詢完成，總條數:{}，當前頁:{}，每頁數量:{}", resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize());
//
//        //目標是重新包裝其中的amsArtTagList欄位
//        List<AmsArticlePreviewVo> articles = resultPage.getRecords();
//        if(articles.isEmpty()){
//            //不拋出異常只記錄
//            log.warn("獲取文章預覽時結果為空, categoryId:{} , routerPage:{} ,tagsId.size:{}",categoryId,routePage,tagsId!=null?tagsId.size():0);
//            //返回空的資料
//            return new Page<>();
////            throw BusinessRuntimeException.builder()
////                    .iErrorCode(ResultCode.ARTICLE_INTERNAL_ERROR)
////                    .detailMessage("獲取文章預覽時結果為空")
////                    .data(Map.of("categoryId",ObjectUtils.defaultIfNull(categoryId, ""),
////                            "routePage", ObjectUtils.defaultIfNull(routePage, "")))
////                    .build();
//        }
//        //獲取文章所需的標籤IDS，目標是取得標籤的名稱等資訊
//
//
//        Stream<AmsArtTagsVo> amsArtTagsVoStream = articles.stream().flatMap(item -> item.getAmsArtTagList().stream());
//        Set<Long> tagsIdSet = amsArtTagsVoStream.map(AmsArtTagsVo::getId).collect(Collectors.toSet());
//        log.debug("彙總標籤ID完成，標籤ID數量:{}", tagsIdSet.size());
//
//        Map<Long, AmsTags> tagsList = amsTagsService.getArtTagsByIds(tagsIdSet);
//        log.debug("從DB取得標籤實體數量:{}", tagsList != null ? tagsList.size() : 0);
//
//
//        articles.stream()
//                .flatMap(article -> article.getAmsArtTagList().stream())
//                .forEach(
//                        AmsArtTagsVo->{
//                            AmsTags tags = tagsList.get(AmsArtTagsVo.getId());
//                            if(tags!=null){
//                                AmsArtTagsVo.setName(tags.getName());
//                            }else{
//                                log.warn("標籤 ID {} 不存在於 tagsList 中", AmsArtTagsVo.getId());
//                            }
//                        });
//
//        articles.stream().forEach(item->{
//            List<AmsArtTagsVo> amsArtTagList = item.getAmsArtTagList();
//            if (amsArtTagList == null) {
//                log.debug("文章 文章ID:{} 無標籤列表", item.getArticleId());
//                return;
//            }
//            amsArtTagList.forEach(amsArtTagsVo -> {
//                AmsTags tags = tagsList.get(amsArtTagsVo.getId());
//                if (tags != null) {
//                    amsArtTagsVo.setName(tags.getName());
//                }
//            });
//        });
//
//        log.debug("文章預覽標籤填充完成，categoryId:{}，routePage:{}", categoryId, routePage);
//        log.debug("文章預覽結果返回，records:{}，total:{}", resultPage.getRecords().size(), resultPage.getTotal());
//        return resultPage;
//
//    }
//
//    /**
//     * Zset/set交集查詢文章預覽列表
//     */
//    @Override
//    public IPage<AmsArticlePreviewVo> getArticlesPreviewPage(Integer routePage,Long categoryId,List<Long> tagsId) {
//        log.debug("開始取得文章預覽列表 - categoryId: {}, routePage: {} ,tagsId.size: {}",categoryId, routePage,tagsId!=null?tagsId.size():0);
//
//        //試圖從快取中讀取
//
//
//        String categoryKey = "idx:category:" + categoryId;
//        String tempKey = "temp:search:" + UUID.randomUUID();
//
//        RScoredSortedSet<Long> tempSet = redissonClient.getScoredSortedSet(tempKey);
////        RScoredSortedSet<Long> sortedSet = redissonClient.getScoredSortedSet(categoryKey);
//
//
//        List<String> tagKeys = tagsId.stream()
//                .map(id -> "idx:tag:" + id)
//                .toList();
//
////        Set<Long> tagSet = new HashSet<>();
////        if (tagsId != null) {
////            for (Long tagId : tagsId) {
////                tagSet = redissonClient.getSet("idx:tag:" + tagId);
////
////            }
////        }
//
//        // 構建交集所需的 Key 列表
//        List<String> intersectionKeys = new ArrayList<>();
//        intersectionKeys.add(categoryKey); // 先加入分類 Key
//        intersectionKeys.addAll(tagKeys);  // 再加入所有標籤 Key
//
//
//        //實施該分類的所有文章ID與標籤的所有文章ID交集
//        //只保留分類以及標籤中都有的文章ID
//        tempSet.intersection(intersectionKeys.toArray(new String[0]));
//        tempSet.expire(Duration.ofSeconds(60));
//
//
//        final int pageSize = 20;
//
//        //從零開始, 假設第1頁 取20條就是0-19; 假設第2頁 取20條就是20-39
//        int start = (routePage - 1) * pageSize;
//        int end = start + pageSize - 1;
//
//        //獲取交集且排序過後的articleIds
//        Collection<Long> articleIds  = tempSet.valueRangeReversed(start, end);
//
//        if (articleIds.isEmpty()) {
//            return null;
//        }
//
//        RBuckets buckets = redissonClient.getBuckets();
//        List<String> keys = articleIds.stream()
//                .map(id -> "ams:article:preview:" + id)
//                .collect(Collectors.toList());
//
//
//        Map<String, AmsArticlePreviewVo> loadedArticles = buckets.get(keys.toArray(new String[0]));
//        List<AmsArticlePreviewVo> result = new ArrayList<>();
//
//        for (Long id : articleIds) {
//            AmsArticlePreviewVo vo = loadedArticles.get("ams:article:preview:" + id);
//            if (vo != null) result.add(vo);
//        }
//        IPage<AmsArticlePreviewVo> iPage = new Page<>(routePage, pageSize);
//        iPage.setRecords(result);
//        iPage.setTotal(result.size());
//
//        return iPage;
//    }


//    private R<Void> saveArticlesPreviewPageCache(IPage<AmsArticlePreviewVo> resultPage,Integer routePage, Long categoryId ,List<Long> tagIds){
//        //儲存文章詳情
//        RBucket<IPage<AmsArticlePreviewVo>> bucket = redissonClient.getBucket("AmsArticles:categoryId_" + categoryId + ":routerPage_" + routePage + ":articles");
//        bucket.set(resultPage);
//
//        //蒐集所有articleId以及該文章修改時間
//        Map<Long, Double> collect = Optional.ofNullable(resultPage)
//                .map(IPage::getRecords)
//                .orElse(Collections.emptyList())
//                .stream()
//                .collect(Collectors.toMap(AmsArticlePreviewVo::getArticleId, article -> (double) article.getUpdateTime()
//                        .atZone(ZoneId.systemDefault())
//                        .toInstant()
//                        .toEpochMilli()
//                ));
//
//
//        //紀錄該分類下有哪些文章ID並保存
//        RScoredSortedSet<Long> sortedSet = redissonClient.getScoredSortedSet("idx:category:" + categoryId);
//        sortedSet.addAll(collect);
//
//        //保存標籤
//        if (tagIds != null) {
//            for (Long tagId : tagIds) {
//                RSet<Long> tagSet = redissonClient.getSet("idx:tag:" + tagId);
//                tagSet.add(articleId);
//            }
//        }
//    }

}

