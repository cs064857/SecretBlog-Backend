package com.shijiawei.secretblog.article.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shijiawei.secretblog.article.entity.AmsArticle;
import com.shijiawei.secretblog.article.service.AmsArticleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
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
    public void postArticle(@RequestBody AmsArticle article) {
        amsArticleService.saveArticle(article);
        log.info("完成");
    }
    @GetMapping("/list")
    public List<AmsArticle> listArticle() {
        List<AmsArticle> amsArticles = amsArticleService.list(new LambdaQueryWrapper<AmsArticle>().eq(AmsArticle::getIs_show,1));
        log.info("amsArticles:{}",amsArticles);
        return amsArticles;
    }
    @GetMapping("/get/{articleId}")
    public AmsArticle getArticle(@PathVariable Long articleId) {
        log.info("articleId:{}",articleId);
        AmsArticle article = amsArticleService.getById(articleId);
        log.info("article:{}",article);
        return article;
    }
}
