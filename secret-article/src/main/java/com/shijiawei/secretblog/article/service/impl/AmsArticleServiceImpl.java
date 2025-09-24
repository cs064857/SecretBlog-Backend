package com.shijiawei.secretblog.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shijiawei.secretblog.article.annotation.DelayDoubleDelete;
import com.shijiawei.secretblog.article.entity.*;
import com.shijiawei.secretblog.article.feign.UserFeignClient;


import com.shijiawei.secretblog.article.service.*;
import com.shijiawei.secretblog.article.vo.AmsArticleTagsVo;
import com.shijiawei.secretblog.article.vo.AmsArticleVo;
import com.shijiawei.secretblog.common.dto.UserBasicDTO;
import com.shijiawei.secretblog.article.vo.AmsArticlePreviewVo;
import com.shijiawei.secretblog.article.annotation.OpenLog;
import com.shijiawei.secretblog.article.mapper.AmsArticleMapper;
import com.shijiawei.secretblog.article.vo.AmsSaveArticleVo;
import com.shijiawei.secretblog.common.utils.R;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.shijiawei.secretblog.common.utils.UserContextHolder;

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
    public AmsArticleVo getArticle(Long articleId) {

        AmsArticleVo amsArticleVo1 = this.baseMapper.getArticleVo(articleId);

        AmsArticle amsArticle = this.baseMapper.selectById(articleId);
        AmsArtinfo amsArtinfo = amsArtinfoService.getOne(new LambdaQueryWrapper<AmsArtinfo>().eq(AmsArtinfo::getArticleId, articleId));
        AmsCategory amsCategory = amsCategoryService.getById(amsArtinfo.getCategoryId());
        AmsArtStatus amsArtStatus = amsArtStatusService.getOne(new LambdaQueryWrapper<AmsArtStatus>().eq(AmsArtStatus::getArticleId, amsArticle.getId()));
        long commentsCount = amsCommentInfoService.count(new LambdaQueryWrapper<AmsCommentInfo>().eq(AmsCommentInfo::getArticleId, amsArticle.getId()));
        List<AmsArtTag> amsArtTagList = amsArtTagService.list(new LambdaQueryWrapper<AmsArtTag>().eq(AmsArtTag::getArticleId, amsArticle.getId()));
        /*
        設置文章標籤
         */
            /*
            搜集文章標籤對象的所有標籤ID
             */
            List<Long> amsArtTagsIdList = amsArtTagList.stream().map(AmsArtTag::getTagsId).toList();

            /*
            透過標籤ID列表獲取標籤對象列表
             */
            List<AmsTags> amsTagsList = amsTagsService.listByIds(amsArtTagsIdList);




        AmsArticleVo amsArticleVo = new AmsArticleVo();
        /*
        設置文章內容、文章標題、文章ID
         */
        BeanUtils.copyProperties(amsArticle, amsArticleVo);
        /*
        設置文章資訊
         */
        ///  TODO缺少userName
        BeanUtils.copyProperties(amsArtinfo, amsArticleVo,"id","articleId");
        /*
        設置分類資訊
         */
        amsArticleVo.setCategoryId(amsCategory.getId());
        amsArticleVo.setCategoryName(amsCategory.getCategoryName());
        /*
        設置文章狀態
         */
        BeanUtils.copyProperties(amsArtStatus, amsArticleVo,"id","articleId");
        /*
        設置評論數量
         */
        amsArticleVo.setCommentsCount((int) commentsCount);



        /*
        包裝標籤對象列表到文章VO中
         */
        List<AmsArticleTagsVo> amsArticleTagsVo = amsTagsList.stream().map(amsTags -> {
            AmsArticleTagsVo tagsVo = new AmsArticleTagsVo();
            BeanUtils.copyProperties(amsTags, tagsVo);
            return tagsVo;
        }).toList();

        amsArticleVo.setAmsArticleTagsVoList(amsArticleTagsVo);
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
}
