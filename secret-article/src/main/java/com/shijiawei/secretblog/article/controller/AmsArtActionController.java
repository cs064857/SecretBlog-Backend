package com.shijiawei.secretblog.article.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shijiawei.secretblog.article.service.AmsArtActionService;
import com.shijiawei.secretblog.article.vo.AmsArtActionVo;
import com.shijiawei.secretblog.article.vo.UserLikedArticleVo;
import com.shijiawei.secretblog.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * ClassName: AmsArtActionController
 * Description:
 *
 * @Create 2025/11/25 下午10:13
 */

@Slf4j
@RequestMapping("/article")
@RestController
public class AmsArtActionController {

    @Autowired
    private AmsArtActionService amsArtActionService;

    /**
     * 獲取文章操作記錄
     * @param articleId 文章ID

     * @return 文章操作記錄
     */
    @GetMapping("/{articleId}/action-status")
    public R<AmsArtActionVo> getArtAction(@PathVariable(value = "articleId") Long articleId) {
        AmsArtActionVo amsArtActionVo = amsArtActionService.getArticleActionStatusVo(articleId);
        return R.ok(amsArtActionVo);
    }

    /**
     * 獲取用戶喜歡（點讚）過的文章列表（分頁）
     * @param userId 用戶ID
     * @param routePage 頁碼（從 1 開始）
     * @return 喜歡（點讚）文章列表分頁
     */
    @GetMapping("/user/{userId}/liked-articles")
    public R<IPage<UserLikedArticleVo>> getLikedArticlesByUserId(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "routePage", required = true) Integer routePage
    ) {
        IPage<UserLikedArticleVo> likedArticles = amsArtActionService.getLikedArticlesByUserId(userId, routePage);
        return R.ok(likedArticles);
    }

    /**
     * 獲取用戶書籤文章列表（分頁）
     * @param userId 用戶ID
     * @param routePage 頁碼（從 1 開始）
     * @return 書籤文章列表分頁
     */
    @GetMapping("/user/{userId}/bookmarked-articles")
    public R<IPage<UserLikedArticleVo>> getBookmarkedArticlesByUserId(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "routePage", required = true) Integer routePage
    ) {
        IPage<UserLikedArticleVo> bookmarkedArticles = amsArtActionService.getBookmarkedArticlesByUserId(userId, routePage);
        return R.ok(bookmarkedArticles);
    }

}
