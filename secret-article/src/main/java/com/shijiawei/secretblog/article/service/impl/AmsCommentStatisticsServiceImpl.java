package com.shijiawei.secretblog.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shijiawei.secretblog.article.entity.AmsCommentStatistics;
import com.shijiawei.secretblog.article.mapper.AmsCommentStatisticsMapper;
import com.shijiawei.secretblog.article.service.AmsCommentStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * ClassName: AmsCommentStatisticsServiceImpl
 * Description:
 *
 * @Create 2025/11/7 下午10:43
 */
@Slf4j
@Service
public class AmsCommentStatisticsServiceImpl extends ServiceImpl<AmsCommentStatisticsMapper, AmsCommentStatistics> implements AmsCommentStatisticsService {

    /**
     * 修改留言點讚數
     * @param commentId 留言ID
     * @param delta     遞增量（正數為增加，負數為減少）
     * @return 是否更新成功
     */
    @Override
    public boolean updateLikesCount(Long commentId, int delta) {
        if (commentId == null) {
            log.warn("修改留言點讚數失敗: commentId 為空");
            return false;
        }

        LambdaUpdateWrapper<AmsCommentStatistics> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AmsCommentStatistics::getCommentId, commentId)
                .setSql("likes_count = likes_count + " + delta);

        boolean result = this.update(updateWrapper);
        if (result) {
            log.debug("成功修改留言點讚數 {} 給留言ID: {}", delta, commentId);
        } else {
            log.warn("修改留言點讚數失敗，留言ID: {}（留言可能不存在）", commentId);
        }
        return result;
    }
}
