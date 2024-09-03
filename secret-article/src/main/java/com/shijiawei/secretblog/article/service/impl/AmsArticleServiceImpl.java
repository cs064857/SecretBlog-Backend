package com.shijiawei.secretblog.article.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shijiawei.secretblog.article.entity.AmsArticle;
import com.shijiawei.secretblog.article.service.AmsArticleService;
import com.shijiawei.secretblog.article.mapper.AmsArticleMapper;
import com.shijiawei.secretblog.article.vo.AmsSaveArticleVo;
import org.springframework.stereotype.Service;

/**
 * @author User
 * @description 针对表【ams_article(文章內容)】的数据库操作Service实现
 * @createDate 2024-08-26 00:17:06
 */
@Service
public class AmsArticleServiceImpl extends ServiceImpl<AmsArticleMapper, AmsArticle> implements AmsArticleService {

    @Override
    public void saveArticle(AmsSaveArticleVo amsSaveArticleVo) {
        AmsArticle amsArticle = new AmsArticle();
        amsArticle.setTitle(amsSaveArticleVo.getTitle());
        amsArticle.setCategoryId(amsSaveArticleVo.getCategoryId());
        amsArticle.setContent(amsSaveArticleVo.getContent());
        //TODO 新增文章添加用戶ID與TAGID
//        amsArticle.setUserId(1L);
//        amsArticle.setTagId(1L);
        this.baseMapper.insert(amsArticle);



    }
}




