package com.shijiawei.secretblog.article.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shijiawei.secretblog.article.dto.AmsArticleUpdateDTO;
import com.shijiawei.secretblog.article.dto.ArticlePreviewQueryDto;
import com.shijiawei.secretblog.article.entity.AmsArticle;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shijiawei.secretblog.article.vo.AmsArticleEditVo;
import com.shijiawei.secretblog.article.vo.AmsArticlePreviewVo;
import com.shijiawei.secretblog.article.vo.AmsArticleVo;
import com.shijiawei.secretblog.article.vo.AmsSaveArticleVo;
import com.shijiawei.secretblog.common.dto.ArticlePreviewDTO;
import com.shijiawei.secretblog.common.utils.R;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;

import java.util.List;

/**
 * @author User
 * @description 針對表【ams_article(文章內容)】的數據庫操作Service
 * @createDate 2024-08-26 00:17:06
 */

public interface AmsArticleService extends IService<AmsArticle> {

    long saveArticles(AmsSaveArticleVo amsSaveArticleVo, HttpServletRequest httpServletRequest, Authentication authentication);

    //    List<AmsArticle> getListArticle();
//
    IPage<AmsArticlePreviewVo> getArticlesPreviewPage(Integer routePage, Long categoryId);

    AmsArticleVo getAmsArticleVo(Long articleId);

    AmsArticleVo getAmsArticleVoWithStatus(Long articleId);

    /**
     * 取得文章編輯資料(回傳原始 Markdown 內容)
     * @param articleId 文章ID
     * @return 文章編輯用 VO
     */
    AmsArticleEditVo getAmsArticleEditVo(Long articleId);

    Long incrementArticleLikes(Long articleId);

    Long incrementArticleBooksMarket(Long articleId);

    /**
     * 用戶移除文章書籤
     * @param articleId 文章ID
     * @return 新的文章書籤數
     */
    Long decrementArticleBooksMarket(Long articleId);

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

    /**
     * 批量獲取文章預覽 DTO
     * @param articleIds 文章 ID 列表
     * @return 文章預覽 DTO 列表
     */
    List<ArticlePreviewDTO> getBatchArticlePreviewDTOs(List<Long> articleIds);

    /**
     * 獲取文章預覽 DTO
     * @param articleId 文章ID
     * @return 文章預覽 DTO
     */
    ArticlePreviewDTO getArticlePreviewDTO(Long articleId);

//
//
////    Page<AmsArticle> getLatestArticles();
}
