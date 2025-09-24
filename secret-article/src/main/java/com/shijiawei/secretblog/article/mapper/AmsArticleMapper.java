package com.shijiawei.secretblog.article.mapper;

import com.shijiawei.secretblog.article.entity.AmsArticle;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shijiawei.secretblog.article.vo.AmsArticleTagsVo;
import com.shijiawei.secretblog.article.vo.AmsArticleVo;
import org.apache.ibatis.annotations.Param;

/**
 * 針對表 ams_article(文章內容) 的數據庫操作 Mapper
 *
 * @author User
 * @since 2024-08-26
 */
public interface AmsArticleMapper extends BaseMapper<AmsArticle> {

    AmsArticleTagsVo getArticleTagVoList(@Param("articleId") Long articleId);
    AmsArticleVo getArticleVo(@Param("articleId") Long articleId);
}
