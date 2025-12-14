package com.shijiawei.secretblog.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shijiawei.secretblog.article.entity.AmsArtAction;
import com.shijiawei.secretblog.article.vo.UserLikedArticleVo;
import org.apache.ibatis.annotations.Param;

/**
 * ClassName: AmsArtActionMapper
 * Description:
 *
 * @Create 2025/11/25 下午10:09
 */
public interface AmsArtActionMapper extends BaseMapper<AmsArtAction> {

    /**
     * 根據用戶ID查詢喜歡（點讚）過的文章列表（分頁）
     * @param page 分頁參數
     * @param userId 用戶ID
     * @return 喜歡（點讚）文章列表分頁
     */
    IPage<UserLikedArticleVo> selectLikedArticlesByUserId(Page<?> page, @Param("userId") Long userId);

    /**
     * 根據用戶ID查詢書籤文章列表（分頁）
     * @param page 分頁參數
     * @param userId 用戶ID
     * @return 書籤文章列表分頁
     */
    IPage<UserLikedArticleVo> selectBookmarkedArticlesByUserId(Page<?> page, @Param("userId") Long userId);
}
