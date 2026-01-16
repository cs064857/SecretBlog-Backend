package com.shijiawei.secretblog.article.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shijiawei.secretblog.article.entity.AmsArticle;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shijiawei.secretblog.article.vo.AmsArtTagsVo;
import com.shijiawei.secretblog.article.vo.AmsArticlePreviewVo;
import com.shijiawei.secretblog.article.vo.AmsArticleVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 針對表 ams_article(文章內容) 的數據庫操作 Mapper
 *
 * @author User
 * @since 2024-08-26
 */
public interface AmsArticleMapper extends BaseMapper<AmsArticle> {

    AmsArtTagsVo getArticleTagVoList(@Param("articleId") Long articleId);
    AmsArticleVo getArticleVo(@Param("articleId") Long articleId);
    int countCommentsByArticleId(@Param("articleId") Long articleId);
//    List<AmsArticlePreviewVo> getArticlesByCategoryIdAndPage(@Param("categoryId") Long categoryId);

//    IPage<AmsArticlePreviewVo> getArticlePreviewListByCategoryId(
    IPage<AmsArticlePreviewVo> getArticlesPreviewPage(
            Page<?> page,
            Integer routePage, Long categoryId
    );







    IPage<AmsArticlePreviewVo> getArticlesPreviewPage(
            Page<?> page,
            Integer routePage, Long categoryId, List<Long> tagsId
    );

    //    /**
//     * Zset/set交集查詢文章預覽列表
//     */
//    List<AmsArticlePreviewVo> getArticlesPreviewPageByArticleIds(
//            @Param("articleIds") List<Long> articleIds
//    );
}
