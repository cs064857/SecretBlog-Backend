package com.shijiawei.secretblog.article.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shijiawei.secretblog.article.dto.AmsArticleUpdateDTO;
import com.shijiawei.secretblog.article.service.AmsArticleService;
import com.shijiawei.secretblog.article.vo.AmsArticlePreviewVo;
import com.shijiawei.secretblog.article.vo.AmsArticleVo;
import com.shijiawei.secretblog.article.vo.AmsSaveArticleVo;
import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.common.vaildation.Insert;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * ClassName: AmsArticleController
 * Description:
 *
 * @Create 2024/8/26 上午12:18
 */
@Slf4j
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

    /**
     * 編輯文章
     *
     * @return
     *
     */
    @PutMapping("/update/{articleId}")
    public R updateArticle(@PathVariable(value = "articleId",required = true) Long articleId,@RequestBody AmsArticleUpdateDTO amsArticleUpdateDTO){

        amsArticleService.updateArticle(articleId,amsArticleUpdateDTO);

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
     * 文章點讚數+1
     */
    @GetMapping("/articles/{articleId}/like")
    public R <Long> incrementArticleLikes(@PathVariable Long articleId) {
        Long likes = amsArticleService.incrementArticleLikes(articleId);
        return R.ok(likes);
    }

    /**
     * 移除文章點讚
     */
    @PostMapping("/articles/{articleId}/unlike")
    public R <Long> decrementArticleLikes(@PathVariable Long articleId) {
        Long likes = amsArticleService.decrementArticleLikes(articleId);
        return R.ok(likes);
    }
    /**
     * 根據articleId獲取文章內容以及資訊
     * @param articleId
     * @return
     */
    @Validated
    @GetMapping("articles/{articleId}")
    public R<AmsArticleVo> getAmsArticleVo( @Positive @NotNull @PathVariable Long articleId) {
//        log.info("articleId:{}",articleId);
//        AmsArticle article = amsArticleService.getById(articleId);
        AmsArticleVo article = amsArticleService.getAmsArticleVoWithStatus(articleId);
//        AmsArticleVo article = amsArticleService.getArticle(articleId);
//        log.info("article:{}",article);
        R<AmsArticleVo> ok = R.ok(article);
//        log.info("ok:{}",ok);
        return ok;
    }
//
    /**
     * 根據categoryId與routerPage分頁查詢文章預覽列表
     * @param categoryId
     * @param routePage
     * @return
     */

    @GetMapping("/categories/{categoryId}/articles")
    public R<IPage<AmsArticlePreviewVo>> getArticlesByCategoryIdAndPage(@PathVariable Long categoryId, @RequestParam(name = "routePage") Integer routePage) {
        log.info("categoryId:{}",categoryId);
        log.info("routePage:{}",routePage);
        IPage<AmsArticlePreviewVo> Page  = amsArticleService.getArticlesPreviewPage(categoryId,routePage);
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
