package com.shijiawei.secretblog.article.mapper;

import com.shijiawei.secretblog.article.entity.AmsArticle;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shijiawei.secretblog.article.vo.AmsArticleTagsVo;
import com.shijiawei.secretblog.article.vo.AmsArticleVo;
import io.lettuce.core.dynamic.annotation.Param;
import org.apache.ibatis.annotations.Mapper;

/**
* @author User
* @description 针对表【ams_article(文章內容)】的数据库操作Mapper
* @createDate 2024-08-26 00:17:06
* @Entity com.shijiawei.secretblog.article.entity.AmsArticle
*/

public interface AmsArticleMapper extends BaseMapper<AmsArticle> {

    AmsArticleTagsVo getArticleTagVoList(@Param("articleId") Long articleId);
    AmsArticleVo getArticleVo(@Param("articleId") Long articleId);
}




