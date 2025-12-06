package com.shijiawei.secretblog.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shijiawei.secretblog.article.entity.AmsArtStatus;

/**
 * ClassName: AmsArtStatusService
 * Description:
 *
 * @Create 2025/8/3 上午2:35
 */
public interface AmsArtStatusService extends IService<AmsArtStatus> {

    /**
     * 修改文章點讚數
     *
     * @param articleId 文章ID
     * @param delta     遞增量（正數為增加，負數為減少）
     * @return 是否更新成功
     */
    boolean updateLikesCount(Long articleId, int delta);

    /**
     * 修改文章書籤數
     *
     * @param articleId 文章ID
     * @param delta     遞增量（正數為增加，負數為減少）
     * @return 是否更新成功
     */
    boolean updateBookmarksCount(Long articleId, int delta);

}
