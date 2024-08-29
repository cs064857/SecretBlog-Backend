package com.shijiawei.secretblog.article.service;

import com.shijiawei.secretblog.article.entity.AmsArticle;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shijiawei.secretblog.article.vo.AmsSaveArticleVo;
import org.springframework.web.bind.annotation.RequestBody;

/**
* @author User
* @description 针对表【ams_article(文章內容)】的数据库操作Service
* @createDate 2024-08-26 00:17:06
*/

public interface AmsArticleService extends IService<AmsArticle> {

    void saveArticle(AmsSaveArticleVo amsSaveArticleVo);
}
