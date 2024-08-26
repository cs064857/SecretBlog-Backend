package com.shijiawei.secretblog.article.controller;

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
        Long l1 = 1828018170493947905L;
        List<Long> longList = Arrays.asList(l1);
        List<AmsArticle> amsArticles = amsArticleService.listByIds(longList);
        log.info("amsArticles:{}",amsArticles);
        return amsArticles;
    }
}
