package com.shijiawei.secretblog.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shijiawei.secretblog.article.entity.AmsArtAction;
import com.shijiawei.secretblog.article.vo.AmsArtActionVo;
import com.shijiawei.secretblog.article.vo.UserLikedArticleVo;

import java.util.List;

/**
 * ClassName: AmsArtActionService
 * Description:
 *
 * @Create 2025/11/25 下午10:09
 */
public interface AmsArtActionService extends IService<AmsArtAction> {
    AmsArtActionVo getArticleActionStatusVo(Long articleId);

    /**
     * 根據用戶ID獲取點讚過的文章列表
     * @param userId 用戶ID
     * @return 點讚文章列表
     */
    List<UserLikedArticleVo> getLikedArticlesByUserId(Long userId);

    /**
     * 更新用戶對文章的點讚狀態
     * @param articleId 文章ID
     * @param userId 用戶ID
     * @param isLiked 點讚狀態 (1: 點讚, 0: 取消點讚)
     */
    void updateLikedStatus(Long articleId, Long userId, Byte isLiked);
}

