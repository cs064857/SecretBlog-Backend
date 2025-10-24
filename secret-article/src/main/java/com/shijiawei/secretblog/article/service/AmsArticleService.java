package com.shijiawei.secretblog.article.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shijiawei.secretblog.article.dto.AmsArticleUpdateDTO;
import com.shijiawei.secretblog.article.entity.AmsArticle;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shijiawei.secretblog.article.vo.AmsArticlePreviewVo;
import com.shijiawei.secretblog.article.vo.AmsArticleVo;
import com.shijiawei.secretblog.article.vo.AmsSaveArticleVo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;

/**
* @author User
* @description 针对表【ams_article(文章內容)】的数据库操作Service
* @createDate 2024-08-26 00:17:06
*/

public interface AmsArticleService extends IService<AmsArticle> {

    void saveArticles(AmsSaveArticleVo amsSaveArticleVo, HttpServletRequest httpServletRequest, Authentication authentication);

//    List<AmsArticle> getListArticle();
//
    Page<AmsArticlePreviewVo> getArticlesByCategoryIdAndPage(Long categoryId, Integer routePage);

    AmsArticleVo getAmsArticleVo(Long articleId);

    AmsArticleVo getAmsArticleVoWithStatus(Long articleId);

    Long incrementArticleLikes(Long articleId);

    Long incrementArticleBooksMarket(Long articleId);

    boolean isArticleNotExists(Long articleId);

    void updateArticleContent(Long articleId, AmsArticleUpdateDTO amsArticleUpdateDTO);

//
//
////    Page<AmsArticle> getLatestArticles();
}
