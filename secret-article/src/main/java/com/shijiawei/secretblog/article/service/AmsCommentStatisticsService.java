package com.shijiawei.secretblog.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shijiawei.secretblog.article.entity.AmsCommentStatistics;
import com.shijiawei.secretblog.article.mapper.AmsCommentStatisticsMapper;

/**
 * ClassName: AmsCommentStatisticsService
 * Description:
 *
 * @Create 2025/11/7 下午10:39
 */
public interface AmsCommentStatisticsService extends IService<AmsCommentStatistics> {

    /**
     * 修改留言點讚數
     * @param commentId 留言ID
     * @param delta     遞增量（正數為增加，負數為減少）
     * @return 是否更新成功
     */
    boolean updateLikesCount(Long commentId, int delta);

}
