package com.shijiawei.secretblog.article.controller;

import com.shijiawei.secretblog.article.entity.AmsArtAction;
import com.shijiawei.secretblog.article.service.AmsArtActionService;
import com.shijiawei.secretblog.article.vo.AmsArtActionVo;
import com.shijiawei.secretblog.article.vo.UserLikedArticleVo;
import com.shijiawei.secretblog.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * 獲取用戶點讚過的文章列表
     * @param userId 用戶ID
     * @return 點讚文章列表
     */
    @GetMapping("/user/{userId}/liked-articles")
    public R<List<UserLikedArticleVo>> getLikedArticlesByUserId(@PathVariable("userId") Long userId) {
        List<UserLikedArticleVo> likedArticles = amsArtActionService.getLikedArticlesByUserId(userId);
        return R.ok(likedArticles);
    }
}
