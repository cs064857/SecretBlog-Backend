package com.shijiawei.secretblog.article.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shijiawei.secretblog.article.entity.AmsArticle;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shijiawei.secretblog.article.vo.AmsSaveArticleVo;

import java.util.List;

/**
* @author User
* @description 针对表【ams_article(文章內容)】的数据库操作Service
* @createDate 2024-08-26 00:17:06
*/

public interface AmsArticleService extends IService<AmsArticle> {

    void saveArticles(AmsSaveArticleVo amsSaveArticleVo);

//    List<AmsArticle> getListArticle();
//
    Page<AmsArticle> getArticlesByCategoryIdAndPage(Long categoryId, Integer routePage);
//
//
////    Page<AmsArticle> getLatestArticles();
}
