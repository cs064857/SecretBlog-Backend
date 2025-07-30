package com.shijiawei.secretblog.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shijiawei.secretblog.article.annotation.DelayDoubleDelete;
import com.shijiawei.secretblog.article.entity.AmsArtinfo;
import com.shijiawei.secretblog.article.entity.AmsTags;
import com.shijiawei.secretblog.article.service.AmsArtinfoService;
import com.shijiawei.secretblog.common.annotation.OpenCache;
import com.shijiawei.secretblog.article.annotation.OpenLog;
import com.shijiawei.secretblog.article.entity.AmsArticle;
import com.shijiawei.secretblog.article.service.AmsArticleService;
import com.shijiawei.secretblog.article.mapper.AmsArticleMapper;
import com.shijiawei.secretblog.article.vo.AmsSaveArticleVo;
import com.shijiawei.secretblog.common.utils.JwtService;
import com.shijiawei.secretblog.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    @Autowired
    private AmsArtinfoService amsArtinfoService;

    @Autowired
    private JwtService jwtService;

    @OpenLog//開啟方法執行時間紀錄
    @DelayDoubleDelete(prefix = "AmsArticles", key = "categoryId_#{#amsSaveArticleVo.categoryId}")
//    @DelayDoubleDelete(prefix = "AmsArticle",key = "articles",delay = 5,timeUnit = TimeUnit.SECONDS)//AOP延遲雙刪
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
            userNameFromToken = (String) hashMap.get("Nickname");
            if (userNameFromToken == null) {
                log.error("Token 中未找到 userName 信息");
            }
            // 使用從 Token 解析的 userId
            Long userId = Long.parseLong(userIdFromToken);
            log.debug("從Token解析的userId: {}", userId);
            log.debug("從Token解析的userName: {}", userNameFromToken);

            //從jwtToken中取得用戶ID及用戶名並存入artInfo中
            amsArtinfo.setCategoryId(amsSaveArticleVo.getCategoryId());


            amsArticle.setTitle(amsSaveArticleVo.getTitle());

            amsArticle.setContent(amsSaveArticleVo.getContent());

            //TODO 新增文章添加用戶ID與TAGID
//        amsArticle.setUserId(1L);
//        amsArticle.setTagId(1L);

            this.baseMapper.insert(amsArticle);

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
//    @Override
//    public Page<AmsArticle> getArticlesByCategoryIdAndPage(Long categoryId, Integer routePage) {
//        //根據categoryId分類查詢
//        Page<AmsArticle> iPage = this.baseMapper.selectPage(new Page<>(routePage, 20),
//                new LambdaQueryWrapper<AmsArticle>().eq(AmsArticle::getCategoryId, categoryId));
//
//        ///TODO 讚、喜歡、觀看等
//
////        List<AmsArticle> records = iPage.getRecords();
////
////        List<Long> articleIds = records.stream().map(AmsArticle::getId)
////                .toList();
////
////        List<AmsArtinfo> amsArtinfos = amsArtinfoService.list(new LambdaQueryWrapper<AmsArtinfo>().eq(AmsArtinfo::getArticleId, articleIds));
////
////        System.out.println(amsArtinfos);
//
//        return iPage;
//    }
//
//
//
////    @Override
////    public Page<AmsArticle> getLatestArticles() {
////
////
////    }
}





