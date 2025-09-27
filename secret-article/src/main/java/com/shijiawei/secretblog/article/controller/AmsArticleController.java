package com.shijiawei.secretblog.article.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shijiawei.secretblog.article.annotation.OpenLog;
import com.shijiawei.secretblog.article.entity.AmsArticle;
import com.shijiawei.secretblog.article.service.AmsArticleService;
import com.shijiawei.secretblog.article.vo.AmsArticlePreviewVo;
import com.shijiawei.secretblog.article.vo.AmsArticleVo;
import com.shijiawei.secretblog.article.vo.AmsSaveArticleVo;
import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.common.vaildation.Insert;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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
    public R saveArticle(@Validated(value = {Insert.class}) @RequestBody AmsSaveArticleVo amsSaveArticleVo, HttpServletRequest httpServletRequest, Authentication authentication) {
        log.info("amsSaveArticleVo:{}",amsSaveArticleVo);
        amsArticleService.saveArticles(amsSaveArticleVo,httpServletRequest,authentication);
        //log.info("完成");
        return R.ok();
    }
////    @GetMapping("/list")
////    public R<List<AmsArticle>> listArticle() {
////        List<AmsArticle> amsArticles = amsArticleService
////                .list(new LambdaQueryWrapper<AmsArticle>()
////                        .eq(AmsArticle::getDeleted,0));
//////        log.info("amsArticles:{}",amsArticles);
////        return R.ok(amsArticles);
////    }
//
//    @GetMapping("/list")
//    public R<List<AmsArticle>> listArticle() {
//        List<AmsArticle> articles = amsArticleService.getListArticle();
//
////        log.info("amsArticles:{}",amsArticles);
//        return R.ok(articles);
//    }
//

    /**
     * 根據articleId獲取文章內容以及資訊
     * @param articleId
     * @return
     */
    @GetMapping("articles/{articleId}")
    public R<AmsArticleVo> AmsArticleVo(@PathVariable Long articleId) {
//        log.info("articleId:{}",articleId);
//        AmsArticle article = amsArticleService.getById(articleId);
        AmsArticleVo article = amsArticleService.AmsArticleVo(articleId);
//        AmsArticleVo article = amsArticleService.getArticle(articleId);
//        log.info("article:{}",article);
        R<AmsArticleVo> ok = R.ok(article);
//        log.info("ok:{}",ok);
        return ok;
    }
//
    /**
     * 根據categoryId與routerPage分頁查詢文章列表
     * @param categoryId
     * @param routePage
     * @return
     */

    @GetMapping("/categories/{categoryId}/articles")
    public R<Page<AmsArticlePreviewVo>> getArticlesByCategoryIdAndPage(@PathVariable Long categoryId, @RequestParam(name = "routePage") Integer routePage) {
        log.info("categoryId:{}",categoryId);
        log.info("routePage:{}",routePage);
        Page<AmsArticlePreviewVo> Page  = amsArticleService.getArticlesByCategoryIdAndPage(categoryId,routePage);
        return R.ok(Page);
    }
//    /**
//     * 獲得最新文章的文章列表(按照日期遞減排序顯示文章)
//     */
////    @GetMapping("/latest")
////    public R<Page<AmsArticle>> getLatestArticles(){
////        Page<AmsArticle> Page  = amsArticleService.getLatestArticles();
////        return Page;
////    }
}
