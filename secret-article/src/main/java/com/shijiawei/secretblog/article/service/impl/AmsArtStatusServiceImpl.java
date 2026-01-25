package com.shijiawei.secretblog.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shijiawei.secretblog.article.entity.AmsArtStatus;
import com.shijiawei.secretblog.article.mapper.AmsArtStatusMapper;
import com.shijiawei.secretblog.article.service.AmsArtStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * ClassName: AmsArtStatusServiceImpl
 * Description:
 *
 * @Create 2025/8/3 上午2:36
 */
@Slf4j
@Service
public class AmsArtStatusServiceImpl extends ServiceImpl<AmsArtStatusMapper, AmsArtStatus> implements AmsArtStatusService {

    /**
     * 修改文章點讚數
     * @param articleId 文章ID
     * @param delta     遞增量（正數為增加，負數為減少）
     * @return 是否更新成功
     */
    @Override
    public boolean updateLikesCount(Long articleId, int delta) {
        if (articleId == null) {
            log.warn("修改點讚數失敗: articleId 為空");
            return false;
        }

        LambdaUpdateWrapper<AmsArtStatus> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AmsArtStatus::getArticleId, articleId)
                .setSql("likes_count = likes_count + " + delta);

        boolean result = this.update(updateWrapper);
        if (result) {
            log.debug("成功修改點讚數 {} 給文章ID: {}", delta, articleId);
        } else {
            log.warn("修改點讚數失敗，文章ID: {}（文章可能不存在）", articleId);
        }
        return result;
    }

    /**
     * 修改文章書籤數
     * @param articleId 文章ID
     * @param delta     遞增量（正數為增加，負數為減少）
     * @return 是否更新成功
     */
    @Override
    public boolean updateBookmarksCount(Long articleId, int delta) {
        if (articleId == null) {
            log.warn("修改書籤數失敗: articleId 為空");
            return false;
        }

        LambdaUpdateWrapper<AmsArtStatus> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AmsArtStatus::getArticleId, articleId)
                .setSql("bookmarks_count = bookmarks_count + " + delta);

        boolean result = this.update(updateWrapper);
        if (result) {
            log.debug("成功修改書籤數 {} 給文章ID: {}", delta, articleId);
        } else {
            log.warn("修改書籤數失敗，文章ID: {}（文章可能不存在）", articleId);
        }
        return result;
    }

    /**
     * 批量更新文章瀏覽數(用於定時從Redis同步瀏覽數至 DB)
     * @param viewsCountMap key=articleId, value=viewsCount
     * @return 更新成功的筆數
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateViewsCount(Map<Long, Integer> viewsCountMap) {
        if (viewsCountMap == null || viewsCountMap.isEmpty()) {
            log.debug("batchUpdateViewsCount: 傳入空 Map，跳過更新");
            return 0;
        }

        int successCount = 0;
        for (Map.Entry<Long, Integer> entry : viewsCountMap.entrySet()) {
            Long articleId = entry.getKey();
            Integer viewsCount = entry.getValue();
            if (articleId == null || viewsCount == null) {
                continue;
            }

            LambdaUpdateWrapper<AmsArtStatus> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(AmsArtStatus::getArticleId, articleId)
                    .set(AmsArtStatus::getViewsCount, viewsCount);

            boolean result = this.update(updateWrapper);
            if (result) {
                successCount++;
            } else {
                log.warn("更新瀏覽數失敗，articleId={}，可能文章不存在", articleId);
            }
        }

        log.info("批量更新瀏覽數完成，總數={}，成功={}",viewsCountMap.size(), successCount);
        return successCount;
    }

}

