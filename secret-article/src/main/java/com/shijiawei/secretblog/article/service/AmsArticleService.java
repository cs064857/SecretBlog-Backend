package com.shijiawei.secretblog.article.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shijiawei.secretblog.article.dto.AmsArticleUpdateDTO;
import com.shijiawei.secretblog.article.dto.ArticlePreviewQueryDto;
import com.shijiawei.secretblog.article.entity.AmsArticle;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shijiawei.secretblog.article.vo.AmsArticlePreviewVo;
import com.shijiawei.secretblog.article.vo.AmsArticleVo;
import com.shijiawei.secretblog.article.vo.AmsSaveArticleVo;
import com.shijiawei.secretblog.common.utils.R;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;

import java.util.List;

/**
* @author User
* @description 针对表【ams_article(文章內容)】的数据库操作Service
* @createDate 2024-08-26 00:17:06
*/

public interface AmsArticleService extends IService<AmsArticle> {

    void saveArticles(AmsSaveArticleVo amsSaveArticleVo, HttpServletRequest httpServletRequest, Authentication authentication);

//    List<AmsArticle> getListArticle();
//
    IPage<AmsArticlePreviewVo> getArticlesPreviewPage(Integer routePage, Long categoryId);

    AmsArticleVo getAmsArticleVo(Long articleId);

    AmsArticleVo getAmsArticleVoWithStatus(Long articleId);

    Long incrementArticleLikes(Long articleId);

    Long incrementArticleBooksMarket(Long articleId);

    void isArticleNotExists(Long articleId);

    void updateArticle(Long articleId, AmsArticleUpdateDTO amsArticleUpdateDTO);

    Long decrementArticleLikes(Long articleId);

    /**
     * 刪除文章（邏輯刪除）
     * @param articleId 文章ID
     * @return 刪除結果
     */
    R<Void> deleteArticle(Long articleId);

    /**
     * 更新文章作者資訊(頭像、暱稱)
     * @param userId
     * @param nickName
     * @param avatar
     */
    void updateAuthorInfo(Long userId, String nickName, String avatar);

    /**
     * 更新文章作者頭像
     * @param userId
     * @param avatar
     */
    void updateAuthorAvatar(Long userId, String avatar);

    List<AmsArticle> getAllDistinctArticleIds();

//
//
////    Page<AmsArticle> getLatestArticles();
}
