package com.shijiawei.secretblog.article.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shijiawei.secretblog.article.dto.AmsArticleUpdateDTO;
import com.shijiawei.secretblog.article.dto.ArticlePreviewQueryDto;
import com.shijiawei.secretblog.article.service.AmsArticleService;
import com.shijiawei.secretblog.article.service.AmsArtinfoService;
import com.shijiawei.secretblog.article.vo.AmsArticlePreviewVo;
import com.shijiawei.secretblog.article.vo.AmsArticleVo;
import com.shijiawei.secretblog.article.vo.AmsSaveArticleVo;
import com.shijiawei.secretblog.common.dto.ArticlePreviewDTO;
import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.common.vaildation.Insert;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RestController
@RequestMapping("/article")
public class AmsArticleController {
    @Autowired
    AmsArticleService amsArticleService;

    @Autowired
    AmsArtinfoService amsArtinfoService;

    @PostMapping("/save")
    public R saveArticle(@Validated(value = {Insert.class}) @RequestBody AmsSaveArticleVo amsSaveArticleVo, HttpServletRequest httpServletRequest, Authentication authentication) {
        log.debug("amsSaveArticleVo:{}",amsSaveArticleVo);
        long articleId = amsArticleService.saveArticles(amsSaveArticleVo, httpServletRequest,authentication);

        //log.info("完成");
        return R.ok(String.valueOf(articleId));
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

    /**
     * 刪除文章（邏輯刪除）
     * @param articleId 文章ID
     * @return 刪除結果
     */
    @PostMapping("/delete/{articleId}")
    public R<Void> deleteArticle(@NotNull @PathVariable(value = "articleId") Long articleId) {
        log.info("deleteArticle - articleId:{}", articleId);
        return amsArticleService.deleteArticle(articleId);
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
     * 加入文章書籤
     */
    @GetMapping("/articles/{articleId}/bookmark")
    public R<Long> incrementArticleBookmark(@PathVariable Long articleId) {
        Long bookmarks = amsArticleService.incrementArticleBooksMarket(articleId);
        return R.ok(bookmarks);
    }

    /**
     * 移除文章書籤
     */
    @PostMapping("/articles/{articleId}/unbookmark")
    public R<Long> decrementArticleBookmark(@PathVariable Long articleId) {
        Long bookmarks = amsArticleService.decrementArticleBooksMarket(articleId);
        return R.ok(bookmarks);
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


//    /**
//     * 根據categoryId與routerPage分頁查詢文章預覽列表
//     * @param categoryId
//     * @param routePage
//     * @return
//     */
//
//    @GetMapping("/categories/{categoryId}/articles")
//    public R<IPage<AmsArticlePreviewVo>> getArticlesByCategoryIdAndPage(@PathVariable Long categoryId, @RequestParam(name = "routePage") Integer routePage) {
//        log.info("categoryId:{}",categoryId);
//        log.info("routePage:{}",routePage);
//        IPage<AmsArticlePreviewVo> Page  = amsArticleService.getArticlesPreviewPage(categoryId,routePage);
//        return R.ok(Page);
//    }



    /**
     * 根據categoryId與routerPage分頁查詢文章預覽列表

     * @param routePage
     * @return
     */

    @GetMapping("/categories/articles")
    public R<IPage<AmsArticlePreviewVo>> getArticlesByCategoryIdAndPage(
            @RequestParam(value = "routePage" , required = true) Integer routePage,
            @RequestParam(value = "categoryId", required = false) Long categoryId
            ) {
        IPage<AmsArticlePreviewVo> Page  = amsArticleService.getArticlesPreviewPage(routePage,categoryId);
        return R.ok(Page);
    }

    /**
     * 根據 articleId 獲取文章預覽資料
     * @param articleId 文章ID
     * @return 文章預覽 DTO
     */
    @GetMapping("/internal/preview/{articleId}")
    public R<ArticlePreviewDTO> getArticlePreviewForSearch(@PathVariable Long articleId) {
        ArticlePreviewDTO preview = amsArticleService.getArticlePreviewDTO(articleId);
        return R.ok(preview);
    }

    /**
     * 批量獲取文章預覽資料
     * @param articleIds 文章 ID 列表
     * @return 文章預覽 DTO 列表
     */
    @PostMapping("/internal/preview/batch")
    public R<List<ArticlePreviewDTO>> getBatchArticlePreviewsForSearch(@RequestBody List<Long> articleIds) {
        List<ArticlePreviewDTO> previews = amsArticleService.getBatchArticlePreviewDTOs(articleIds);
        return R.ok(previews);
    }

    /**
     * 內部 Feign 專用：獲取所有文章 ID（
     * @return 所有文章 ID 列表
     */
    @GetMapping("/internal/article-ids")
    public R<List<Long>> getAllDistinctArticleIds() {
        List<Long> articleIds = amsArticleService.getAllDistinctArticleIds()
                .stream()
                .map(article -> article.getId())
                .toList();
        return R.ok(articleIds);
    }

    /**
     * 內部 Feign 專用：獲取文章總數量
     * 基於 AmsArtinfo 表統計未刪除的文章數量
     * @return 文章總數量
     */
    @GetMapping("/internal/article-count")
    public R<Long> getTotalArticleCount() {
        long count = amsArtinfoService.getTotalArticleCount();
        return R.ok(count);
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
