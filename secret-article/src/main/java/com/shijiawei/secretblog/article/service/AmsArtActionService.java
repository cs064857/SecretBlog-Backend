package com.shijiawei.secretblog.article.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shijiawei.secretblog.article.entity.AmsArtAction;
import com.shijiawei.secretblog.article.vo.AmsArtActionVo;
import com.shijiawei.secretblog.article.vo.UserLikedArticleVo;

/**
 * ClassName: AmsArtActionService
 * Description:
 *
 * @Create 2025/11/25 下午10:09
 */
public interface AmsArtActionService extends IService<AmsArtAction> {
    AmsArtActionVo getArticleActionStatusVo(Long articleId);

    /**
     * 根據用戶ID獲取喜歡（點讚）過的文章列表（分頁）
     * @param userId 用戶ID
     * @param routePage 頁碼（從 1 開始）
     * @return 喜歡（點讚）文章列表分頁
     */
    IPage<UserLikedArticleVo> getLikedArticlesByUserId(Long userId, Integer routePage);

    /**
     * 根據用戶ID獲取書籤文章列表（分頁）
     * @param userId 用戶ID
     * @param routePage 頁碼（從 1 開始）
     * @return 書籤文章列表分頁
     */
    IPage<UserLikedArticleVo> getBookmarkedArticlesByUserId(Long userId, Integer routePage);

    /**
     * 更新用戶對文章的點讚狀態
     * @param articleId 文章ID
     * @param userId 用戶ID
     * @param isLiked 點讚狀態 (1: 點讚, 0: 取消點讚)
     */
    void updateLikedStatus(Long articleId, Long userId, Byte isLiked);

    /**
     * 更新用戶對文章的書籤狀態
     * @param articleId 文章ID
     * @param userId 用戶ID
     * @param isBookmarked 書籤狀態 (1: 加入書籤, 0: 移除書籤)
     */
    void updateBookmarkedStatus(Long articleId, Long userId, Byte isBookmarked);
}

