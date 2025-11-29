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
}
