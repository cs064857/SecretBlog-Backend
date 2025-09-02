package com.shijiawei.secretblog.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shijiawei.secretblog.article.annotation.DelayDoubleDelete;
import com.shijiawei.secretblog.article.entity.*;
import com.shijiawei.secretblog.article.feign.UserFeignClient;


import com.shijiawei.secretblog.article.service.*;
import com.shijiawei.secretblog.common.dto.UserBasicDTO;
import com.shijiawei.secretblog.article.vo.AmsArticlePreviewVo;
import com.shijiawei.secretblog.article.annotation.OpenLog;
import com.shijiawei.secretblog.article.mapper.AmsArticleMapper;
import com.shijiawei.secretblog.article.vo.AmsSaveArticleVo;
import com.shijiawei.secretblog.common.utils.JwtService;
import com.shijiawei.secretblog.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private JwtService jwtService;

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private AmsArtTagService amsArtTagService;

    @Autowired
    private AmsArtStatusService amsArtStatusService;

    @Autowired
    private AmsCategoryService amsCategoryService;

    @OpenLog//開啟方法執行時間紀錄
    @DelayDoubleDelete(prefix = "AmsArticles", key = "categoryId_#{#amsSaveArticleVo.categoryId}")
//    @DelayDoubleDelete(prefix = "AmsArticle",key = "articles",delay = 5,timeUnit = TimeUnit.SECONDS)//AOP延遲雙刪
    @Transactional
    @Override
    public void saveArticles(AmsSaveArticleVo amsSaveArticleVo) {
        AmsArticle amsArticle = new AmsArticle();
        AmsArtinfo amsArtinfo = new AmsArtinfo();

        String jwtToken = amsSaveArticleVo.getJwtToken();

        String userIdFromToken = null;
        String userNameFromToken = null;
        try {
            // 驗證並解析 JWT Token
            Map<String, Object> hashMap = jwtService.verifyJwt(jwtToken, HashMap.class);
            if (hashMap == null) {
                log.error("JWT Token 驗證失敗或已過期");
            }

            // 從 Token 中獲取用戶ID
            userIdFromToken = (String) hashMap.get("userId");
            if (userIdFromToken == null) {
                log.error("Token 中未找到 userId 信息");
            }
            // 從 Token 中獲取用戶ID
            userNameFromToken = (String) hashMap.get("nickname");
            if (userNameFromToken == null) {
                log.error("Token 中未找到 userName 信息");
            }
            // 使用從 Token 解析的 userId
            Long userId = Long.parseLong(userIdFromToken);
            log.debug("從Token解析的userId: {}", userId);
            log.debug("從Token解析的userName: {}", userNameFromToken);

            //從jwtToken中取得用戶ID及用戶名並存入artInfo中


            amsArticle.setTitle(amsSaveArticleVo.getTitle());

            amsArticle.setContent(amsSaveArticleVo.getContent());
            this.baseMapper.insert(amsArticle);
            amsArtinfo.setCategoryId(amsSaveArticleVo.getCategoryId());
            amsArtinfo.setUserName(userNameFromToken);
            amsArtinfo.setArticleId(amsArticle.getId());
            amsArtinfo.setUserId(userId);
            amsArtinfoService.save(amsArtinfo);
            //TODO 新增文章添加用戶ID與TAGID
            List<AmsArtTag> amsArtTagList = amsSaveArticleVo.getTagsId().stream().map(tagsId -> {

                AmsArtTag amsArtTag = new AmsArtTag();
                amsArtTag.setTagsId(tagsId);
                amsArtTag.setArticleId(amsArticle.getId());
                return amsArtTag;
            }).toList();
            amsArtTagService.saveBatch(amsArtTagList);

            AmsArtStatus amsArtStatus = new AmsArtStatus();
            amsArtStatus.setArticleId(amsArticle.getId());
            amsArtStatusService.save(amsArtStatus);

//        amsArticle.setUserId(1L);
//        amsArticle.setTagId(1L);



        } catch (NumberFormatException e) {
            log.error("Token 中的 userId 格式錯誤: {}", userIdFromToken, e);

        } catch (Exception e) {
            log.error("JWT Token 解析失敗", e);

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
//    @OpenCache(prefix = "AmsArticles", key = "categoryId_#{#categoryId}:routerPage_#{#routePage}:articles")//正確SpEL語法,變數使用#{#變數名}
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

        R<List<UserBasicDTO>> usersByIds = userFeignClient.getUsersByIds(userIdList);
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
//
//
//
////    @Override
////    public Page<AmsArticle> getLatestArticles() {
////
////
////    }
}

