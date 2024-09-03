package com.shijiawei.secretblog.article.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shijiawei.secretblog.article.entity.AmsArticle;
import com.shijiawei.secretblog.article.service.AmsArticleService;
import com.shijiawei.secretblog.article.vo.AmsSaveArticleVo;
import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.common.vaildation.Insert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ClassName: AmsArticleController
 * Description:
 *
 * @Create 2024/8/26 上午12:18
 */
@Slf4j
//@RestController("/article")
@RestController
@RequestMapping("/article")
public class AmsArticleController {
    @Autowired
    AmsArticleService amsArticleService;

    @PostMapping("/save")
    public R saveArticle(@Validated(value = {Insert.class}) @RequestBody AmsSaveArticleVo amsSaveArticleVo) {
        log.info("amsSaveArticleVo:{}",amsSaveArticleVo);
        amsArticleService.saveArticle(amsSaveArticleVo);
        //log.info("完成");
        return R.ok();
    }
    @GetMapping("/list")
    public R<List<AmsArticle>> listArticle() {
        List<AmsArticle> amsArticles = amsArticleService
                .list(new LambdaQueryWrapper<AmsArticle>()
//                        //ams_article表中除了deleted字段以外其他全選
//                        .select(AmsArticle::getId,
//                                AmsArticle::getTitle,
//                                AmsArticle::getContent,
////                                AmsArticle::getCategoryId,
//                                AmsArticle::getTagId,
//                                AmsArticle::getUserId,
//                                AmsArticle::getCreateTime,
//                                AmsArticle::getUpdateTime)
                        .eq(AmsArticle::getDeleted,0));
//        log.info("amsArticles:{}",amsArticles);
        return R.ok(amsArticles);
    }

    @GetMapping("/get/{articleId}")
    public R<AmsArticle> getArticle(@PathVariable Long articleId) {
//        log.info("articleId:{}",articleId);
        AmsArticle article = amsArticleService.getById(articleId);
//        log.info("article:{}",article);
        R<AmsArticle> ok = R.ok(article);
//        log.info("ok:{}",ok);
        return ok;
    }
}
